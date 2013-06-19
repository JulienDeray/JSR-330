/**
 * @author julien
 */
package com.serli.jderay.jsr330;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerConfig {

    private static Map<Class<?>, Class<?>> inheritances = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ContainerConfig.class);

    public void configure() {
    }

    public BindedClass bind(Class<?> clazz) {
        return new BindedClass<>(clazz);
    }

    static void addInheritance(Class<?> clazz, Class<?> clazzImpl) {
        logger.info("Inheritance added : {} ---> {}", clazz.toString(), clazzImpl.toString());
        inheritances.put(clazz, clazzImpl);
    }

    public class BindedClass<T> {

        Class<T> clazz;

        public BindedClass(Class<T> clazz) {
            this.clazz = clazz;
        }

        public <K extends T> void to(Class<K> clazzImpl) {
            ContainerConfig.addInheritance(clazz, clazzImpl);
        }
    }

    public Map<Class<?>, Class<?>> getInheritances() {
        return inheritances;
    }
}
