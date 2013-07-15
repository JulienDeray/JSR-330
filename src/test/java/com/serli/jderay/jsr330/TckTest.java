/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.*;
import junit.framework.Test;
import junit.framework.TestCase;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author julien
 */
public class TckTest extends TestCase {
        
    public static Test suite() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException, NoSuchMethodException, NoSuchFieldException {
        final DIContainer container = DIContainer.createWith( new TckConfig() );

        // TODO : trouver le moyen de passer une interface !!! (Car -> interface)
        Car car = container.getInstance(Car.class);

        return Tck.testsFor(car, false, false);
    }
}