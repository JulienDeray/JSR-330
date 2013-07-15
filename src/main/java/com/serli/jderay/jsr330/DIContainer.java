/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
    

public class DIContainer {

    private static final Logger logger = LoggerFactory.getLogger(DIContainer.class);
    
    public static DIContainer createWith( ContainerConfig containerConfig ) throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, NoSuchMethodException {
        DIContainer container = new DIContainer();
        container.init( containerConfig );
        return container;
    }
    
    private void init(ContainerConfig containerConfig) throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, NoSuchMethodException {
        logger.info("--------------------- Initialisation of Dependencie Injection Container ---------------------");
        containerConfig.configure();
        logger.info("---------------------------------------------------------------------------------------------");
    }
    
    public <T> T getInstance( Class<T> clazz ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException {
        T t = dynamicallyInstantiate( clazz );
        resolveSetterInjections( t );
        resolveFieldInjections( t );
        return t;
    }

    private <T> T getInstance( Inheritance<T> impl ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException {
        T t;

        if ( impl.isSingleton() ) {
            t = impl.getSingletonInstance();
            resolveSetterInjections( t );
            resolveFieldInjections( t );
        }
        else if ( impl.hasProvider() ) {
            Provider provider = impl.getProvider();
            resolveSetterInjections( provider );
            resolveFieldInjections( provider );

            t = (T) provider.get();
        }
        else {
            t = dynamicallyInstantiate( impl.getImplementation() );
            resolveSetterInjections( t );
            resolveFieldInjections( t );
        }

        return t;
    }

    private <T> T dynamicallyInstantiate(Class<T> clazz) throws InstantiationException, IllegalAccessException, NoImplementationException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException {
        Constructor[] constructors = clazz.getDeclaredConstructors();
        Constructor annotatedConstructor = getAnnotatedConstructor( constructors );
        if ( annotatedConstructor != null ) {
            Object[] newParameters = new Object[ annotatedConstructor.getParameterTypes().length ];
            for (int i = 0; i < annotatedConstructor.getParameterTypes().length; i++) {
                Class parameterClass = annotatedConstructor.getParameterTypes()[i];
                Annotation[] parameterAnnotations = annotatedConstructor.getParameterAnnotations()[i];

                if ( parameterIsAfield( clazz, parameterClass ) ) {
                    Inheritance impl = InheritanceManager.getInheritance( parameterClass, getQualifiers( parameterAnnotations ), getName( parameterAnnotations ) );
                    newParameters[i] = getInstance( impl );
                }
            }
            logger.info("@Inject on constructor : {}", annotatedConstructor.toString());
            return (T) annotatedConstructor.newInstance( newParameters );
        }
        else
            return clazz.newInstance();
    }

    private boolean parameterIsAfield(Class clazz, Class parameterClass ) throws FinalFieldException, NoSuchFieldException {
        System.out.println(clazz.getDeclaredFields()[0]);
        System.out.println(parameterClass.getCanonicalName());

        Field field = null;
        boolean ok = false;
        Field[] fields = clazz.getDeclaredFields();
        for ( int i = 0; i < fields.length; i++ ) {
            if ( fields[i].getType().getName().equals( parameterClass.getName() ) ) {
                ok = true;
                field = fields[i];
            }
        }

        if ( ok ) {
            if (java.lang.reflect.Modifier.isFinal(field.getModifiers() ))
                throw new FinalFieldException();
            else
                return true;
        }
        else
            return false;
    }

    private <T> T resolveFieldInjections(T t) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException {
        List<Field> fieldsToInject = new ArrayList<>();

        for (Field field : t.getClass().getDeclaredFields()) {
            Annotation[] annotations = field.getDeclaredAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation instanceof Inject) {
                    if (java.lang.reflect.Modifier.isFinal(field.getModifiers() ))
                        throw new FinalFieldException();
                    else
                        fieldsToInject.add(field);
                }
            }
        }
        return injectFields(t, fieldsToInject);
    }
    
    private <T> T resolveSetterInjections(T t) throws NoImplementationException, AmbiguousImplementationsException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException {
        for ( Method method : t.getClass().getDeclaredMethods() ) {
            if ( method.getName().startsWith("set") && method.getGenericParameterTypes().length == 1 && method.getGenericReturnType() == void.class ) {
                for ( Annotation annotation : method.getAnnotations() )
                    if ( annotation instanceof Inject ) {
                        Annotation[] parameterAnnotations = method.getParameterAnnotations()[0];
                        Class toImpl = method.getParameterTypes()[0];

                        Inheritance impl = InheritanceManager.getInheritance( toImpl, getQualifiers( parameterAnnotations ), getName( parameterAnnotations ) );
                        Object[] args = { getInstance( impl ) };
                        
                        logger.info("@Inject on setter : {}", method.toString());
                        method.invoke(t, args);
                    }
            }
        }
        return t;
    }
    
    private <T> T injectFields(T t, List<Field> fields) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException {
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
