/**
 * @author julien
 */

package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.*;
import org.atinject.tck.auto.*;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;


public class TckConfig extends ContainerConfig {

    @Override
    public void configure() throws NotAnInterfaceException, DoesNotImplementException, NoImplementationException, IllegalAccessException, AmbiguousImplementationsException, InstantiationException, NoSuchMethodException, IsNotScopeException, InvocationTargetException, MultipleConstructorsInjection, NoSuchFieldException, FinalFieldException {
        
        bind(Seat.class).annotatedWith(Drivers.class).to(DriversSeat.class);
        
        bind(Engine.class).to(V8Engine.class);

        bind(Tire.class).named("spare").providedBy(new Provider<Tire>() {
            @Inject
            private SpareTire tire;

            @Override
            public Tire get() {
                return tire;
            }
        });

        bind(Seat.class).providedBy(new Provider<Seat>() {
            @Inject
            private Seat seat;

            @Override
            public Seat get() {
                return seat;
            }
        });
        
        bind(Car.class).to(Convertible.class);

        bind(Tire.class).to(Tire.class);

        bind(SpareTire.class).to(SpareTire.class);

        bind(FuelTank.class).to(FuelTank.class);

        bind(Seat.class).to(Seat.class).withScope(Singleton.class);
        //bind(Seat.class).to(Seat.class);

        bind(Cupholder.class).to(Cupholder.class).withScope(Singleton.class);
        //bind(Cupholder.class).to(Cupholder.class);
    }
}
