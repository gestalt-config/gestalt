---
sidebar_position: 4
---

# Config Loader
Each config loader understands how to load a specific type of config. Often this is associated with a specific ConfigSource. For example the EnvironmentVarsLoader only loads the EnvironmentConfigSource. However, some loaders expect a format of the config, but accept it from multiple sources. For example the PropertyLoader expects the typical java property file, but it can come from any source as long as it is an input stream. It may be the system properties, local file, github, or S3.

| Config Loader         | Formats supported                       | details                                                                                                                                                                                                                                                                                                                                                                                                                                             | module                                                             |
|-----------------------|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| EnvironmentVarsLoader | envVars                                 | Loads Environment Variables from the EnvironmentConfigSource, it expects a list not a InputStream. By default, it splits the paths using a "_". You can also disable failOnErrors if you are receiving errors from the environment variables, as you can not always control what is present. By treating Errors as warnings it will not fail if it finds a configuration the parser doesn't understand. Instead it will ignore the specific config. | core                                                               | 
| MapConfigLoader       | mapConfig                               | Loads a user provided Map from the MapConfigSource, it expects a list not a InputStream. By default, it splits the paths using a "." and tokenizes arrays with a numeric index as "[0]".                                                                                                                                                                                                                                                            | core                                                               | 
| PropertyLoader        | properties, props, and systemProperties | Loads a standard property file from an InputStream. By default, it splits the paths using a "." and tokenizes arrays with a numeric index as "[0]".                                                                                                                                                                                                                                                                                                 | core                                                               |
| JsonLoader            | json                                    | Leverages Jackson to load json files and convert them into a ConfigNode tree.                                                                                                                                                                                                                                                                                                                                                                       | [`gestalt-json`](https://search.maven.org/search?q=gestalt-json)   |
| TomlLoader            | toml                                    | Leverages Jackson to load toml files and convert them into a ConfigNode tree.                                                                                                                                                                                                                                                                                                                                                                       | [`gestalt-toml`](https://search.maven.org/search?q=gestalt-toml)   |
| YamlLoader            | yml and yaml                            | Leverages Jackson to load yaml files and convert them into a ConfigNode tree.                                                                                                                                                                                                                                                                                                                                                                       | [`gestalt-yaml`](https://search.maven.org/search?q=gestalt-yaml)   |
| HoconLoader           | config                                  | Leverages com.typesafe:config to load hocon files, supports substitutions.                                                                                                                                                                                                                                                                                                                                                                          | [`gestalt-hocon`](https://search.maven.org/search?q=gestalt-hocon) |

If you didn't manually add any ConfigLoaders as part of the GestaltBuilder, it will add the defaults. The GestaltBuilder uses the service loader to create instances of the Config loaders. It will configure them by passing in the GestaltConfig to applyConfig.
To register your own default ConfigLoaders add them to the builder, or add it to a file in META-INF\services\org.github.gestalt.config.loader.ConfigLoader and add the full path to your ConfigLoader

By default, Gestalt expects Environment Variables to be screaming snake case, but you can configure it to have a different case.

By registering a `EnvironmentVarsLoaderModuleConfig` with the `GestaltBuilder` you can customize the Environment Loader.

In this example it will expect double `__` as delimiter.
```java
 GestaltBuilder builder = new GestaltBuilder();
Gestalt gestalt = builder
  .addSource(EnvironmentConfigSourceBuilder.builder().build())
  .addModuleConfig(EnvironmentVarsLoaderModuleConfigBuilder
    .builder()
    .setLexer(new PathLexer("__"))
    .build())
  .build();

gestalt.loadConfigs();
```

You can also customize many of the Loaders such as the `YamlLoader`, `TomlLoader`, `JsonLoader` and `HoconLoader` by registering the Module Configs with the builder.

```java
 GestaltBuilder builder = new GestaltBuilder();
Gestalt gestalt = builder
  .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.yaml").build())
  .addModuleConfig(YamlModuleConfigBuilder.builder()
    .setObjectMapper(customObjectmapper)
    .build())
  .build();

gestalt.loadConfigs();
```
