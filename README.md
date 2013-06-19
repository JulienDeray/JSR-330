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
            public void configure() {
                bind(Name2Service.class).to(Name2ServiceImpl.class);
            }
        };
        DIContainer container = DIContainer.createWith(config);
                
        ModuleTest1 mt1 = container.getInstance(ModuleTest1.class);
        mt1.shootM2();
    }
    
    public ModuleTest1() {
    }
    
    @Inject private Name2Service name2service;
    public void shootM2() {
        System.out.println( name2service.give2Name() );
    }

}
```
