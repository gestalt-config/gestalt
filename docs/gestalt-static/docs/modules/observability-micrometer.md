---
sidebar_position: 2
---

# Micrometer Observability
Gestalt exposes several observations and provides a implementation for [micrometer](https://micrometer.io/).

To import the micrometer implementation add `gestalt-micrometer` to your build files.

In Maven:
```xml
<dependency>
  <groupId>com.github.gestalt-config</groupId>
  <artifactId>gestalt-micrometer</artifactId>
  <version>${version}</version>
</dependency>
```
Or in Gradle
```kotlin
implementation("com.github.gestalt-config:gestalt-micrometer:${version}")
```

Then when building gestalt, you need to register the module config `MicrometerModuleConfig` using the `MicrometerModuleConfigBuilder`.

```java
SimpleMeterRegistry registry = new SimpleMeterRegistry();

Gestalt gestalt = new GestaltBuilder()
    .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
    .setObservationsEnabled(true)
    .addModuleConfig(MicrometerModuleConfigBuilder.builder()
        .setMeterRegistry(registry)
        .setPrefix("myApp")
        .build())
    .build();

gestalt.loadConfigs();
```

There are several options to configure the micrometer module.

| Option          | Description                                                                                                                         | Default             |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------|---------------------|
| meterRegistry   | Provide the micrometer registry to submit observations.                                                                             | SimpleMeterRegistry |
| includePath     | When getting a config include the path in the observations tags. This can be a high cardinality observation so is not recommended.  | false               | 
| includeClass    | When getting a config include the class in the observations tags. This can be a high cardinality observation so is not recommended. | false               |
| includeOptional | When getting a config include if the configuration is optional or default as a true or false in the observation tags.               | false               |
| includeTags     | When getting a config include the tags in the request in the observations tags.                                                     | false               |
| prefix          | Add a prefix to the observations to better group your observations.                                                                 | gestalt             |

The following observations are exposed

| Observations       | Description                                                                                                       | Type     | tags                                                                                                          |
|--------------------|-------------------------------------------------------------------------------------------------------------------|----------|---------------------------------------------------------------------------------------------------------------|
| config.get         | Recorded when we request a configuration from gestalt that is not cached.                                         | Timer    | default:true if a default or optional value is returned. exception:exception class if there was an exception. |
| reload             | Recorded when a configuration is reloaded.                                                                        | Timer    | source:source name. exception:exception class if there was an exception.                                      | 
| get.config.missing | Incremented for each missing configuration, if decoding a class this can be more than one.                        | Counter  | optional: true or false depending if the optional value is optional or has a default.                         |
| get.config.error   | Incremented for each error while getting a configuration, if decoding a class this can be more than one.          | Counter  |                                                                                                               |
| get.config.warning | Incremented for warning error while getting a configuration, if decoding a class this can be more than one.       | Counter  |                                                                                                               | 
| cache.hit          | Incremented for each request served from the cache. A cache miss would be recorded in the observations config.get | Counter  |                                                                                                               |
