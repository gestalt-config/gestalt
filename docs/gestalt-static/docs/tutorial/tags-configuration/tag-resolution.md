---
sidebar_position: 3
---

# Config Node Tags Resolution Strategies.

By default, Gestalt expects tags to be an exact match to select the roots to search. This is configurable by setting a different `ConfigNodeTagResolutionStrategy` in the gestalt builder.

```java
Gestalt gestalt = new GestaltBuilder()
      .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
      .addSource(MapConfigSourceBuilder.builder()
          .setCustomConfig(configs2)
          .addTag(Tag.profile("orange"))
          .addTag(Tag.profile("flower"))
          .build())
      .setConfigNodeTagResolutionStrategy(new SubsetTagsWithDefaultTagResolutionStrategy())
      .build();
```

You can implement the interface `ConfigNodeTagResolutionStrategy` to define your own resolution strategy.

The available strategies are:

| name                                        | Set Theory | Description                                                                                                                                                                                                      |
|---------------------------------------------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| EqualTagsWithDefaultTagResolutionStrategy   | Equals     | Will Search two config node roots, the one that is an equal match to the tags and the root with no tags. Then return the config node roots to be searched. Only return the roots if they exist.                  |
| SubsetTagsWithDefaultTagResolutionStrategy  | Subset     | Will Search for any roots that are a subset of the tags provided with a fallback of the default root. In combination with default tags, this can be used to create a profile system similar to Spring Config.    |
