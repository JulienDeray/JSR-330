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
    private boolean needsProvider;

    public Inheritance(Class<?> clazz, Class<T> implementation) {
        this.toImpl = clazz;
        this.implementation = implementation;
        this.qualifiers = new ArrayList<>();
        this.isSingleton = false;
        this.singletonInstance = null;
        this.name = "";
        this.needsProvider = false;
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
        return clazzToImpl.equals(toImpl);
    }

    boolean isQualifieredBy(List<Class<?>> qualifiers) {
        if ( qualifiers.isEmpty() && this.qualifiers.isEmpty() )
            return true;
        else {
            Set<Object> set1 = new HashSet<>();
            set1.addAll(qualifiers);
            Set<Object> set2 = new HashSet<>();
            set2.addAll(this.qualifiers);
            
            return set1.equals(set2);
        }
    }

    boolean isNamedAs(String name) {
        return (name.equals(this.name) || name.equals(""));
    }
    
    Class<T> getImplementation() {
        return implementation;
    }

    void setSingleton() throws InstantiationException, IllegalAccessException, NoImplementationException, AmbiguousImplementationsException, FinalFieldException, NoSuchMethodException, MultipleConstructorsInjection, InvocationTargetException, NoSuchFieldException {
        this.isSingleton = true;
        DIContainer diContainer = new DIContainer();
        this.singletonInstance = diContainer.getInstance(implementation);
    }
    
    boolean isSingleton() {
        return this.isSingleton;
    }

    T getSingletonInstance() {
        return singletonInstance;
    }

    boolean hasProvider() {
        return getProvider() != null ;
    }

    @Override
    public String toString() {
        return implementation.getName();
    }

    public Provider getProvider() {
        return InheritanceManager.getProvider( this.toImpl );
    }

    public void setProvider() {
        this.needsProvider = true;
    }
}
