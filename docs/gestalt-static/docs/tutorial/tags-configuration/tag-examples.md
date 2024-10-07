---
sidebar_position: 5
---

# Tags Example
When adding a config source you are able to apply zero or more Tags to the source. Those tags are then applied to all configuration within that source. Tags are optional and can be omitted.  
When retrieving the config it will first search for an exact match to the tags, if provided, then search for the configs with no tags. It will then merge the results.
If you provide 2 tags in the source, when retrieving the configuration you must provide those two exact tags.

```java
// head.shot.multiplier = 1.3
// max.online.players = 32
ConfigSourcePackage pveConfig = ClassPathConfigSourceBuilder.builder()
    .setResource("/test-pve.properties")
    .setTags(Tags.of("mode", "pve"))
    .build();

// head.shot.multiplier = 1.5
ConfigSourcePackage pvpConfig = ClassPathConfigSourceBuilder.builder()
  .setResource("/test-pvp.properties")
  .setTags(Tags.of("mode", "pvp"))
  .build();

// head.shot.multiplier = 1.0
// gut.shot.multiplier = 1.0
ConfigSourcePackage defaultConfig = ClassPathConfigSourceBuilder.builder()
  .setResource("/test.properties")
  .setTags(Tags.of())
  .build(); // Tags.of() can be omitted
          
Gestalt gestalt = builder
  .addSource(pveConfig)
  .addSource(pvpConfig)
  .addSource(defaultConfig)
  .build();

// retrieving "head.shot.multiplier" values change depending on the tag. 
float pvpHeadShot = gestalt.getConfig("head.shot.multiplier", Float.class, Tags.of("mode", "pve"));  // 1.3
float pveHeadShot = gestalt.getConfig("head.shot.multiplier", Float.class, Tags.of("mode", "pvp"));  // 1.5
float coopHeadShot = gestalt.getConfig("head.shot.multiplier", Float.class, Tags.of("mode", "coop"));  // 1.0 fall back to default
float defaultHeadShot = gestalt.getConfig("head.shot.multiplier", Float.class);  // 1.0

// Gut shot is only defined in the default, so it will always return the default. 
float pvpGutShot = gestalt.getConfig("gut.shot.multiplier", Float.class, Tags.of("mode", "pve"));  // 1.0
float pveGutShot = gestalt.getConfig("gut.shot.multiplier", Float.class, Tags.of("mode", "pvp"));  // 1.0
float coopGutSoot = gestalt.getConfig("gut.shot.multiplier", Float.class, Tags.of("mode", "coop"));  // 1.0
float defaultGutShot = gestalt.getConfig("gut.shot.multiplier", Float.class);  // 1.0

// Max online players is only defined in the pvp, so it will only return with the pvp tags. 
float pvpGutShot = gestalt.getConfig("gut.shot.multiplier", Float.class, Tags.of("mode", "pve"));  // 32
float pveGutShot = gestalt.getConfig("gut.shot.multiplier", Float.class, Tags.of("mode", "pvp"));  // not found
float coopGutSoot = gestalt.getConfig("gut.shot.multiplier", Float.class, Tags.of("mode", "coop"));  // not found
float defaultGutShot = gestalt.getConfig("gut.shot.multiplier", Float.class);  // not found
```

* **Note**: The config node processor string replacement doesn't accept tags, so it will always replace the configs with the tag-less ones.
