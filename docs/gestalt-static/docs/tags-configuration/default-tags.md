---
sidebar_position: 2
---

# Default Tags

You can set a default tag in the gestalt builder. The default tags are applied to all calls to get a gestalt configuration when tags are not provided. If the caller provides tags they will be used and the default tags will be ignored.
```java
  Gestalt gestalt = new GestaltBuilder()
    .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())  // Load the default property files from resources. 
    .addSource(FileConfigSourceBuilder.builder().setFile(devFile).setTags(Tags.profile("dev").build()))
    .addSource(FileConfigSourceBuilder.builder().setFile(testFile).setTags(Tags.profile("test").build()))
    .setDefaultTags(Tags.profile("dev"))
    .build();
    
  // has implicit Tags of Tags.profile("dev") that is applied as the default tags, so it will use values from the devFile.
  HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);
  
  // will use the Tags.profile("test") and ignore the default tags of Tags.profile("dev"), so it will use values from the testFile.
  HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class, Tags.profile("test")); 
```
