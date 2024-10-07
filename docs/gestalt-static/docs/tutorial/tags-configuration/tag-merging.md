---
sidebar_position: 4
---

# Tags Merging Strategies.

You can provide tags to gestalt in two ways, setting the defaults in the gestalt config and passing in tags when getting a configuration.

```java
Gestalt gestalt = new GestaltBuilder()
.addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())  // Load the default property files from resources.
.addSource(FileConfigSourceBuilder.builder().setFile(devFile).setTags(Tags.profile("dev").build()))
.addSource(FileConfigSourceBuilder.builder().setFile(testFile).setTags(Tags.profile("test").build()))
.setDefaultTags(Tags.profile("dev"))
.build();

// will use the Tags.profile("test") and ignore the default tags of Tags.profile("dev"), so it will use values from the testFile.
HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class, Tags.profile("test"));
```

The default behaviour is to use the provided tags with the `getConfig` and if not provided, fall back to the defaults.

By passing in the TagMergingStrategy to the GestaltBuilder, you can set your own strategy.

```java
Gestalt gestalt = new GestaltBuilder()
      .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
      .addSource(MapConfigSourceBuilder.builder()
          .setCustomConfig(configs2)
          .addTag(Tag.profile("orange"))
          .addTag(Tag.profile("flower"))
          .build())
      .setTagMergingStrategy(new TagMergingStrategyCombine())
      .build();
```

The available strategies are:

| name                           | Set Theory   | Description                                                                         |
|--------------------------------|--------------|-------------------------------------------------------------------------------------|
| TagMergingStrategyFallback     | exclusive or | Use the provided tags with `getConfig`, and if not provided use a default fallback. |
| TagMergingStrategyCombine      | union        | Merge the provided tags with `getConfig`, and the defaults                          |

You can provide your own strategy by implementing TagMergingStrategy.
