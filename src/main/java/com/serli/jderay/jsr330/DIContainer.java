/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.MultipleConstructorsInjection;
import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.DoesNotImplementException;
import com.serli.jderay.jsr330.exceptions.IsNotScopeException;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import com.serli.jderay.jsr330.exceptions.NotAnInterfaceException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
    

public class DIContainer {

    private static final Logger logger = LoggerFactory.getLogger(DIContainer.class);
    
    public static DIContainer createWith( ContainerConfig containerConfig ) throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
        DIContainer container = new DIContainer();
        container.init( containerConfig );
        return container;
    }
    
    private void init(ContainerConfig containerConfig) throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
        logger.info("--------------------- Initialisation of Dependencie Injection Container ---------------------");
        containerConfig.configure();
        logger.info("---------------------------------------------------------------------------------------------");
    }
    
    public <T> T getInstance( Class<T> clazz ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection {
        T t = dynamicallyInstantiate( clazz );
        resolveSetterInjections( t );
        resolveFieldInjections( t );
        return t;
    }

    private <T> T getInstance( Inheritance<T> impl ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection {
        T t = impl.isSingleton() ? impl.getSingletonInstance() : dynamicallyInstantiate( impl.getImplementation() );
        resolveFieldInjections( t );
        resolveSetterInjections( t );
        return t;
    }

    private <T> T dynamicallyInstantiate(Class<T> clazz) throws InstantiationException, IllegalAccessException, NoImplementationException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection {
        Constructor[] constructors = clazz.getDeclaredConstructors();
        Constructor annotatedConstructor = getAnnotatedConstructor( constructors );
        if ( annotatedConstructor != null ) {
            Object[] newParameters = new Object[ annotatedConstructor.getParameterTypes().length ];
            for (int i = 0; i < annotatedConstructor.getParameterTypes().length; i++) {
                Class parameterClass = annotatedConstructor.getParameterTypes()[i];
                Annotation[] parameterAnnotations = annotatedConstructor.getParameterAnnotations()[i];
                
                Inheritance impl = InheritanceManager.getInheritance( parameterClass, getQualifiers( parameterAnnotations ), getName( parameterAnnotations ) );
                newParameters[i] = getInstance( impl );
            }
            logger.info("@Inject on constructor : {}", annotatedConstructor.toString());
            return (T) annotatedConstructor.newInstance( newParameters );
        }
        else
            return clazz.newInstance();
    }

    private <T> T resolveFieldInjections(T t) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection {
        List<Field> fieldsToInject = new ArrayList<>();

        for (Field field : t.getClass().getDeclaredFields()) {
            Annotation[] annotations = field.getDeclaredAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation instanceof Inject) {
                    fieldsToInject.add(field);
                }
            }
        }
        return injectFields(t, fieldsToInject);
    }
    
    private <T> T resolveSetterInjections(T t) throws NoImplementationException, AmbiguousImplementationsException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection {
        for ( Method method : t.getClass().getDeclaredMethods() ) {
            if ( method.getName().startsWith("set") && method.getGenericParameterTypes().length == 1 && method.getGenericReturnType() == void.class ) {
                for ( Annotation annotation : method.getAnnotations() )
                    if ( annotation instanceof Inject ) {
                        Annotation[] parameterAnnotations = method.getParameterAnnotations()[0];
                        Class toImpl = method.getParameterTypes()[0];

                        Inheritance impl = InheritanceManager.getInheritance( toImpl, getQualifiers( parameterAnnotations ), getName( parameterAnnotations ) );
                        Object[] args = { getInstance( impl ) };

                        method.invoke(t, args);
                    }
            }
        }
        return t;
    }
    
    private <T> T injectFields(T t, List<Field> fields) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection {
        for (Field field : fields) {
            
            Class<?> clazzToImpl = field.getType();
            Inheritance impl = InheritanceManager.getInheritance( clazzToImpl, getQualifiers( field.getDeclaredAnnotations() ), getName( field.getDeclaredAnnotations() ) );
            
            field.setAccessible(true);
           
            field.set(t, getInstance( impl ) );
            logger.info("@Inject : {} --> {}", clazzToImpl, impl);

            field.setAccessible(false);
        }

        return t;
    }
    
    private List<Class<?>> getQualifiers( Annotation[] annotations ) {
        List<Class<?>> qualifiers = new ArrayList<>();

        for (Annotation annotation : annotations) {
            if ( !(annotation instanceof Inject) ) {
                qualifiers.add( annotation.annotationType() );
            }
        }
        return qualifiers;
    }

    private String getName( Annotation[] annotations ) {
        for (Annotation annotation : annotations) {
            if ( annotation instanceof Named ) {
                return ( (Named) annotation).value();
            }
        }
        return "";
    }

    private Constructor getAnnotatedConstructor(Constructor[] constructors) throws MultipleConstructorsInjection {
        List<Constructor> annotatedConstructors = new ArrayList<>();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            Annotation[] annotations = constructor.getDeclaredAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation instanceof Inject) {
                    annotatedConstructors.add(constructor);
                }
            }
        }
        
        if ( annotatedConstructors.isEmpty() )
            return null;
        else if ( annotatedConstructors.size() == 1 )
            return annotatedConstructors.get(0);
        else
            throw new MultipleConstructorsInjection();
    }
    
    public static void reset() {
        InheritanceManager.reset();
    }
}
