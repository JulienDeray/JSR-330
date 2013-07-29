/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.FinalFieldException;
import com.serli.jderay.jsr330.exceptions.MultipleConstructorsInjection;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;

import javax.inject.Provider;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

class Inheritance<T> {
    
    private Class<?> toImpl;
    private Class<T> implementation;
    private List<Class<?>> qualifiers;
    private boolean isSingleton;
    private T singletonInstance;
    private String name;
    private Provider provider;

    public Inheritance(Class<?> clazz, Class<T> implementation) {
        this.toImpl = clazz;
        this.implementation = implementation;
        this.qualifiers = new ArrayList<>();
        this.isSingleton = false;
        this.singletonInstance = null;
        this.name = "";
        this.provider = null;
    }
    
    public Inheritance(Class<?> clazz, Class<T> implementation, Class<?>[] qualifiers) {
        this(clazz, implementation);
        this.qualifiers = new ArrayList<>(Arrays.asList(qualifiers));
    }

    public Inheritance(Class<?> clazz, Class<T> implementation, Class<?>[] qualifiers, String name) {
        this(clazz, implementation, qualifiers);
        this.name = name;
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

    boolean isNamedAs(String name) {
        return (name.equals(this.name) || name.equals("")) ? true : false;
    }
    
    Class<T> getImplementation() {
        return implementation;
    }

    void setSingleton() throws InstantiationException, IllegalAccessException, NoImplementationException, AmbiguousImplementationsException, FinalFieldException, NoSuchMethodException, MultipleConstructorsInjection, InvocationTargetException, NoSuchFieldException {
        this.isSingleton = true;
        DIContainer diContainer = new DIContainer();
        this.singletonInstance = diContainer.getInstance( implementation );
    }
    
    boolean isSingleton() {
        return this.isSingleton;
    }

    T getSingletonInstance() {
        return singletonInstance;
    }

    boolean hasProvider() {
        return this.provider == null ? false : true;
    }

    @Override
    public String toString() {
        return implementation.getName();
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Provider getProvider() {
        return provider;
    }
}
