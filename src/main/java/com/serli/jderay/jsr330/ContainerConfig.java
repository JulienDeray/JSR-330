/**
 * @author julien
 */
package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.DoesNotImplementException;
import com.serli.jderay.jsr330.exceptions.NotAnInterfaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ContainerConfig.class);

    public void configure() throws DoesNotImplementException, NotAnInterfaceException {
    }

    public BindedClass bind(Class<?> clazz) throws NotAnInterfaceException {
        if ( clazz.isInterface() )
            return new BindedClass(clazz);
        else
            throw new NotAnInterfaceException( clazz.getName() );
    }

    public static class QualifieredClass {

        Class<?> clazzToImpl;
        Class<?>[] qualifiers;

        public QualifieredClass(Class<?> clazzToImpl, Class<?>[] qualifiers) {
            this.clazzToImpl = clazzToImpl;
            this.qualifiers = qualifiers;
        }
        
        public void to(Class<?> clazzImpl) {
            InheritanceManager.addInheritance(clazzToImpl, clazzImpl, qualifiers);
        }
    }

    public class BindedClass {

        Class<?> clazzToImpl;

        public BindedClass(Class<?> clazz) {
            this.clazzToImpl = clazz;
        }

        public void to(Class<?> implementation) throws DoesNotImplementException {
            if ( isAimplementation( implementation ) )
                InheritanceManager.addInheritance(clazzToImpl, implementation);
        }

        public QualifieredClass annotatedWith(Class<?>... qualifiers) {
            return new QualifieredClass( clazzToImpl, qualifiers );
        }

        private boolean isAimplementation(Class<?> implementation) throws DoesNotImplementException {
            Class<?>[] interfaces = implementation.getInterfaces();
            
            for (int i = 0; i < interfaces.length; i++) {
                if ( interfaces[i].equals( clazzToImpl ) )
                    return true;
            }
            throw new DoesNotImplementException( clazzToImpl.getName(), implementation.getName() );
        }
    }
}
