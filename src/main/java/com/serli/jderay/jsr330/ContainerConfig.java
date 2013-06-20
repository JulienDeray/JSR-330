/**
 * @author julien
 */
package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.DoesNotImplementException;
import com.serli.jderay.jsr330.exceptions.IsNotScopeException;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import com.serli.jderay.jsr330.exceptions.NotAnInterfaceException;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ContainerConfig.class);

    public void configure() throws AmbiguousImplementationsException, DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException  {
    }

    public BindedClass bind(Class<?> clazz) throws NotAnInterfaceException {
        if ( clazz.isInterface() )
            return new BindedClass(clazz);
        else
            throw new NotAnInterfaceException( clazz.getName() );
    }

    public class QualifieredClass {

        Class<?> clazzToImpl;
        Class<?>[] qualifiers;

        public QualifieredClass(Class<?> clazzToImpl, Class<?>[] qualifiers) {
            this.clazzToImpl = clazzToImpl;
            this.qualifiers = qualifiers;
        }
        
        public AfterTo to(Class<?> clazzImpl) {
            return new AfterTo( clazzToImpl, qualifiers, clazzImpl );
        }
    }

    public class AfterTo {

        Class<?> clazzToImpl;
        Class<?>[] qualifiers;
        Class<?> clazzImpl;

        public AfterTo(Class<?> clazzToImpl, Class<?> clazzImpl) {
            this.clazzToImpl = clazzToImpl;
            this.clazzImpl = clazzImpl;
            
            InheritanceManager.addInheritance(clazzToImpl, clazzImpl);
        }
        
        public AfterTo(Class<?> clazzToImpl, Class<?>[] qualifiers, Class<?> clazzImpl) {
            this.clazzToImpl = clazzToImpl;
            this.qualifiers = qualifiers;
            this.clazzImpl = clazzImpl;
            
            InheritanceManager.addInheritance(clazzToImpl, clazzImpl, qualifiers);
        }
        
        public void withScope( Class<?> singleton ) throws IsNotScopeException, InstantiationException, IllegalAccessException, NoImplementationException, AmbiguousImplementationsException {
            if ( singleton.equals( Singleton.class ) )
                InheritanceManager.setSingleton( clazzToImpl, qualifiers );
            else
                throw new IsNotScopeException( singleton.getName() );
        }
    }

    public class BindedClass {

        Class<?> clazzToImpl;

        public BindedClass(Class<?> clazz) {
            this.clazzToImpl = clazz;
        }

        public AfterTo to(Class<?> implementation) throws DoesNotImplementException, NoImplementationException {
            if ( isAimplementation( implementation ) )
                return new AfterTo( clazzToImpl, implementation );
            else 
                throw new NoImplementationException( implementation.getName() );
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
