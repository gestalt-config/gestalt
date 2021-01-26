# Gestalt
A Java configuration library that allows you to build your configurations from multiple sources, merges them and convert them into an easy-to-use typesafe configuration class. 
A simple but powerful interface allows you to navigate to a path within your configurations and retrieve a configuration object, list, or a primitive value.

# Features
- **Automatic decoding based on type:** support decoding into most classes, lists, sets or primitive types.
- **Supports Multiple formats:** Load your configurations from Environment Variables, Property files, an in memory map or more.   
- **Read sub-sections of your config:** Navigate to a path within your configurations and load a sub section.
- **Kotlin interface:** Full support for Kotlin with an easy to use kotlin-esk interface that makes it easy to integrate into any kotlin project.   
- **Merge Multiple Sources:** Merge multiple config sources together by layering on your configurations.
- **Flexible and configurable:** Library is a collection of lego pieces with well-defined interfaces, so you can add to or modify any part of it. 
- **Easy to use builder:** Easy to use builder can get you running quick, or be used to customize any part of the library.

# Getting Started
1. Add the Bintray repository:
```kotlin
repositories {
    jcenter()
}
```
2. Import gestalt-core, and the specific modules you need to support your use cases. 
Gradle example:
```groovy
implementation 'org.config.gestalt:gestalt-core:${version}'
implementation 'org.config.gestalt:gestalt-kotlin:${version}'
```
Or
```kotlin
testImplementation("org.config.gestalt:gestalt-core:$version")
testImplementation("org.config.gestalt:gestalt-kotlin:$version")
```
Maven Example:
```xml
<dependency>
  <groupId>org.config.gestalt</groupId>
  <artifactId>gestalt-core</artifactId>
  <version>${version}</version>
</dependency>
```

3. Setup your configuration files

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

4. Construct Gestalt using the builder. 
You can use the builder to construct the Gestalt library. It is possible to do this manually as well, but the builder greatly simplifies the construction of the library.
```java
// Create a map of configurations we wish to inject. 
Map<String, String> configs = new HashMap<>();
configs.put("db.hosts[0].password", "1234");
configs.put("db.hosts[1].password", "5678");
configs.put("db.hosts[2].password", "9012");
configs.put("db.idleTimeout", "123");

// Load the default property files from resources. 
URL defaultFileURL = GestaltSample.class.getClassLoader().getResource("default.properties");
File defaultFile = new File(defaultFileURL.getFile());

// Load the environment specific property files from resources.
URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
File devFile = new File(devFileURL.getFile());

// using the builder to layer on the configuration files. 
// The later ones layer on and over write any values in the previous
Gestalt gestalt = new GestaltBuilder()
  .addSource(new FileConfigSource(defaultFile))
  .addSource(new FileConfigSource(devFile))
  .addSource(new MapConfigSource(configs))
  .build();

// Load the configurations, this will thow exceptions if there are any errors. 
gestalt.loadConfigs();
```

5. Retrieve configurations from Gestalt
Using the simple API you can load sub sections of your configurations with a wide variety of classes. 
For simple classes you can pass in the type of the class with `getConfig("db.port", Integer.class)` or for classes with generic types we need to use a special TypeCapture wrapper that captures the generic type at runtime, so we can construct complex classes with generic types such as List<String> `new TypeCapture<List<String>>() {}` 
   
The API is as simple as:
```java
/**
 * Get a config for a path and a given class. 
 * If the config is missing or there are any errors it will throw a GestaltException
 */
<T> T getConfig(String path, Class<T> klass) throws GestaltException;

/**
 * Get a config for a path and a given TypeCapture. 
 * If the config is missing or there are any errors it will throw a GestaltException
 */
<T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException;

/**
 * Get a config for a path and a given class.
 * If the config is missing or invalid it will return the default value.
 */
<T> T getConfig(String path, T defaultVal, Class<T> klass);

/**
 * Get a config for a path and a given class.
 * If the config is missing or invalid it will return the default value.
 */
<T> T getConfig(String path, T defaultVal, TypeCapture<T> klass);

/**
 * Get a config Optional for a path and a given class. 
 * If there are any exceptions or errors it will return an Optional.empty()
 */
<T> Optional<T> getConfigOptional(String path, Class<T> klass);

/**
 * Get a config Optional for a path and a given TypeCapture. 
 * If there are any exceptions or errors it will return an Optional.empty()
 */
<T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass);
```   

Example of how to create and load a configuration using Gestalt:
```java
public static class HttpPool {
    public short maxTotal;
    public long maxPerRoute;
    public int validateAfterInactivity;
    public double keepAliveTimeoutMs = 6000; // has a default value if not found in configurations
    public int idleTimeoutSec = 10; // has a default value if not found in configurations
    public float defaultWait = 33.0F; // has a default value if not found in configurations

    public HttpPool() {

    }
}

public static class Host {
  private String user;
  private String url;
  private String password;

  public Host() {
  }

  // getter and setters ...
}

...
// load a whole class, this works best with pojo's
HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);
// or get a spcific config value
short maxTotal  gestalt.getConfig("http.pool.maxTotal", Short.class);
// get with a default if you want a fallback from code
long maxConnectionsPerRoute = gestalt.getConfig("http.pool.maxPerRoute", Long.class);

// get a list of objects as well.
List<Host> hosts = gestalt.getConfig("db.hosts", Collections.emptyList(), 
  new TypeCapture<List<Host>>() {});
```
With kotlin this is made easier with the inline reified methods that automatically capture the type for you based on return type. 
```kotlin
data class HttpPool(
  var maxTotal: Short = 0,
  var maxPerRoute: Long = 0,
  var validateAfterInactivity: Int = 0,
  var keepAliveTimeoutMs: Double = 6000.0,
  var idleTimeoutSec: Short = 10,
  var defaultWait: Float = 33.0f
)
// load a kotlin data class
val pool: HttpPool = gestalt.getConfig("http.pool")
// get a list of objects as well.
val hosts: List<Host> = gestalt.getConfig("db.hosts", emptyList())
```   

For more examples of how to use gestalt see the [gestalt-sample](https://github.com/credmond-git/gestalt/tree/main/gestalt-sample/src/test)

