---
sidebar_position: 1
---

# Overriding config values with command line arguments

Often you may wish to override a configuration value with a value provided on the command line.
One way to do this is to add a `SystemPropertiesConfigSource` as the last source in Gestalt. This way it will have the highest priority and override all previous sources.

Then when running the project you provide the command line parameter `-D<path.to.config=value>`. This will override all other config sources with this value.

In this example we provide a config source for default and dev, but allow for the overriding those with the system properties.

with the property values
```properties
# default
http.pool.maxTotal=100
# dev
http.pool.maxTotal=1000
```

However, we override with a command line parameter of: `-Dhttp.pool.maxTotal=200`
```java
  // for this to work you need to set the following command line Options
  // -Dhttp.pool.maxTotal=200
  GestaltBuilder builder = new GestaltBuilder();
  Gestalt gestalt = builder
      .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.properties").build())
      .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
      .addSource(SystemPropertiesConfigSourceBuilder.builder().build())
      .build();

  // Load the configurations, this will throw exceptions if there are any errors.
  gestalt.loadConfigs();

  GestaltConfigTest.HttpPool pool = gestalt.getConfig("http.pool", GestaltConfigTest.HttpPool.class);
  
  Assertions.assertEquals(200, pool.maxTotal);
```

In the end we should get the value 200 based on the overridden command line parameter.
