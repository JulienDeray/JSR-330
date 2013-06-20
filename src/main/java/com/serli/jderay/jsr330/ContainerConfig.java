/**
 * @author julien
 */
package com.serli.jderay.jsr330;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ContainerConfig.class);

    public void configure() {
    }

    public BindedClass bind(Class<?> clazz) {
        return new BindedClass<>(clazz);
    }

    public static class QualifieredClass<T> {

        Class<?> clazzToImpl;
        Class<?>[] qualifiers;

        public QualifieredClass(Class<?> clazzToImpl, Class<?>[] qualifiers) {
            this.clazzToImpl = clazzToImpl;
            this.qualifiers = qualifiers;
        }
        
        public <K extends T> void to(Class<K> clazzImpl) {
            InheritanceManager.addInheritance(clazzToImpl, clazzImpl, qualifiers);
        }
    }

    public class BindedClass<T> {

        Class<T> clazzToImpl;

        public BindedClass(Class<T> clazz) {
            this.clazzToImpl = clazz;
        }

        public <K extends T> void to(Class<K> implementation) {
            InheritanceManager.addInheritance(clazzToImpl, implementation);
        }

        public QualifieredClass annotatedWith(Class<?>... qualifiers) {
            return new QualifieredClass<>( clazzToImpl, qualifiers );
        }
    }
}
