---
sidebar_position: 7
---

# Logging
Gestalt leverages [System.logger](https://docs.oracle.com/javase/9/docs/api/java/lang/System.Logger.html), the jdk logging library to provide a logging facade. Many logging libraries provide backends for System Logger.


## log4j 2
To use log4j2 as the logging backend for the system logger include these dependencies. This is supported in version 2.13.2 of log4j2.

In Maven:
```xml
<dependency>
  <groupId>org.apache.logging.log4j</groupId>
  <artifactId>log4j-jpl</artifactId>
  <version>${version}</version>
  <scope>runtime</scope>
</dependency>
```
Or in Gradle
```kotlin
implementation("org.apache.logging.log4j:log4j-jpl:${version}")
```


## logback
To use logback as the logging backend for the system logger include these dependencies. This is supported in version 2+ of Logback.

In Maven:
```xml
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-jdk-platform-logging</artifactId>
  <version>${version}</version>
</dependency>
```
Or in Gradle
```kotlin
implementation("org.slf4j:slf4j-jdk-platform-logging:${version}")
```

# Secrets in exceptions and logging
Several places in the library we will print out the contents of a node if there is an error, or you call the debug print functionality.
To ensure that no secrets are leaked we conceal the secrets based on searching the path for several keywords. If the keyword is found in the path the leaf value will be replaced with a configurable mask.


How to configure the masking rules and the mask.
```java
Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSecurityMaskingRule("port")
            .setSecurityMask("&&&&&")
            .build();

        gestalt.loadConfigs();

        String rootNode = gestalt.debugPrint(Tags.of());

        Assertions.assertEquals("MapNode{db=MapNode{password=LeafNode{value='test'}, " +
            "port=LeafNode{value='*****'}, uri=LeafNode{value='my.sql.com'}}}", rootNode);
```

By default, the builder has several rules predefined [here](https://github.com/gestalt-config/gestalt/blob/main/gestalt-core/src/main/java/org/github/gestalt/config/builder/GestaltBuilder.java#L76). 
