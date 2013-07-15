/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.DoesNotImplementException;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import com.serli.jderay.jsr330.exceptions.NotAnInterfaceException;
import org.atinject.tck.auto.*;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

import javax.inject.Inject;
import javax.inject.Provider;


public class TckConfig extends ContainerConfig {

    @Override
    public void configure() throws NotAnInterfaceException, DoesNotImplementException, NoImplementationException, IllegalAccessException, AmbiguousImplementationsException, InstantiationException, NoSuchMethodException {
        
        bind(Seat.class).annotatedWith(Drivers.class).to(DriversSeat.class);
        
        bind(Engine.class).to(V8Engine.class);

        bind(Tire.class).named("spare").providedBy(new Provider<Tire>() {
            @Inject
            private SpareTire tire;

            public Tire get() {
                return (Tire) tire;
            }
        });
        
        bind(Car.class).to(Convertible.class);

        bind(Tire.class).to(Tire.class);

        bind(SpareTire.class).to(SpareTire.class);

        bind(FuelTank.class).to(FuelTank.class);

        bind(Seat.class).to(Seat.class);

        bind(Cupholder.class).to(Cupholder.class);
    }
}
