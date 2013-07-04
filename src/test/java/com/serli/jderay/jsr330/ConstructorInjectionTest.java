/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serli.jderay.jsr330;

import com.serli.jderay.jsr330.exceptions.AmbiguousImplementationsException;
import com.serli.jderay.jsr330.exceptions.DoesNotImplementException;
import com.serli.jderay.jsr330.exceptions.IsNotScopeException;
import com.serli.jderay.jsr330.exceptions.MultipleConstructorsInjection;
import com.serli.jderay.jsr330.exceptions.NoImplementationException;
import com.serli.jderay.jsr330.exceptions.NotAnInterfaceException;
import com.serli.jderay.jsr330.exceptions.FinalFieldException;
import com.serli.jderay.moduletest1.constructor.MultiQualifiedInject;
import com.serli.jderay.moduletest1.constructor.NamedInject;
import com.serli.jderay.moduletest1.constructor.QualifiedInject;
import com.serli.jderay.moduletest1.constructor.SimpleInject;
import com.serli.jderay.moduletest1.constructor.SingletonInject;
import com.serli.jderay.moduletest1.annotations.LeJoliSingleton;
import com.serli.jderay.moduletest1.annotations.Touch;
import com.serli.jderay.moduletest1.annotations.Touch1;
import com.serli.jderay.moduletest1.annotations.Touch2;
import com.serli.jderay.moduletest1.constructor.FinalInject;
import com.serli.jderay.moduletest1.constructor.StaticInject;
import com.serli.jderay.moduletest2.Name2Service;
import com.serli.jderay.moduletest2.impl.Name2ServiceImpl;
import com.serli.jderay.moduletest2.impl.NameMultiTouchServiceImpl;
import com.serli.jderay.moduletest2.impl.NameTouchServiceImpl;
import com.serli.jderay.moduletest2.impl.NamedServiceImpl;
import com.serli.jderay.moduletest2.impl.SingleServiceImpl;
import com.serli.jderay.moduletest2.impl.FinalServiceImpl;
import com.serli.jderay.moduletest2.impl.StaticServiceImpl;
import java.lang.reflect.InvocationTargetException;
import javax.inject.Singleton;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author julien
 */
public class ConstructorInjectionTest extends TestCase {
    
    public ConstructorInjectionTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite( ConstructorInjectionTest.class );
    }

    @Override
    protected void tearDown() throws Exception {
        DIContainer.reset();
    }

    public void testSimpleInjection() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException {
        ContainerConfig config = new ContainerConfig() {
            @Override
            public void configure() throws NotAnInterfaceException, DoesNotImplementException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
                bind(Name2Service.class).to(Name2ServiceImpl.class);
            }
        };
        DIContainer container = DIContainer.createWith(config);
        SimpleInject mt1 = container.getInstance(SimpleInject.class);
        
        assertEquals("Module 2 -> yes it works !", mt1.shootM2());
    }
    
    public void testStaticInjection() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException {
        ContainerConfig config = new ContainerConfig() {
            @Override
            public void configure() throws NotAnInterfaceException, DoesNotImplementException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
                bind(Name2Service.class).to(StaticServiceImpl.class);
            }
        };
        DIContainer container = DIContainer.createWith(config);
        StaticInject mt1 = container.getInstance(StaticInject.class);
        
        assertEquals("Module 2 [Static] -> Yes it works !", mt1.shootStatic());
    }
    
    public void testQualifedInjection() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException {
        ContainerConfig config = new ContainerConfig() {
            @Override
            public void configure() throws NotAnInterfaceException, DoesNotImplementException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
                bind(Name2Service.class).annotatedWith(Touch.class).to(NameTouchServiceImpl.class);
            }
        };
        DIContainer container = DIContainer.createWith(config);
        QualifiedInject mt1 = container.getInstance(QualifiedInject.class);
        
        assertEquals("Module 2 [Touchy Mod] -> yes it works !", mt1.shootMTouch());
    }
    
    public void testDoubleQualifedInjection() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException {
        ContainerConfig config = new ContainerConfig() {
            @Override
            public void configure() throws NotAnInterfaceException, DoesNotImplementException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
                bind(Name2Service.class).annotatedWith(Touch1.class, Touch2.class).to(NameMultiTouchServiceImpl.class);
            }
        };
        DIContainer container = DIContainer.createWith(config);
        MultiQualifiedInject mt1 = container.getInstance(MultiQualifiedInject.class);
        
        assertEquals("Module 2 [Multi Touchy Mod] -> yes it works !", mt1.shootMultiTouch());
    }
    
    public void testSingletonInjection() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException {
        ContainerConfig config = new ContainerConfig() {
            @Override
            public void configure() throws NotAnInterfaceException, DoesNotImplementException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
                bind(Name2Service.class).annotatedWith(LeJoliSingleton.class).to(SingleServiceImpl.class).withScope(Singleton.class);
            }
        };
        DIContainer container = DIContainer.createWith(config);
        SingletonInject mt1 = container.getInstance(SingletonInject.class);
        
        assertEquals("Module 2 [Singleton] -> yes it works ! [0]Module 2 [Singleton] -> yes it works ! [1]", mt1.shootSingleton());
    }
    
    public void testNamedInjection() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException {
        ContainerConfig config = new ContainerConfig() {
            @Override
            public void configure() throws NotAnInterfaceException, DoesNotImplementException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
                bind(Name2Service.class).named("CocoLasticot").to(NamedServiceImpl.class);
            }
        };
        DIContainer container = DIContainer.createWith(config);
        NamedInject mt1 = container.getInstance(NamedInject.class);
        
        assertEquals("Module 2 [Named : \"CocoLasticot\"] -> yes it works !", mt1.shootCocoLasticot());
    }
    
    public void testFinalException() throws DoesNotImplementException, NotAnInterfaceException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException, IllegalArgumentException, InvocationTargetException, MultipleConstructorsInjection, FinalFieldException {
        ContainerConfig config = new ContainerConfig() {
            @Override
            public void configure() throws NotAnInterfaceException, DoesNotImplementException, NoImplementationException, IsNotScopeException, InstantiationException, IllegalAccessException, AmbiguousImplementationsException {
                bind(Name2Service.class).to(FinalServiceImpl.class);
            }
        };
        DIContainer container = DIContainer.createWith(config);
        
        try {
            FinalInject mt1 = container.getInstance(FinalInject.class);
            fail();
        }
        catch(FinalFieldException e) {
            assertEquals(new FinalFieldException().getMessage(), e.getMessage());
        }
    }
}
