---
sidebar_position: 1
---

# Getting Started

## Introduction

Gestalt is a powerful Java configuration library that merges multiple sources of configuration data into a type-safe and structured format. It supports configuration files, environment variables, in-memory maps, and more.

### 1. Add the Bintray repository:

Versions 0.1.0 through version 0.11.0 require Java 8. Versions 0.12.0 plus require Java 11.

```kotlin
repositories {
    mavenCentral()
}
```
### 2. Import gestalt-core, and the specific modules you need to support your use cases.

Gradle example:
```groovy
implementation 'com.github.gestalt-config:gestalt-core:${version}'
implementation 'com.github.gestalt-config:gestalt-kotlin:${version}'
```
Or with the kotlin DSL:
```kotlin
implementation("com.github.gestalt-config:gestalt-core:$version")
implementation("com.github.gestalt-config:gestalt-kotlin:$version")
```
Maven Example:
```xml
<dependency>
  <groupId>com.github.gestalt-config</groupId>
  <artifactId>gestalt-core</artifactId>
  <version>${version}</version>
</dependency>
```

### 3. Setup your configuration files

Multiple types of configurations are supported from multiple sources.
Here is an example of the `default.properties`:
```properties
db.hosts[0].user=credmond
db.hosts[0].url=jdbc:postgresql://localhost:5432/mydb1
db.hosts[1].user=credmond
db.hosts[1].url=jdbc:postgresql://localhost:5432/mydb2
db.hosts[2].user=credmond
db.hosts[2].url=jdbc:postgresql://localhost:5432/mydb3
db.connectionTimeout=6000
db.idleTimeout=600
db.maxLifetime=60000.0

http.pool.maxTotal=100
http.pool.maxPerRoute=10
http.pool.validateAfterInactivity=6000
http.pool.keepAliveTimeoutMs=60000
http.pool.idleTimeoutSec=25
```
Here is an example of the `dev.properties`:
```properties
db.hosts[0].url=jdbc:postgresql://dev.host.name1:5432/mydb
db.hosts[1].url=jdbc:postgresql://dev.host.name2:5432/mydb
db.hosts[2].url=jdbc:postgresql://dev.host.name3:5432/mydb
db.connectionTimeout=600

http.pool.maxTotal=1000
http.pool.maxPerRoute=50
```

### 4. Construct Gestalt using the builder.

   Use the builder to construct the Gestalt library. It is possible to do this manually, but the builder greatly simplifies the construction of the library. It uses the service loader to automatically load all the default dependencies.
```java
  // Load the default property files from resources.
  URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
  File devFile = new File(devFileURL.getFile());

  // using the builder to layer on the configuration files.
  // The later ones layer on and over write any values in the previous
  Gestalt gestalt = new GestaltBuilder()
    .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())   
    .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
    .build();

  // Loads and parses the configurations, this will throw exceptions if there are any errors. 
  gestalt.loadConfigs();
```

### 5. Retrieve configurations from Gestalt

   Using the Gestalt Interface you can load sub nodes with dot notation into a wide variety of classes.
   For non-generic classes you can pass in the class with `getConfig("db.port", Integer.class)` or for classes with generic types we need to use a special TypeCapture wrapper that captures the generic type at runtime. This allows us to construct generic classes with such as `List<String>` using  `new TypeCapture<List<String>>() {}`

```java
Short myShortWrapper = gestalt.getConfig("http.pool.maxTotal", Short.class);
HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);
List<HttpPool> httpPoolList = gestalt.getConfig("http.pools", new TypeCapture<>() { });
var httpPoolList = gestalt.getConfig("http.pools", new TypeCapture<List<HttpPool>>() { });
```

The API to retrieve configurations:
```java
  /**
   * Get a config for a path and a given class. 
   * If the config is missing or there are any errors it will throw a GestaltException
   */
  <T> T getConfig(String path, Class<T> klass) throws GestaltException;

  /**
   * Get a config for a path and a given class.
   * If the config is missing, invalid or there was an exception it will return the default value.
   */
  <T> T getConfig(String path, T defaultVal, Class<T> klass);

  /**
   * Get a config Optional for a path and a given class. 
   * If the config is missing, invalid or there was an exception it will return an Optional.empty()
   */
  <T> Optional<T> getConfigOptional(String path, Class<T> klass);
```   

#### Example
Example of how to create and load a configuration objects using Gestalt:
```java
  public static class HttpPool {
    public short maxTotal;
    public long maxPerRoute;
    public int validateAfterInactivity;
    public double keepAliveTimeoutMs = 6000; // has a default value if not found in configurations
    public OptionalInt idleTimeoutSec = 10; // has a default value if not found in configurations
    public float defaultWait = 33.0F; // has a default value if not found in configurations

    public HttpPool() {

    }
  }

  public static class Host {
    private String user;
    private String url;
    private String password;
    private Optional<Integer> port;

    public Host() {
    }

  // getter and setters ...
  }

...
  // load a whole class, this works best with pojo's 
  HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);
  // or get a spcific config value
  short maxTotal = gestalt.getConfig("http.pool.maxTotal", Short.class);
  // get with a default if you want a fallback from code
  long maxConnectionsPerRoute = gestalt.getConfig("http.pool.maxPerRoute", 24, Long.class);


  // get a list of objects, or an PlaceHolder collection if there is no hosts found.
  List<Host> hosts = gestalt.getConfig("db.hosts", Collections.emptyList(), 
    new TypeCapture<List<Host>>() {});
```
