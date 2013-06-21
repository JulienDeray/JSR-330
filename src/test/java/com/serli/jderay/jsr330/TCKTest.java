/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.DoesNotImplementException;
import com.serli.jderay.jsr330.exceptions.IsNotScopeException;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import com.serli.jderay.jsr330.exceptions.NotAnInterfaceException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;



/**
 *
 * @author julien
 */


public class TCKTest extends TestSuite {


    public static Test suite() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {


        final DIContainer container = DIContainer.createWith( new TCKConfig() );


        Car car = container.getInstance(Car.class);


        return Tck.testsFor(car, false, false);


    }
}



