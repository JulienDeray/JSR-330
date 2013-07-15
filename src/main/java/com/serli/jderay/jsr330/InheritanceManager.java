/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class InheritanceManager {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceManager.class);
    private static final List<Inheritance> inheritances = new ArrayList<>();
    
    static void addInheritance(Class<?> clazz, Class<?> impl, Class<?>... qualifiers ) {
        inheritances.add( new Inheritance(clazz, impl, qualifiers));
        logger.info("Inheritance added : {} ---> {}", clazz.toString(), impl.toString());
    }
    
    static void addInheritance(Class<?> clazz, Class<?> impl, String name, Class<?>... qualifiers ) {
        inheritances.add( new Inheritance(clazz, impl, qualifiers, name) );
        logger.info("Inheritance added : {} ---> {}", clazz.toString(), impl.toString());
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

    static void setSingleton(Class<?> clazzToImpl, Class<?>[] qualifiers, String name) throws NoImplementationException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
        List<Class<?>> listQualifiers;
                
        if (qualifiers != null) 
            listQualifiers = new ArrayList<>(Arrays.asList(qualifiers));
        else
            listQualifiers = new ArrayList<>();
        
        getInheritance(clazzToImpl, listQualifiers, name).setSingleton();
    }
    
    static void reset() {
        inheritances.clear();
    }

    public static void setProvider(Class<?> clazzToImpl, String name, Class<?>[] qualifiers, Provider provider) throws NoImplementationException, AmbiguousImplementationsException {
        List<Class<?>> listQualifiers;

        if (qualifiers != null)
            listQualifiers = new ArrayList<>(Arrays.asList(qualifiers));
        else
            listQualifiers = new ArrayList<>();

        getInheritance(clazzToImpl, listQualifiers, name).setProvider(provider);
    }
}
