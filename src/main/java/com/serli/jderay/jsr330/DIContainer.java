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
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
    

public class DIContainer {

    private static final Logger logger = LoggerFactory.getLogger(DIContainer.class);
    
    public static DIContainer createWith( ContainerConfig containerConfig ) throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, NoSuchMethodException, InvocationTargetException, MultipleConstructorsInjection, NoSuchFieldException, FinalFieldException {
        DIContainer container = new DIContainer();
        container.init( containerConfig );
        return container;
    }
    
    private void init(ContainerConfig containerConfig) throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, NoSuchMethodException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException, InvocationTargetException {
        logger.info("--------------------- Initialisation of Dependencie Injection Container ---------------------");
        containerConfig.configure();
        logger.info("---------------------------------------------------------------------------------------------");
    }
    
    public <T> T getInstance( Class<T> clazz ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException, NoSuchMethodException {
        T t = dynamicallyInstantiate( clazz );
        resolveSetterInjections( t );
        resolveFieldInjections( t );
        return t;
    }

    private <T> T getInstance( Inheritance<T> impl ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException, NoSuchMethodException {
        T t;

        if ( impl.isSingleton() ) {
            t = impl.getSingletonInstance();
            resolveInjections( t );
        }
        else if ( impl.hasProvider() ) {
            Provider provider = impl.getProvider();
            resolveInjections( provider );

            t = (T) provider.get();
        }
        else {
            t = dynamicallyInstantiate( impl.getImplementation() );
            resolveInjections( t );
        }

        return t;
    }

    private <T> void resolveInjections(T t) throws IllegalAccessException, NoImplementationException, InstantiationException, FinalFieldException, NoSuchFieldException, MultipleConstructorsInjection, InvocationTargetException, AmbiguousImplementationsException, NoSuchMethodException {
        resolveSetterInjections( t );
        resolveFieldInjections( t );
    }

    private <T> T dynamicallyInstantiate(Class<T> clazz) throws InstantiationException, IllegalAccessException, NoImplementationException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException, NoSuchMethodException {
        Constructor[] constructors = clazz.getDeclaredConstructors();
        Constructor annotatedConstructor = getAnnotatedConstructor( constructors );
        if ( annotatedConstructor != null ) {
            Object[] newParameters = new Object[ annotatedConstructor.getParameterTypes().length ];
            for (int i = 0; i < annotatedConstructor.getParameterTypes().length; i++) {
                Class parameterClass = annotatedConstructor.getParameterTypes()[i];
                Annotation[] parameterAnnotations = annotatedConstructor.getParameterAnnotations()[i];
                Class<?> providedClass = null;
                if ( parameterIsAfield( clazz, parameterClass ) ) {
                    if ( parameterClass.isAssignableFrom(Provider.class) ) {
                        Type t = annotatedConstructor.getGenericParameterTypes()[0];
                        ParameterizedType ptype = (ParameterizedType) t;
                        providedClass = (Class<?>) ptype.getActualTypeArguments()[0];
                    }
                    Inheritance impl = InheritanceManager.getInheritance( parameterClass, getQualifiers( parameterAnnotations ), getName( parameterAnnotations ), providedClass );
                    newParameters[i] = getInstance( impl );
                }
            }
            logger.info("@Inject on constructor : {}", annotatedConstructor.toString());
            return (T) annotatedConstructor.newInstance( newParameters );
        }
        else {
            if ( clazz.isInterface() )
                return findEligibleClass(clazz);
            else
                return clazz.newInstance();
        }
    }

    private <T> T findEligibleClass(Class<T> clazz) throws NoImplementationException, AmbiguousImplementationsException, IllegalAccessException, InstantiationException, FinalFieldException, MultipleConstructorsInjection, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        Inheritance impl = InheritanceManager.getInheritance(clazz, new ArrayList<Class<?>>(), "", null);
        return (T) getInstance( impl );
    }

    private boolean parameterIsAfield(Class clazz, Class parameterClass ) throws FinalFieldException, NoSuchFieldException {
        Field[] fields = clazz.getDeclaredFields();
        for ( int i = 0; i < fields.length; i++ ) {
            if ( fields[i].getType().getName().equals( parameterClass.getName() ) ) {
                return true;
            }
        }
        return false;
    }

    private <T> T resolveFieldInjections(T t) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException, NoSuchMethodException {
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
    
    private <T> T resolveSetterInjections(T t) throws NoImplementationException, AmbiguousImplementationsException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException, NoSuchMethodException {
        for ( Method method : t.getClass().getDeclaredMethods() ) {
            if ( method.getName().startsWith("set") && method.getGenericParameterTypes().length == 1 && method.getGenericReturnType() == void.class ) {
                for ( Annotation annotation : method.getAnnotations() )
                    if ( annotation instanceof Inject ) {
                        Annotation[] parameterAnnotations = method.getParameterAnnotations()[0];
                        Class toImpl = method.getParameterTypes()[0];

                        Inheritance impl = InheritanceManager.getInheritance( toImpl, getQualifiers( parameterAnnotations ), getName( parameterAnnotations ), null );
                        Object[] args = { getInstance( impl ) };
                        
                        logger.info("@Inject on setter : {}", method.toString());
                        method.invoke(t, args);
                    }
            }
        }
        return t;
    }
    
    private <T> T injectFields(T t, List<Field> fields) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException, AmbiguousImplementationsException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchFieldException, NoSuchMethodException {
        for (Field field : fields) {
            Class<?> providedClass = null;
            Class<?> clazzToImpl = field.getType();

            if ( field.getType().isAssignableFrom(Provider.class) ) {
                Type type = field.getGenericType();
                ParameterizedType ptype = (ParameterizedType) type;
                providedClass = (Class<?>) ptype.getActualTypeArguments()[0];
            }

            Inheritance impl = InheritanceManager.getInheritance( clazzToImpl, getQualifiers( field.getDeclaredAnnotations() ), getName( field.getDeclaredAnnotations() ), providedClass );
            
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
