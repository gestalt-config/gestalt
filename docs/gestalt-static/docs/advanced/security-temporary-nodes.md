---
sidebar_position: 5
---

# Security - Temporary Nodes
## Temporary Value with Access Restrictions
One layer of security used by Gestalt is to restrict the number of times a value can be read before it is released, GC'ed and no longer accessible in memory.

The Temporary Value feature allows us to specify the secret using a regex and the number of times it is accessible.
Once the leaf value has been read the accessCount times, it will release the secret value of the node by setting it to null.
Eventually the secret node should be garbage collected. However, while waiting for GC it may still be found in memory.
These values will not be cached in the Gestalt Cache and should not be cached by the caller. Since they are not cached there a performance cost since each request has to be looked up.

To protect values you can either annotate a configuration with `@{temp:int}` or use the `addTemporaryNodeAccessCount` methods in the `GestaltBuilder`, register a `TemporarySecretModule` by using the `TemporarySecretModuleBuilder`.

Using the annotation `@{temp:int}`: 

```java
Map<String, String> configs = new HashMap<>();
configs.put("my.password", "abcdef@{temp:1}");

GestaltBuilder builder = new GestaltBuilder();
Gestalt gestalt = builder
  .addSource(MapConfigSourceBuilder.builder()
    .setCustomConfig(configs)
    .build())
  .build();

gestalt.loadConfigs();

// the first call will get the node and reduce the access cound for the node to 0.
Assertions.assertEquals("abcdef", gestalt.getConfig("my.password", String.class));

// The second time we get the node the value has been released and will have no result.
Assertions.assertTrue(gestalt.getConfigOptional("some.value", String.class).isEmpty());
```

Or using the method `addTemporaryNodeAccessCount` in the gestalt builder:

```java
Map<String, String> configs = new HashMap<>();
configs.put("my.password", "abcdef");

GestaltBuilder builder = new GestaltBuilder();
Gestalt gestalt = builder
  .addSource(MapConfigSourceBuilder.builder()
    .setCustomConfig(configs)
    .build())
  .addTemporaryNodeAccessCount("password", 1)
  .build();

gestalt.loadConfigs();

// the first call will get the node and reduce the access cound for the node to 0.
Assertions.assertEquals("abcdef", gestalt.getConfig("my.password", String.class));

// The second time we get the node the value has been released and will have no result.
Assertions.assertTrue(gestalt.getConfigOptional("some.value", String.class).isEmpty());
```

Or using the `TemporarySecretModule`

```java
TemporarySecretModuleBuilder builder = TemporarySecretModuleBuilder.builder().addSecretWithCount("secret", 1);

GestaltBuilder builder = new GestaltBuilder();
builder.addModuleConfig(builder.build());
```
