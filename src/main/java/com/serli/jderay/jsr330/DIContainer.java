/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.DoesNotImplementException;
import com.serli.jderay.jsr330.exceptions.IsNotScopeException;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import com.serli.jderay.jsr330.exceptions.NotAnInterfaceException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
    
    public <T> T getInstance( Class<T> clazz ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException, AmbiguousImplementationsException {
        T t = clazz.newInstance();
        searchInjections( t );
        return t;
    }

    private <T> T getInstance( Inheritance<T> impl ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException, AmbiguousImplementationsException {
        T t = impl.isSingleton() ? impl.getSingletonInstance() : impl.getImplementation().newInstance();
        searchInjections( t );
        return t;
    }

    private <T> T searchInjections(T t) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException, AmbiguousImplementationsException {
        List<Field> fields = new ArrayList<>();

        for (Field field : t.getClass().getDeclaredFields()) {
            Annotation[] annotations = field.getDeclaredAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation instanceof Inject) {
                    fields.add(field);
                }
            }
        }
        return inject(t, fields);
    }
    
    private <T> T inject(T t, List<Field> fields) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException, AmbiguousImplementationsException {
        for (Field field : fields) {
            
            Class<?> clazzToImpl = field.getType();
            Inheritance impl = InheritanceManager.getInheritance( clazzToImpl, getQualifiers( field ), getName( field ) );
            
            field.setAccessible(true);
           
            field.set(t, getInstance( impl ) );
            logger.info("@Inject : {} --> {}", clazzToImpl, impl);

            field.setAccessible(false);
        }

        return t;
    }
    
    private List<Class<?>> getQualifiers( Field field ) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        List<Class<?>> qualifiers = new ArrayList<>();

        for (Annotation annotation : annotations) {
            if ( !(annotation instanceof Inject) ) {
                qualifiers.add( annotation.annotationType() );
            }
        }
        return qualifiers;
    }
    
    private String getName( Field field ) {
        try {
            return field.getAnnotation(Named.class).value();
        }
        catch( NullPointerException e ) {
            return "";
        }
    }
    
}
