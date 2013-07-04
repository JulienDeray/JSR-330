/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.DoesNotImplementException;
import com.serli.jderay.jsr330.exceptions.FinalFieldException;
import com.serli.jderay.jsr330.exceptions.IsNotScopeException;
import com.serli.jderay.jsr330.exceptions.MultipleConstructorsInjection;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import com.serli.jderay.jsr330.exceptions.NotAnInterfaceException;
import java.lang.reflect.InvocationTargetException;
import junit.framework.Test;
import junit.framework.TestCase;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;

/**
 *
 * @author julien
 */
public class TckTest extends TestCase {
        
    public static Test suite() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException {
        final DIContainer container = DIContainer.createWith( new TckConfig() );
        
        Car car = container.getInstance(Car.class);

        return Tck.testsFor(car, false, false);
    }
}