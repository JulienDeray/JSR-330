/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InheritanceManager {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceManager.class);
    private static List<Inheritance> inheritances = new ArrayList<>();
    
    static void addInheritance(Class<?> clazz, Class<?> impl, Class<?>... annotations ) {
        inheritances.add( new Inheritance(clazz, impl, annotations));
        logger.info("Inheritance added (with annotations) : {} ---> {}", clazz.toString(), impl.toString());
    }
    
    static void addInheritance(Class<?> clazz, Class<?> impl ) {
        inheritances.add( new Inheritance(clazz, impl) );
        logger.info("Inheritance added : {} ---> {}", clazz.toString(), impl.toString());
    }

    static boolean contains(Class<?> clazzToImpl) {
        for (Inheritance inheritance : inheritances) {
            if ( inheritance.is( clazzToImpl ) )
                return true;
        }
        return false;
    }

    static Class<?> get(Class<?> clazzToImpl, List<Class<?>> qualifiers) throws NoImplementationException {
        for ( Inheritance inheritance : inheritances ) {
            if ( inheritance.is(clazzToImpl) )
                if ( inheritance.isQualifieredBy( qualifiers ) )
                    return inheritance.getImplementation();
        }
        throw new NoImplementationException( clazzToImpl.getCanonicalName() );
    }
}
