---
sidebar_position: 4
---

# Dynamically updating config values
Typically, when you get a configuration from Gestalt, you maintain a reference to the value in your class. You typically dont want to call Gestalt each time you want to check the value of the configuration. Although Gestalt has a cache, there is overhead in calling Gestalt each time.
However, when you cache locally if the configuration in Gestalt change via a reload, you will still have a reference to the old value.

So, instead of getting your specific configuration you could request a ConfigContainer, or a proxy decoder (by providing an interface).
```java
var myConfigValue = gestalt.getConfig("some.value", new TypeCapture<ConfigContainer<String>>() {});
```
The ConfigContainer will hold your configuration value with several options to get it.
```java
var myValue = configContainer.orElseThrow();
var myOptionalValue = configContainer.getOptional();
```

Then, when there is a reload, the ConfigContainer or proxy decoder will get and cache the new configuration. Ensuring you always have the most recent value.

The following example shows a simple use case for ConfigContainer.
```java
Map<String, String> configs = new HashMap<>();
configs.put("some.value", "value1");

var manualReload = new ManualConfigReloadStrategy();

GestaltBuilder builder = new GestaltBuilder();
Gestalt gestalt = builder
  .addSource(MapConfigSourceBuilder.builder()
    .setCustomConfig(configs)
    .addConfigReloadStrategy(manualReload)
    .build())
  .build();

gestalt.loadConfigs();

var configContainer = gestalt.getConfig("some.value", new TypeCapture<ConfigContainer<String>>() {});

Assertions.assertEquals("value1", configContainer.orElseThrow());

// Change the values in the config map
configs.put("some.value", "value2");

// let gestalt know the values have changed so it can update the config tree. 
manualReload.reload();

// The config container is automatically updated. 
Assertions.assertEquals("value2", configContainer.orElseThrow());
```
