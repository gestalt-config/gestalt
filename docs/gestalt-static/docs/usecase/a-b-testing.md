---
sidebar_position: 5
---

# A/B Testing

Create an A/B that gives a different value for a feature flag or other value each time we retrieve the value, and overrides per group for testing or segmentation.

By leveraging runtime string replacement evaluation in the form `#{}` along with the `dist100` you can configure the distribution of certain results and ensure that each call it is re-evaluated. Then use Tags with your groups to control the results for specific groups.

```java
Map<String, String> customMap = new HashMap<>();
// we want 10% of traffic to be true for the default case
customMap.put("newFeature.enabled", "#{dist100:10:true,false}");

// for a user in test group 1 we want the feature to be enabled. 
Map<String, String> customTest1Map = new HashMap<>();
customTest1Map.put("newFeature.enabled", "true");

// for a user in test group 2 we want the feature to be disabled. 
Map<String, String> customTest2Map = new HashMap<>();
customTest2Map.put("newFeature.enabled", "false");

GestaltBuilder builder = new GestaltBuilder();
Gestalt gestalt = builder
  .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
  .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customTest1Map).setTags(Tags.of("group", "test1")).build())
  .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customTest2Map).setTags(Tags.of("group", "test2")).build())

  .build();

gestalt.loadConfigs();

int enabled = 0;
int disabled = 0;
int passes = 100;
while (passes-- > 0) {
  Boolean featEnabled = gestalt.getConfig("newFeature.enabled", TypeCapture.of(Boolean.class));
  if (featEnabled) {
      enabled++;
  } else {
      disabled++;
  }

  Boolean enabledTest1 = gestalt.getConfig("newFeature.enabled", TypeCapture.of(Boolean.class),Tags.of("group", "test1"));
  Assertions.assertTrue(enabledTest1);

  Boolean enabledTest2 = gestalt.getConfig("newFeature.enabled", TypeCapture.of(Boolean.class),Tags.of("group", "test2"));
  Assertions.assertFalse(enabledTest2);
}

// we expect that we should get less than 15 % enabled
Assertions.assertTrue(enabled < 15);
// we expect that we should get more than 85 % enabled
Assertions.assertTrue(disabled > 85);
}
```
