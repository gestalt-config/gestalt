---
sidebar_position: 1
---

# Annotations Configurations

Certain annotations can be applied to a configuration using `@{annotation}`, this will covert the annotation to metadata that can be applied to the node. Then the metadata is used to apply the intended behaviour to the node. 

For example, we can apply the temporary node feature on a node by using the annotation `@{temp:1}`
```properties
my.password=abcdef@{temp:1}
```

| annotation | parameter                                        | description                                                                                  |
|------------|--------------------------------------------------|----------------------------------------------------------------------------------------------|
| temp       | (int) Number of times this temp node can be read | restrict the number of times a value can be read before it is released                       |
| encrypt    | (boolean) if we should apply to this node        | Encrypts the node in memory.                                                                 |
| nocache    | (boolean) if we should apply to this node        | Will not cache the node. If a node is part of a object the whole object will not be cached.  |
| secret     | (boolean) if we should apply to this node        | Treats the node as a secret, so it will not print it out in errors or the debug print.       |

## Trim Whitespace

By default, white spaces before and after the annotation are trimmed. You can disable this feature using the gestalt builder and setting `setAnnotationTrimWhiteSpace(false)`

```java
GestaltBuilder builder = new GestaltBuilder();
Gestalt gestalt = builder
  .addSource(MapConfigSourceBuilder.builder()
    .setCustomConfig(configs)
    .build())
  .setAnnotationTrimWhiteSpace(false)
  .build();
```
