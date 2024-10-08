---
sidebar_position: 3
---

# Guice Dependency Injection.
Allow Gestalt to inject configuration directly into your classes using Guice using the `@InjectConfig` annotation on any class fields. This does not support constructor injection (due to Guice limitation)
To enable add the `new GestaltModule(gestalt)` to your Guice Modules, then pass in your instance of Gestalt.

See the [unit tests](https://github.com/gestalt-config/gestalt/blob/main/gestalt-guice/src/test/java/org/github/gestalt/config/guice/GuiceTest.java) for examples of use.
```java
Injector injector = Guice.createInjector(new GestaltModule(gestalt));

  MyService service = injector.getInstance(MyService.class);

// use the InjectConfig along with the path to inject configuration. 
public static class MyService {
  @InjectConfig(path = "db.user") DBConnection connection;

  public DBConnection getConnection() {
    return connection;
  }
}
`````    
