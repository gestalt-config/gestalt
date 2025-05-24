---
sidebar_position: 1
---

# Config Sources

Adding a ConfigSource to the builder is the minimum step needed to build the Gestalt Library.
You can add several ConfigSources to the builder and Gestalt, and they will be loaded in the order they are added. Where each new source will be merged with the existing source and where applicable overwrite the values of the previous sources. Each Config Source can be a diffrent format such as json, properties or Snake Case Env Vars, then internally they are converted into a common config tree.

```java
  Gestalt gestalt = builder
    .addSource(FileConfigSourceBuilder.builder().setFile(defaults).build())
    .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
    .addSource(EnvironmentConfigSourceBuilder.builder().setPrefix("MY_APP_CONFIG").build())
    .build();
```
In the above example we first load a file defaults, then load a file devFile and overwrite any defaults, then overwrite any values from the Environment Variables.
The priority will be Env Vars > devFile > defaults.

## Adding Config Sources After Loading
Typically, you add config sources as part of the builder, then call the `loadConfigs` to build the config tree. However, if you need to dynamically add a config source after initialization you can use the `addConfigSourcePackage` method. This will load and merge the new config source into the config tree. It will then call the core reload listener and reset all caches. 

```java
  Gestalt gestalt = builder
    .addSource(FileConfigSourceBuilder.builder().setFile(defaults).build())
    .build();

  gestalt.loadConfigs();
  gestalt.addConfigSourcePackage(FileConfigSourceBuilder.builder().setFile(devFile).build());

  gestalt.addConfigSourcePackage(EnvironmentConfigSourceBuilder.builder().setPrefix("MY_APP_CONFIG").build());
```
