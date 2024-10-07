---
sidebar_position: 1
---

# Tags
The API also supports tagged configuration, where providing a tag will retrieve configs that match the specific tags or fallback to the default of no tags.
You can implement profiles or environments using tags.

```java
 <T> T getConfig(String path, T defaultVal, Class<T> klass, Tags tags);
 HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class, Tags.of("environment", "dev"));
```

Most configuration sources support tagging them. So you can easily add tags to all properties in a source for your profile or environment.

```java
 Gestalt gestalt = new GestaltBuilder()
    .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())  // Load the default property files from resources. 
    .addSource(FileConfigSourceBuilder.builder().setFile(devFile).setTags(Tags.of("environment", "dev")).build())
    .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
    .build();
```

There are utility methods for common tags such as profile and environment.
```java
Tags.profile("test") == Tags.of("profile", "test")
Tags.environment("dev") == Tags.of("environment", "dev")
```
