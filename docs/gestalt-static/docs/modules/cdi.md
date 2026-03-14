---
sidebar_position: 11
---

## CDI Module

Gestalt provides a CDI (Contexts and Dependency Injection) module that integrates with CDI containers to enable dependency injection of configuration values.

To use the CDI module, add `gestalt-cdi` to your build configuration.

The module supports:
- CDI producer for Gestalt instances
- Injection of configuration values using `@InjectConfig`
- Injection of configuration collections using `@InjectConfigs`
- CDI extension for automatic configuration setup

---

### Setting up CDI Integration

The CDI module provides a `GestaltConfigExtension` that automatically registers producers for configuration injection.

### Injecting Configuration Values

Use the `@InjectConfig` annotation to inject individual configuration values:

```java
@Inject
@InjectConfig("db.host")
private String dbHost;

@Inject
@InjectConfig("db.port")
private int dbPort;
```

### Injecting Configuration Collections

Use the `@InjectConfigs` annotation to inject collections of configuration:

```java
@Inject
@InjectConfigs("servers")
private List<ServerConfig> servers;
```

### Providing a Custom Gestalt Instance

You can provide a custom Gestalt instance using a producer:

```java
@Produces
public Gestalt createGestalt() {
    return new GestaltBuilder()
        .addSource(ClassPathConfigSourceBuilder.builder().setResource("/config.properties").build())
        .build();
}
```

The CDI extension will automatically detect and use the produced Gestalt instance for injections. If no producer is provided, it will create a default instance. 

### Configuration Classes with Prefix

You can also create configuration classes that automatically map to a prefix:

```java
@ConfigClassWithPrefix("database")
public class DatabaseConfig {
    @InjectConfig
    private String host;
    
    @InjectConfig
    private int port;
    
    // getters and setters
}
```