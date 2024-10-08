---
sidebar_position: 2
---

# Overriding config values with Environment Variables

In a similar vein as overriding with command line variables, you can override with an Environment Variable.
There is two ways of doing this. You can use string substitution but an alternative is to use the `EnvironmentConfigSource`.


### String Substitution
In this example we provide a config source for default that uses string substitution to load an Env Var. It expects the Env Var to be an exact match, it does not translate it in any way. You can also provide a default that will be used if the Env Var is not found.

with the property values
```properties
# default
http.pool.maxTotal=${HTTP_POOL_MAXTOTAL:=1000}
```

Using an Environment Variable of: `HTTP_POOL_MAXTOTAL=200`
```java
  GestaltBuilder builder = new GestaltBuilder();
  Gestalt gestalt = builder
      .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.properties").build())
      .build();

  // Load the configurations, this will throw exceptions if there are any errors.
  gestalt.loadConfigs();

  GestaltConfigTest.HttpPool pool = gestalt.getConfig("http.pool", GestaltConfigTest.HttpPool.class);
  
  Assertions.assertEquals(200, pool.maxTotal);
```

In the end we should get the value 200 based on the Env Var. If we didnt provide the Env Var, it would default to 1000.

### Override using Environment Variables from a EnvironmentConfigSource

If you wish to use Env Vars to directly override values in your config you can use the `EnvironmentConfigSource` as the last source in Gestalt. This way it will have the highest priority and override all previous sources.

The Environment Variables are expected to be Screaming Snake Case, then the path is created from the key split up by the underscore "_".

So `HTTP_POOL_MAXTOTAL` becomes an equivalent path of http.pool.maxtotal

In this example we provide a config source for default and dev, but allow for the overriding those with the Env Var.

with the property values
```properties
# default
http.pool.maxTotal=100
# dev
http.pool.maxTotal=1000
```

However, we override with an Env Var of: `HTTP_POOL_MAXTOTAL=200`
```java
  // for this to work you need to set the following command line Options
  // -Dhttp.pool.maxTotal=200
  GestaltBuilder builder = new GestaltBuilder();
  Gestalt gestalt = builder
      .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.properties").build())
      .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
      .addSource(EnvironmentConfigSource.builder().build())
      .build();

  // Load the configurations, this will throw exceptions if there are any errors.
  gestalt.loadConfigs();

  GestaltConfigTest.HttpPool pool = gestalt.getConfig("http.pool", GestaltConfigTest.HttpPool.class);
  
  Assertions.assertEquals(200, pool.maxTotal);
```

In the end we should get the value 200 based on the overridden Environment Variable.

If you wish to use a different case then Screaming Snake Case, you would need to provide your own EnvironmentVarsLoader with your specific SentenceLexer lexer.

There are several configuration options on the `EnvironmentConfigSource`,

| Configuration Name | Default | Description                                                                                                                                   |
|--------------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| failOnErrors       | false   | If we should fail on errors. By default the Environment Config Source pulls in all Environment variables, and several may not parse correctly |
| prefix             | ""      | By provide a prefix only Env Vars that start with the prefix will be included.                                                                |
| ignoreCaseOnPrefix | false   | Define if we want to ignore the case when matching the prefix.                                                                                |
| removePrefix       | false   | If we should remove the prefix and the following "_" or"." from the imported configuration                                                    |

