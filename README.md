JSR-330
=======

Implementation of Java JSR-330 (actually partially implemented)


Exemple :
---------

```java
public class ModuleTest1 {
    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoImplementationException {

        ContainerConfig config = new ContainerConfig() {
            @Override
            public void configure() throws NotAnInterfaceException, DoesNotImplementException {
                bind(Name2Service.class).to(Name2ServiceImpl.class);
                bind(Name2Service.class).annotatedWith(Touch.class).to(NameTouchServiceImpl.class);
                bind(Name2Service.class).annotatedWith(Touch1.class, Touch2.class).to(NameMultiTouchServiceImpl.class);
            }
        };
        DIContainer container = DIContainer.createWith(config);
                
        ModuleTest1 mt1 = container.getInstance(ModuleTest1.class);
        mt1.shootM2();
        mt1.shootMTouch();
        mt1.shootMultiTouch();
    }
    
        
    public ModuleTest1() {
    }
    
    @Inject private Name2Service name2service;
    public void shootM2() {
        System.out.println( name2service.give2Name() );
    }
    
    @Inject @Touch private Name2Service nameTouchService;
    public void shootMTouch() {
        System.out.println( nameTouchService.give2Name() );
    }

    @Inject @Touch1 @Touch2 private Name2Service nameMultiTouchService;
    public void shootMultiTouch() {
        System.out.println( nameMultiTouchService.give2Name() );
    }

}
```
