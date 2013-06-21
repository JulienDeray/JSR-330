/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InheritanceManager {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceManager.class);
    private static List<Inheritance> inheritances = new ArrayList<>();
    
    static void addInheritance(Class<?> clazz, Class<?> impl ) {
        inheritances.add( new Inheritance(clazz, impl) );
        logger.info("Inheritance added : {} ---> {}", clazz.toString(), impl.toString());
    }
    
    static void addInheritance(Class<?> clazz, Class<?> impl, Class<?>... qualifiers ) {
        inheritances.add( new Inheritance(clazz, impl, qualifiers));
        logger.info("Inheritance added (with annotations) : {} ---> {}", clazz.toString(), impl.toString());
    }
    
    static void addInheritance(Class<?> clazz, Class<?> impl, String name, Class<?>... qualifiers ) {
        inheritances.add( new Inheritance(clazz, impl, qualifiers, name) );
        logger.info("Inheritance added (named \"" + name + "\") : {} ---> {}", clazz.toString(), impl.toString());
    }
    
    static boolean contains(Class<?> clazzToImpl) {
        for (Inheritance inheritance : inheritances) {
            if ( inheritance.is( clazzToImpl ) )
                return true;
        }
        return false;
    }

    static Inheritance getInheritance(Class<?> clazzToImpl, List<Class<?>> qualifiers, String name) throws NoImplementationException, AmbiguousImplementationsException {
        Inheritance res = null;
        
        for ( Inheritance inheritance : inheritances ) {
            if ( inheritance.is(clazzToImpl) && inheritance.isQualifieredBy( qualifiers ) && inheritance.isNamedAs( name ) )
                if ( res == null ) 
                    res = inheritance;
                else
                    throw new AmbiguousImplementationsException( res.toString(), inheritance.toString(), clazzToImpl.getName() );
        }
        if ( res != null )
            return res;
        else
            throw new NoImplementationException( clazzToImpl.getCanonicalName() );
    }

    static <T> void setSingleton(Class<?> clazzToImpl, Class<?>[] qualifiers, String name) throws NoImplementationException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
        List<Class<?>> listQualifiers;
                
        if (qualifiers != null) 
            listQualifiers = new ArrayList<>(Arrays.asList(qualifiers));
        else
            listQualifiers = new ArrayList<>();
        
        getInheritance(clazzToImpl, listQualifiers, name).setSingleton();
    }

    static <T> boolean isSingleton(Class<T> clazz) {
        for ( Inheritance inheritance : inheritances ) {
            if ( inheritance.is(clazz) )
                if ( inheritance.isSingleton() )
                    return true;
        }
       return false;
    }
}
