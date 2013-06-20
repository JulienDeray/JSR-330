/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Inheritance {
    
    private Class<?> toImpl;
    private Class<?> implementation;
    private List<Class<?>> qualifiers;

    public Inheritance(Class<?> clazz, Class<?> implementation) {
        this.toImpl = clazz;
        this.implementation = implementation;
        this.qualifiers = new ArrayList<>();
    }
    
    public Inheritance(Class<?> clazz, Class<?> implementation, Class<?>[] annotations) {
        this(clazz, implementation);
        this.qualifiers = new ArrayList<>(Arrays.asList(annotations));
    }

    boolean is(Class<?> clazzToImpl) {
        return clazzToImpl.equals(toImpl) ? true : false;
    }

    boolean isQualifieredBy(List<Class<?>> qualifiers) {
        if ( qualifiers.isEmpty() && this.qualifiers.isEmpty() )
            return true;
        else {
            Set<Object> set1 = new HashSet<>();
            set1.addAll(qualifiers);
            Set<Object> set2 = new HashSet<>();
            set2.addAll(this.qualifiers);
            
            return set1.equals(set2) ? true : false;
        }
    }

    public Class<?> getImplementation() {
        return implementation;
    }
}
