/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
    

public class DIContainer {

    private static final Logger logger = LoggerFactory.getLogger(DIContainer.class);
    private Map<Class<?>, Class<?>> inheritances;
    
    public static DIContainer createWith( ContainerConfig containerConfig ) {
        DIContainer container = new DIContainer();
        container.init( containerConfig );
        return container;
    }
    
    private void init(ContainerConfig containerConfig) {
        logger.info("--- Initialisationi of Dependencie Injection Container ---");
        containerConfig.configure();
        inheritances = containerConfig.getInheritances();
    }
    
    public <T> T getInstance( Class<T> clazz ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException {
        T t = clazz.newInstance();
        searchInjections( t );
        return t;
    }

    private <T> T searchInjections(T t) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException {
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
    
    private <T> T inject(T t, List<Field> fields) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoImplementationException {
        for (Field field : fields) {
            
            Class<?> clazzToImpl = field.getType();
            
            if ( inheritances.containsKey( clazzToImpl ) ) {
                Class<?> impl = inheritances.get( clazzToImpl );
                
                field.setAccessible(true);
                
                logger.info("@Inject : {} --> {}", clazzToImpl, impl);

                field.set(t, getInstance( impl ) );
                field.setAccessible(false);
            } else {
                throw new NoImplementationException( clazzToImpl.getCanonicalName() );
            }
        }

        return t;
    }
    
}
