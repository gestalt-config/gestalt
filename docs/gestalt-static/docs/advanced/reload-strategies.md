---
sidebar_position: 1
---

# Reload Strategies
Gestalt is idempotent, as in on calling `loadConfigs()` a config tree is built and will not be updated, even if the underlying sources have changed.
By using Reload strategies you can tell Gestalt when the specific config source has changed to dynamically update configuration on the fly. Once the config tree has been rebuilt, Gestalt will trigger its own Gestalt Core Reload Listener. So you can get an update that the reload has happened.

When adding a ConfigSource to the builder, you can choose to a reload strategy. The reload strategy triggers from either a file change, a timer event or a manual call from your code. Each reload strategy is for a specific source, and will not cause all sources to be reloaded, only that source.
Once Gestalt has reloaded the config it will send out its own Gestalt Core Reload event. you can add a listener to the builder to get a notification when a Gestalt Core Reload has completed. The Gestalt Cache uses this to clear the cache when a Config Source has changed.

```java
  Gestalt gestalt = builder
  .addSource(FileConfigSourceBuilder.builder()
      .setFile(devFile)
      .addConfigReloadStrategy(new FileChangeReloadStrategy())
      .build())
  .addCoreReloadListener(reloadListener)
  .build();
```

| Reload Strategy           | Details                                                                                                                                                                                                                   |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| FileChangeReload          | Specify a FileConfigSource, and the  FileChangeReload will listen for changes on that file. When the file changes it will tell Gestalt to reload the file. Also works with symlink and will reload if the symlink change. |
| TimedConfigReloadStrategy | Provide a ConfigSource and a Duration then the Reload Strategy will reload every period defined by the Duration                                                                                                           |
| ManualConfigReloadStrategy| You can manually call reload to force a source to reload.                                                                                                                                                                 |

## Dynamic Configuration with Reload Strategies

For example if you want to use a Map Config Source, and have updated values reflected in calls to Gestalt, you can register a `ManualConfigReloadStrategy` with a Map Config Source. Then after you can update the values in the map call `reload()` on the `ManualConfigReloadStrategy` to tell Gestalt you want to rebuild its internal Config Tree. Future calls to Gestalt should reflect the updated values.

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

Assertions.assertEquals("value1", gestalt.getConfig("some.value", String.class));

configs.put("some.value", "value2");

manualReload.reload();
Assertions.assertEquals("value2", gestalt.getConfig("some.value", String.class));
```
