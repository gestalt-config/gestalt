# Gestalt
[![Maven Central](https://img.shields.io/maven-central/v/com.github.gestalt-config/gestalt-core?label=MavenCentral&logo=apache-maven)](https://search.maven.org/artifact/com.github.gestalt-config/gestalt-core)
[![License](https://img.shields.io/github/license/gestalt-config/gestalt.svg)](LICENSE)
[![codecov](https://codecov.io/gh/gestalt-config/gestalt/branch/main/graph/badge.svg)](https://codecov.io/gh/gestalt-config/gestalt)

Gestalt is a powerful Java configuration library designed to simplify the way you handle and manage configurations in your software projects. In the rapidly evolving landscape of software development, having a flexible and reliable configuration management system is essential for ensuring the smooth operation of your applications.

Gestalt offers a comprehensive solution to the challenges of configuration management. It allows you to source configuration data from multiple inputs, merge them intelligently, and present them in a structured, type-safe manner. Whether you're working with Java beans, lists, sets, or primitive data types, Gestalt's automatic decoding based on data types simplifies the process.

This documentation will guide you through the key features of Gestalt, demonstrate how to get started quickly, and provide detailed insights into its capabilities. Whether you're a seasoned Java developer or just beginning your journey, Gestalt will empower you to manage your application configurations effectively and efficiently.

Let's dive in and explore how Gestalt can streamline your configuration management workflow and help you build more robust and adaptable software.

# Features
- **Automatic decoding based on type:** Supports decoding into bean classes, lists, sets, or primitive types. This simplifies configuration retrieval.

- **Java Records:** Full support for Java Records, constructing records from configuration using the Records Canonical Constructor.

- **Supports Multiple Formats:** Load configurations from various sources, including Environment Variables, Property files, an in-memory map, and more.

- **Read Sub-sections of Your Config:** Easily navigate to specific sub-sections within configurations using dot notation.

- **Kotlin interface:** Full support for Kotlin with an easy-to-use Kotlin-esque interface, ideal for Kotlin projects.

- **Merge Multiple Sources:** Seamlessly merge configurations from different sources to create comprehensive settings.

- **Flexible and Configurable:** The library offers well-defined interfaces, allowing customization and extension.

- **Easy-to-Use Builder:** Get started quickly with a user-friendly builder, or customize specific aspects of the library.

- **Receive All Errors Up Front:** In case of configuration errors, receive multiple errors in a user-friendly log for efficient debugging.

- **Modular Support for Features:** Include only the required features and dependencies in your build, keeping your application lightweight.

- **Zero Dependencies:** The core library has zero external dependencies; add features and dependencies as needed.

- **Java 11 Minimum:** Requires a minimum of Java 11 for compatibility with modern Java versions.

- **Java Modules:** Supports Java 9 modules with proper exports.



# Getting Started
1. Add the Bintray repository:

Versions 0.1.0 through version 0.11.0 require Java 8. Versions 0.12.0 plus require Java 11.

```kotlin
repositories {
    mavenCentral()
}
```
2. Import gestalt-core, and the specific modules you need to support your use cases.

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

   Use the builder to construct the Gestalt library. It is possible to do this manually, but the builder greatly simplifies the construction of the library. It uses the service loader to automatically load all the default dependencies.
```java
  // Create a map of configurations we wish to inject. 
  Map<String, String> configs = new HashMap<>();
  configs.put("db.hosts[0].password", "1234");
  configs.put("db.hosts[1].password", "5678");
  configs.put("db.hosts[2].password", "9012");
  configs.put("db.idleTimeout", "123");

  // Load the default property files from resources.
  URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
  File devFile = new File(devFileURL.getFile());

  // using the builder to layer on the configuration files.
  // The later ones layer on and over write any values in the previous
  Gestalt gestalt = new GestaltBuilder()
    .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())  // Load the default property files from resources. 
    .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
    .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
    .build();

  // Loads and parses the configurations, this will throw exceptions if there are any errors. 
  gestalt.loadConfigs();
```

5. Retrieve configurations from Gestalt

   Using the Gestalt Interface you can load sub nodes with dot notation into a wide variety of classes.
   For non-generic classes you can pass in the class with `getConfig("db.port", Integer.class)` or for classes with generic types we need to use a special TypeCapture wrapper that captures the generic type at runtime. This allows us to construct generic classes with such as List<String> using  `new TypeCapture<List<String>>() {}`

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


# Config Sources
Adding a ConfigSource to the builder is the minimum step needed to build the Gestalt Library.
You can add several ConfigSources to the builder and Gestalt, and they will be loaded in the order they are added. Where each new source will be merged with the existing source and where applicable overwrite the values of the previous sources.

```java
  Gestalt gestalt = builder
    .addSource(FileConfigSourceBuilder.builder().setFile(defaults).build())
    .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
    .addSource(EnvironmentConfigSourceBuilder.builder().setPrefix("my.app.config").build())
    .build();
```
In the above example we first load a file defaults, then load a file devFile and overwrite any defaults, then overwrite any values from the Environment Variables.
The priority will be Env Vars > devFile > defaults.

# Config Tree
The config files are loaded and merged into a config tree. The config tree has a structure (sort of like json) where the root has one or more nodes or leafs. A node can have one or more nodes or leafs. A leaf can have a value but no further nodes or leafs. As we traverse the tree each node or leaf has a name and in combination it is called a path. A path can not have both a node and a leaf at the same place.    

Valid:
```properties

db.connectionTimeout=6000
db.idleTimeout=600
db.maxLifetime=60000.0

http.pool.maxTotal=1000
http.pool.maxPerRoute=50
```

Invalid:
```properties

db.connectionTimeout=6000
db.idleTimeout=600
db=userTable                #invalid the path db is both a node and a leaf. 

http.pool.maxTotal=1000
http.pool.maxPerRoute=50
```

All paths are converted to lower case as different sources have different naming conventions, Env Vars are typically Screaming Snake Case, properties are dot notation, json is camelCase. By normalizing them to lowercase it is easier to merge. 


# Retrieving a configuration

To retrieve a configuration from Gestalt we need the path to the configuration as well as what type of class.

### getConfig path options
Gestalt is **not case sensitive**. Since Gestalt interops between Environment Variables and other sources with various cases, all strings in Gestalt are normalized to a lower case.
By default Gestalt uses dot notation and allows indexing into arrays using a '[0]'.  
If you want to use a different path style you can provide your own SentenceLexer to Gestalt. The SentenceLexer is used to convert the path passed to the Gestalt getConfig interface into tokens that Gestalt can use to navigate to your sub node.

```java
  // load a whole class, this works best with pojo's
  HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);
  // or get a specific config value from a class
  short maxTotal  gestalt.getConfig("HTTP.pool.maxTotal", Short.class);
  // get with a default if you want a fallback from code
  long maxConnectionsPerRoute = gestalt.getConfig("http.Pool.maxPerRoute", 24, Long.class);

  // get a list of Host objects, or an PlaceHolder collection if there is no hosts found.
  List<Host> hosts = gestalt.getConfig("db.hosts", Collections.emptyList(), 
    new TypeCapture<List<Host>>() {});

  // Get a class at a specific list index. 
  Host host = gestalt.getConfig("db.hosts[2]", Host.class);
  // get a value of a class from a specific list index.
  String password = gestalt.getConfig("db.hosts[2].password", String.class);
```


### Retrieving Primitive and boxed types
Getting primitive and boxed types involves calling Gestalt and providing the class of the type you are trying to retrieve. 

```java
Short myShortWrapper = gestalt.getConfig("http.pool.maxTotal", Short.class);
short myShort = gestalt.getConfig("http.pool.maxTotal", short.class);
String serviceMode = gestalt.getConfig("serviceMode", String.class);
```

Gestalt will automatically decode and provide the value in the type you requested. 

### Retrieving complex objects
To get a complex object you need to pass in the class for gestalt to return. 
Gestalt will automatically use reflection to create the object, determine all the fields in the class requested, then lookup the values in the configurations to inject into the object.
It will attempt to use the setter fields first then fallback to directly setting the fields. 

```java
HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);
```

### Retrieving Interfaces
To get an interface you need to pass in the interface class for gestalt to return.
Gestalt will use a proxy object when requesting an interface. When you call a method on the proxy it will look up the similarly named property, decode and return it. 

```java
iHttpPool pool = gestalt.getConfig("http.pool", iHttpPool.class);
```

### Retrieving Generic objects
To get an interface you need to pass in a TypeCapture with the Generic value of the class for gestalt to return.
Gestalt supports getting Generic objects such as Lists or Maps. However, due to type erasure we need to capture the type using the TypeCapture class. The Generic can be any type Gestalt supports decoding such as a primitive wrapper or an Object.    

```java
List<HttpPool> httpPoolList = gestalt.getConfig("http.pool", new TypeCapture<>() { });
var httpPoolMap = gestalt.getConfig("http.pool", new TypeCapture<Map<String, HttpPool>>() { });
```

#### Config Data Type
For non-generic classes you can use the interface that accepts a class `HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);`, for Generic classes you need to use the interface that accepts a TypeCapture `List<HttpPool> pools = gestalt.getConfig("http.pools", Collections.emptyList(),
new TypeCapture<List<HttpPool>>() {});` to capture the generic type. This allows you to decode Lists, Sets and Maps with a generic type.

There are multiple ways to get a configuration with either a default, an Optional or the straight value. With the default and Optional Gestalt will not throw an exception if there is an error, instead returning a default or an PlaceHolder Option and log any warnings or errors.


#### Tags
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

##### Default Tags

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


#### Kotlin
In Kotlin you dont need to specify the types if you used the kotlin extension methods provided in `gestalt-kotlin`. It uses inline reified methods that automatically capture the type for you based on return type. If no configuration is found and the type is nullable, it will return null otherwise it will throw an GestaltException.

```kotlin
  data class HttpPool(
    var maxTotal: Short = 0,
    var maxPerRoute: Long = 0,
    var validateAfterInactivity: Int? = 0,
    var keepAliveTimeoutMs: Double = 6000.0,
    var idleTimeoutSec: Short = 10,
    var defaultWait: Float = 33.0f
  )
  // load a kotlin data class
  val pool: HttpPool = gestalt.getConfig("http.pool")
  // get a list of objects, or an PlaceHolder collection if there is no hosts found.
  val hosts: List<Host> = gestalt.getConfig("db.hosts", emptyList())
```   



# Annotations
When decoding a Java Bean style class, a record, an interface or a Kotlin Data Class you can provide a custom annotation to override the path for the field as well as provide a default.
The field annotation `@Config` takes priority if both the field and method are annotated.
The class annotation `@ConfigPrefix` allows the user to define the prefix for the config object as part of the class instead of the `getConfig()` call. If you provide both the resulting prefix is first the path in getConfig then the prefix in the `@ConfigPrefix` annotation.
For example using `@ConfigPrefix(prefix = "connection")` with `DBInfo pool = gestalt.getConfig("db", DBInfo.class);` the resulting path would be `db.connection`.

```java
@ConfigPrefix(prefix = "db")
public class DBInfo {
    @Config(path = "channel.port", defaultVal = "1234")
    private int port;

    public int getPort() {
        return port;
    }
}

DBInfo pool = gestalt.getConfig("", DBInfo.class);


public class DBInfo {
    private int port;

    @Config(path = "channel.port", defaultVal = "1234")
    public int getPort() {
        return port;
    }
}  

DBInfo pool = gestalt.getConfig("db.connection", DBInfo.class);
```

The path provided in the annotation is used to find the configuration from the base path provided in the call to Gestalt getConfig.

So if the base path from gestalt.getConfig is `db.connection` and the annotation is `channel.port` the path the configuration will look for is `db.connection.channel.port`

The default accepts a string type and will be decoded into the property type using the gestalt decoders. For example if the property is an Integer and the default is "100" the integer value will be 100.


# Searching for path while Decoding Objects
When decoding a class, we need to know what configuration to lookup for each field. To generate the name of the configuration to lookup, we first check for any annotations. If there are no annotations, then we search for the fields by exact match. So we look for a config value with the same name as the field. If it is unable to find the exact match, it will attempt to map it to a path based on camel case. Where the camel case words will be separated and converted to Kebab case, Snake case and Dot Notation, then used to search for the configuration.
The order is descending based on the priority of the mapper.

| Casing                   | Priority | Class Name            |
|--------------------------|----------|-----------------------|
| Camel Case (exact match) | 1000     | StandardPathMapper    |
| Kebab Case               | 600      | KebabCasePathMapper   |
| Snake Case               | 550      | SnakeCasePathMapper   |
| Dot Notation             | 500      | DotNotationPathMapper |

Given the following class lets see how it is translated to the different casings:
```java
// With a class of 
public static class DBConnection {
    @Config(path = "host")
    private String uri;
    private int dbPort;
    private String dbPath;
}
```

Kebab Case:
All objects in Java use the standard Camel case, however in the config files you can use Kebab case, and if an exact match isnt found it will search for a config variable converting Camel case into Kebab case.
Kebab case or an exact match are preferred as using dot notation could potentially cause some issues as it is parsed to a config tree. Using dot notation you would need to ensure that none of values break the tree rules.

```java
// Given a config of
"users.host" => "myHost"
"users.uri" => "myHost"
"users.dbPort" => "1234"
"users.db-path" => "usersTable"
  
// the uri will map to host
// the dbPort will map to dbPort using Camel case using exact match.
// the dbPath will automatically map to db.path using Kebab case.     
DBConnection connection = gestalt.getConfig("users", TypeCapture.of(DBConnection.class));
```

Snake Case:
All objects in Java use the standard Camel case, however in the config files you can use Snake case, and if an exact match isnt found it will search for a config variable converting Camel case into snake case.

```java
// Given a config of
"users.host" => "myHost"
"users.uri" => "myHost"
"users.dbPort" => "1234"
"users.db_path" => "usersTable"
  
// the uri will map to host
// the dbPort will map to dbPort using Camel case using exact match.
// the dbPath will automatically map to db_path using snake case     
DBConnection connection = gestalt.getConfig("users", TypeCapture.of(DBConnection.class));
```

Dot Notation:
All objects in Java use the standard Camel case, however in the config files you can use Dot Notation, and if an exact match isnt found it will search for a config variable converting Camel case into Dot Notation.
Kebab case or an exact match are preferred as using dot notation could potentially cause some issues as it is parsed to a config tree. Using dot notation you would need to ensure that none of values break the tree rules.

```java
// Given a config of
"users.host" => "myHost"
"users.uri" => "myHost"
"users.dbPort" => "1234"
"users.db.path" => "usersTable"
  
// the uri will map to host
// the dbPort will map to dbPort using Camel case using exact match.
// the dbPath will automatically map to db.path using dot notation.     
DBConnection connection = gestalt.getConfig("users", TypeCapture.of(DBConnection.class));
```

# Kotlin
For Kotlin Gestalt includes several extension methods that allow easier use of Gestalt by way of reified functions to better capture the generic type information.
Using the extension functions you don't need to specify the type if the return type has enough information to be inferred. If nothing is found it will throw a GestaltException unless the type is nullable, then it will return null.
```kotlin
  val pool: HttpPool = gestalt.getConfig("http.pool")
  val hosts: List<Host> = gestalt.getConfig("db.hosts", emptyList())
```
| Gestalt Version  | Kotlin Version |
|------------------|----------------|
| 0.25.0 +         | 1.9            |
| 0.17.0 +         | 1.8            |
| 0.13.0 to 0.16.6 | 1.7            |
| 0.10.0 to 0.12.0 | 1.6            |
| 0.9.0 to 0.9.3   | 1.5            |
| 0.1.0 to 0.8.1   | 1.4            |


# String Substitution
Gestalt supports string substitutions at load time on configuration properties to dynamically modify configurations.

For example if we have a properties file with a Database connection you don't want to save your usernames and passwords in the properties files. Instead, you want to inject the username and passwords as Environment Variables.

```properties
db.user=${DB_USER}
db.password=${DB_PASSWORD}
```

You can use multiple string replacements within a single string to build a configuration property.

```properties
db.uri=jdbc:mysql://${DB_HOST}:${DB_PORT}/${environment}
```

### Specifying the Transformer
You can specify the substitution in the format ${transform:key} or ${key}. If you provide a transform name it will only check that one transform. Otherwise, it will check all the Transformer annotated with a `@ConfigPriority` in descending order and will return the first matching value.
Unlike the rest of Gestalt, this is case-sensitive, and it does not tokenize the string (except the node transform). The key expects an exact match, so if the Environment Variable name is DB_USER you need to use the key DB_USER, db.user or db_user will not match.

```properties
db.uri=jdbc:mysql://${DB_HOST}:${map:DB_PORT}/${sys:environment}
```

### Defaults for a Substitution
You can provide a default for the substitution in the format `${transform:key:=default}` or `${key:=default}`. If you provide a default it will use the default value in the event that the key provided cant be found

```properties
db.uri=jdbc:mysql://${DB_HOST}:${map:DB_PORT:=3306}/${environment:=dev}
```

Using nested substitution, you can have a chain of defaults. Where you can fall back from one source to another. 

```properties
test.duration=${sys:duration:=${env:TEST_DURATION:=120}}
```
In this example, it will first try the system variable `duration`, then the Environment Variable `TEST_DURATION` and finally if none of those are found, it will use the default `120`

### Escaping a Substitution
You can escape the value with '\' like `\${my text}` to prevent the substitution. In Java you need to write `\\` to escape the character in a normal string but not in a Text block
In nested substitutions you should escape both the opening token `\${` and the closing token `\}` to be clear what is escaped, otherwise you may get undetermined results. 

```properties
user.block.message=You are blocked because \\${reason\\}
```


### Nested Substitutions
Gestalt supports nested and recursive substitutions. Where a substitution can happen inside another substitution and the results could trigger another substitution.
Please use nested substitution sparingly, it can get very complex and confusing quickly. 
Using these variables:

Environment Variables:
```properties
DB_HOST=cloudHost
environment=dev
```

System Variables:
```properties
DB_HOST=localHost
environment=test
```

Map Variable:
```properties
DB_TRANSFORM=sys
DB_PORT=13306
```
config source:
```properties
db.uri=jdbc:mysql://${${DB_TRANSFORM}:DB_HOST}:${map:DB_PORT}/${sys:environment}
```

This will resolve ${DB_TRANSFORM} => `sys`
then resolve ${sys:DB_HOST} => `localHost`
For a configuration value of `db.uri=jdbc:mysql://localHost:13306/test`


Nested substitution resolving to a nested substitution.
Given properties:
```properties
this.path = greeting
your.path = ${this.path}
my.path.greeting = good day
```

And a string to Substitute:
`"${my.path.${your.path}}"`

the result is `good day`

`${your.path}` resolves to `${this.path}`
`${this.path}` is then resolved to `greeting`
And finally the path `my.path.greeting` is resolved to `good day`

### Provided Transformers
| keyword      | priority | source                                                                                                                                                                                                                                                                                                                    |
|--------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| env          | 100      | Environment Variables                                                                                                                                                                                                                                                                                                     |
| envVar       | 100      | **Deprecated** Environment Variables                                                                                                                                                                                                                                                                                      |
| sys          | 200      | Java System Properties                                                                                                                                                                                                                                                                                                    |
| map          | 400      | A custom map provided to the constructor                                                                                                                                                                                                                                                                                  |
| node         | 300      | map to another leaf node in the configuration tree                                                                                                                                                                                                                                                                        |
| random       | n/a      | provides a random value                                                                                                                                                                                                                                                                                                   |
| base64Decode | n/a      | decode a base 64 encoded string                                                                                                                                                                                                                                                                                           |
| base64Encode | n/a      | encode a base 64 encoded string                                                                                                                                                                                                                                                                                           |
| classpath    | n/a      | load the contents of a file on the classpath into a string substitution.                                                                                                                                                                                                                                                  |
| file         | n/a      | load the contents of a file into a string substitution                                                                                                                                                                                                                                                                    |
| urlDecode    | n/a      | URL decode a string                                                                                                                                                                                                                                                                                                       |
| urlEncode    | n/a      | URL encode a string                                                                                                                                                                                                                                                                                                       |
| awsSecret    | n/a/     | An AWS Secret is injected for the secret name and key. Configure the AWS Secret by registering a AWSModuleConfig using the AWSBuilder.   ```Gestalt gestalt = builder.addModuleConfig(AWSBuilder.builder().setRegion("us-east-1").build()).build();```                                                                    |
| gcpSecret    | n/a      | A Google Cloud Secret given the key provided. Optionally configure the GCP Secret by registering an GoogleModuleConfig using the GoogleBuilder, or let google use the defaults.  ``` Gestalt gestalt = builder.addModuleConfig(GoogleBuilder.builder().setProjectId("myProject").build()).build()```                      |
| vault        | n/a      | A vault Secret given the key provided. Configure the Vault Secret by registering an VaultModuleConfig using the VaultBuilder.  ``` Gestalt gestalt = builder.addModuleConfig(VaultBuilder.builder().setVault(vault).build()).build()```. Uses the io.github.jopenlibs:vault-java-driver project to communicate with vault |


### Random String Substitution
To inject a random variable during post-processing you can use the format ${random:type(origin, bound)}
The random value is generated while loading the config, so you will always get the same random value when asking gestalt.

```properties
db.userId=dbUser-${random:int(5, 25)}
app.uuid=${random:uuid}
```

#### Random Options supported:
| data type | format                | notes                                                |
|-----------|-----------------------|------------------------------------------------------|
| byte      | byte                  | a random byte of data base 64 encoded                |
| byte      | byte(length)          | random bytes of provided length base 64 encoded      |
| int       | int                   | a random int of all possible int values              |
| int       | int(max)              | a random int from 0 to the max value provided        |
| int       | int(origin, bound)    | a random int between origin and bound                |
| long      | long                  | a random long of all possible long values            |
| long      | long(max)             | a random long from 0 to the max value provided       |
| long      | long(origin, bound)   | a random long between origin and bound               |
| float     | float                 | a random float between 0 and 1                       |
| float     | float(max)            | a random float from 0 to the max value provided      |
| float     | float(origin, bound)  | a random float between origin and bound              |
| double    | double                | a random double of all possible long values          |
| double    | double(max)           | a random double from 0 to the max value provided     |
| double    | double(origin, bound) | a random double between origin and bound             |
| boolean   | boolean               | a random boolean                                     |
| string    | string                | a random string of characters a-z of length 1        |
| string    | string(length)        | a random string of characters a-z of length provided |
| char      | char                  | a random char of characters a-z                      |
| uuid      | uuid                  | a random uuid                                        |
* Note: The formats in the table would need to be embedded inside of ${random:format} so byte(length) would be ${random:byte(10)}


# Tags
When adding a config source you are able to apply zero or more Tags to the source. Those tags are then applied to all configuration within that source. Tags are optional and can be omitted.  
When retrieving the config it will first search for an exact match to the tags, if provided, then search for the configs with no tags. It will then merge the results.
If you provide 2 tags in the source, when retrieving the configuration you must provide those two exact tags.

```java
  // head.shot.multiplier = 1.3
// max.online.players = 32
    ConfigSourcePackage pveConfig = ClassPathConfigSourceBuilder.builder().setResource("/test-pve.properties").setTags(Tags.of("mode", "pve")).build();

    // head.shot.multiplier = 1.5
    ConfigSourcePackage pvpConfig = ClassPathConfigSourceBuilder.builder().setResource("/test-pvp.properties").setTags(Tags.of("mode", "pvp")).build();

    // head.shot.multiplier = 1.0
    // gut.shot.multiplier = 1.0
    ConfigSourcePackage defaultConfig = ClassPathConfigSourceBuilder.builder().setResource("/test.properties").setTags(Tags.of()).build(); // Tags.of() can be omitted
          
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

* Note: The post processor string replacement doesn't accept tags, so it will always replace the configs with the tag-less ones.


## Supported config sources
| Config Source                | module                                                               | Details                                                                                                                                                                                                                                                                                                                 |
|------------------------------|----------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| ClassPathConfigSource        | gestalt-core                                                         | Load a file from the java class path. Uses getResourceAsStream to find and load the InputStream.                                                                                                                                                                                                                        |
| EnvironmentConfigSource      | gestalt-core                                                         | Loads all Environment Variables in the system, will convert them to a list of key values from the Env Map for the config loader. You can provide a prefix to only load Environment Variables with the prefix. Then you can choose to keep the prefix or remove it.                                                      |
| FileConfigSource             | gestalt-core                                                         | Loads a file from the local file system. The format for the source will depend on the file extension of the file. For example if it is dev.properties, the format will be properties. Returns a InpuStream for the config loader.                                                                                       |
| KubernetesSecretConfigSource | gestalt-core                                                         | Specify a path to search for [kubernetes secrets](https://kubernetes.io/docs/concepts/configuration/secret/) files. The directory is scanned and each file is added to the configuration. The name of the file is treated as the key for configuration and the content of the file is the value for the configuration.  |
| GCSConfigSource              | [`gestalt-google`](https://search.maven.org/search?q=gestalt-google) | Load a config from Google Cloud Storage. Requires a bucketName and a objectName. A google Storage object is optional, otherwise it defaults to the default instance.                                                                                                                                                    |
| GitConfigSource              | [`gestalt-git`](https://search.maven.org/search?q=gestalt-git)       | Syncs a remote repo locally then uses the files to build a configuration. This uses jgit and supports several forms of authentication. See GitConfigSourceTest.java for examples of use.                                                                                                                                |
| MapConfigSource              | gestalt-core                                                         | Allows you to pass in your own map, it will convert the map into a list of path and value for the config loader.                                                                                                                                                                                                        |
| StringConfigSource           | gestalt-core                                                         | Takes any string and converts it into a InputStream. You must also provide the format type so we can match it to a loader.                                                                                                                                                                                              |
| SystemPropertiesConfigSource | gestalt-core                                                         | Loads the Java System Properties and convert them to a list of key values or the config loader.                                                                                                                                                                                                                         |
| S3ConfigSource               | [`gestalt-aws`](https://search.maven.org/search?q=gestalt-aws)       | Loads a config source from AWS S3, Must include package com.github.gestalt-config:gestalt-s3:version.                                                                                                                                                                                                                   |
| URLConfigSource              | gestalt-core                                                         | Loads a config source from a URL.                                                                                                                                                                                                                                                                                       |


# Config Loader
Each config loader understands how to load a specific type of config. Often this is associated with a specific ConfigSource. For example the EnvironmentVarsLoader only loads the EnvironmentConfigSource. However, some loaders expect a format of the config, but accept it from multiple sources. For example the PropertyLoader expects the typical java property file, but it can come from any source as long as it is an input stream. It may be the system properties, local file, github, or S3.

| Config Loader         | Formats supported                       | details                                                                                                                                                                                                                                                                                                                                                                                                                                             | module                                                             |
|-----------------------|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| EnvironmentVarsLoader | envVars                                 | Loads Environment Variables from the EnvironmentConfigSource, it expects a list not a InputStream. By default, it splits the paths using a "_". You can also disable failOnErrors if you are receiving errors from the environment variables, as you can not always control what is present. By treating Errors as warnings it will not fail if it finds a configuration the parser doesn't understand. Instead it will ignore the specific config. | core                                                               | 
| MapConfigLoader       | mapConfig                               | Loads a user provided Map from the MapConfigSource, it expects a list not a InputStream. By default, it splits the paths using a "." and tokenizes arrays with a numeric index as "[0]".                                                                                                                                                                                                                                                            | core                                                               | 
| PropertyLoader        | properties, props, and systemProperties | Loads a standard property file from an InputStream. By default, it splits the paths using a "." and tokenizes arrays with a numeric index as "[0]".                                                                                                                                                                                                                                                                                                 | core                                                               |
| JsonLoader            | json                                    | Leverages Jackson to load json files and convert them into a ConfigNode tree.                                                                                                                                                                                                                                                                                                                                                                       | [`gestalt-json`](https://search.maven.org/search?q=gestalt-json)   |
| TomlLoader            | toml                                    | Leverages Jackson to load toml files and convert them into a ConfigNode tree.                                                                                                                                                                                                                                                                                                                                                                       | [`gestalt-toml`](https://search.maven.org/search?q=gestalt-toml)   |
| YamlLoader            | yml and yaml                            | Leverages Jackson to load yaml files and convert them into a ConfigNode tree.                                                                                                                                                                                                                                                                                                                                                                       | [`gestalt-yaml`](https://search.maven.org/search?q=gestalt-yaml)   |
| HoconLoader           | config                                  | Leverages com.typesafe:config to load hocon files, supports substitutions.                                                                                                                                                                                                                                                                                                                                                                          | [`gestalt-hocon`](https://search.maven.org/search?q=gestalt-hocon) |

If you didn't manually add any ConfigLoaders as part of the GestaltBuilder, it will add the defaults. The GestaltBuilder uses the service loader to create instances of the Config loaders. It will configure them by passing in the GestaltConfig to applyConfig.
To register your own default ConfigLoaders add them to the builder, or add it to a file in META-INF\services\org.github.gestalt.config.loader.ConfigLoader and add the full path to your ConfigLoader


# Decoders
| Type                | details                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Array               | Java primitive array type with any generic class, Can decode simple types from a single comma separated value, or from an array node. You can escape the comma with a \\, so the values are not split.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| BigDecimal          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| BigInteger          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Boolean             | Boolean and boolean                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| Byte                | Byte and byte                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| Char                | Char and char                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| Date                | takes a DateTimeFormatter as a parameter, by default it uses DateTimeFormatter.ISO_DATE_TIME                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| Double              | Double and double                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| Duration            |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Enum                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| File                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Float               | Float and float                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Instant             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Integer             | Integer and int                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| List                | a Java list with any Generic class, Can decode simple types from a single comma separated value, or from an array node. You can escape the comma with a \\, so the values are not split.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| LocalDate           | Takes a DateTimeFormatter as a parameter, by default it uses DateTimeFormatter.ISO_LOCAL_DATE                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| LocalDateTime       | Takes a DateTimeFormatter as a parameter, by default it uses DateTimeFormatter.ISO_DATE_TIME                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| Long                | Long or long                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| Map                 | A map, Assumes that the key is a simple class that can be decoded from a single string. ie a Boolean, String, Int. The value can be any type we can decode.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| Object              | Decodes a java Bean style class, although it will work with any java class.  Will fail if the constructor is private. Will construct the class even if there are missing values, the values will be null or the default. Then it will return errors which you can disable using treatMissingValuesAsErrors = true. Decodes member classes and lists as well.                                                                                                                                                                                                                                                                                                                                          |
| Optional            | Decodes an optional value, if no value is found it will return an Optional.empty()                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| OptionalDouble      | Decodes an optional Double, if no value is found it will return an OptionalDouble.empty()                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| OptionalInt         | Decodes an optional Integer, if no value is found it will return an OptionaInt.empty()                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| OptionalLong        | Decodes an optional Long, if no value is found it will return an OptionalLong.empty()                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Path                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Pattern             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Proxy (interface)   | Will create a proxy for an interface that will return the config value based on the java bean method name. So a method "getCar()" would match a config named "car". If a config is missing it will call the default method if provided. Has 2 modes, Cached and pass-through, the default is Cached. Cached  will receive a cache of all values on creation and return those from an internal cache. Pass-though will validate the object on creation, but when calling to get the values it will call gestalt for each value. This allows you to always get the most recent values. To set the mode on the builder use `Gestalt gestalt = builder.setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)` |
| Record              | Decodes a Java record. All members of the record must have a value or construction will fail.So unlike the Object decoder it will not have the option to default to null or provide defaults. Will construct the record even if there are extra values, it will ignore all extra values.                                                                                                                                                                                                                                                                                                                                                                                                              |
| SequencedCollection | A SequencedCollection with any Generic class, Can decode simple types from a single comma separated value, or from an array node. Provides a ArrayList.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| SequencedMap        | A map, Assumes that the key is a simple class that can be decoded from a single string. ie a Boolean, String, Int. The value can be any type we can decode. Provides a LinkedHashMap. Json, Toml, and Properties dont support sorted maps. Only Hocon supports sorted maps.                                                                                                                                                                                                                                                                                                                                                                                                                           |
| SequencedSet        | A SequencedSet with any Generic class, Can decode simple types from a single comma separated value, or from an array node. Provides an ordered LinkedHashSet.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| Set                 | A Set with any Generic class, Can decode simple types from a single comma separated value, or from an array node. Provides an unordered HashSet.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Short               | Short or short                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| String              |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| StringConstructor   | Will decode a class that has a constructor that accepts a single string. This will only match for leaf nodes. It will send the value of the leaf node to the String constructor.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| UUID                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |

For Kotlin, the kotlin specific decoders are only selected when calling from the Kotlin Gestalt extension functions, or when using KTypeCapture. Otherwise, it will match the Java decoder.
Kotlin decoders: Boolean, Byte, Char, Data class, Double, Duration, Float, Integer, Long, Short, String

For kotlin data classes it builds a Kotlin Data class by creating a map of parameters. If there are any missing required parameters it will fail.

Required parameters are ones that don't have a default and are not nullable. An exception will be thrown in this case.

If all members are optional, and we have no parameters we will try and create the class with the default PlaceHolder constructor.

If you didn't manually add any Decoders as part of the GestaltBuilder, it will add the defaults. The GestaltBuilder uses the service loader to create instances of the Decoders. It will configure them by passing in the GestaltConfig to applyConfig.
To register your own default Decoders add them to the builder, or add it to a file in META-INF\services\org.github.gestalt.config.decoder.Decoder and add the full path to your Decoder


# Reload Strategies
When adding a ConfigSource to the builder, if can you also add a reload strategy for the ConfigSource, when the source changes, or we receive an event to reload the config source Gestalt will get a notification and automatically attempt to reload the config.
Once Gestalt has reloaded the config it will send out its own Gestalt Core Reload event. you can add a listener to the builder to get a notification when a Gestalt Core Reload has completed. The Gestalt Cache uses this to clear the cache when a Config Source has changed.

```java
  ConfigSourcePackage devFileSource = FileConfigSourceBuilder.builder().setFile(devFile).addConfigReloadStrategy(new FileChangeReloadStrategy()).build();
  Gestalt gestalt = builder
  .addSource(devFileSource)
  .addCoreReloadListener(reloadListener)
  .build();
```

| Reload Strategy | Details |
| --------------- | ------- | 
| FileChangeReload | Specify a FileConfigSource, and the  FileChangeReload will listen for changes on that file. When the file changes it will tell Gestalt to reload the file. Also works with symlink and will reload if the symlink change.  |
| TimedConfigReloadStrategy | Provide a ConfigSource and a Duration then the Reload Strategy will reload every period defined by the Duration |

# Gestalt configuration
| Configuration                 | default  | Details                                                                                                                                                                                                                                                                                                          |
|-------------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| treatWarningsAsErrors         | false    | if we treat warnings as errors Gestalt will fail on any warnings. When set to true it overrides the behaviour in the below configs.                                                                                                                                                                              |
| treatMissingArrayIndexAsError | false    | By default Gestalt will insert null values into an array or list that is missing an index. By enabling this you will get an exception instead                                                                                                                                                                    |
| treatMissingValuesAsErrors    | false    | By default Gestalt will not update values in classes not found in the config. Null values will be left null and values with defaults will keep their defaults. By enabling this you will get an exception if any value is missing.                                                                               |
| treatNullValuesInClassAsErrors | true     | Prior to v0.20.0 null values and values not in the config but have a default in classes were treated the same. By enabling this you will get an exception if a value is null after decoding an object. If the value is missing but has a default this will be caught under the config treatMissingValuesAsErrors |
| dateDecoderFormat             | null     | Pattern for a DateTimeFormatter, if left blank will use the default for the decoder                                                                                                                                                                                                                              |
| localDateTimeFormat           | null     | Pattern for a DateTimeFormatter, if left blank will use the default for the decoder                                                                                                                                                                                                                              |
| localDateFormat               | null     | Pattern for a DateTimeFormatter, if left blank will use the default for the decoder                                                                                                                                                                                                                              |
| substitutionOpeningToken      | ${       | Customize what tokens gestalt looks for when starting replacing substrings                                                                                                                                                                                                                                       |
| substitutionClosingToken      | }        | Customize what tokens gestalt looks for when ending replacing substrings                                                                                                                                                                                                                                         |
| maxSubstitutionNestedDepth    | 5        | Get the maximum string substitution nested depth. If you have nested or recursive substitutions that go deeper than this it will fail.                                                                                                                                                                           |
| proxyDecoderMode              | CACHE    | Either CACHE or PASSTHROUGH, where cache means we serve results through a cache that is never updated or pass through where each call is forwarded to Gestalt to be looked up.                                                                                                                                   |

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


# Additional Modules


## Guice dependency injection.
Allow Gestalt to inject configuration directly into your classes using Guice using the `@InjectConfig` annotation on any class fields. This does not support constructor injection (due to Guice limitation)
To enable add the `new GestaltModule(gestalt)` to your Guice Modules, then pass in your instance of Gestalt.

See the [unit tests](gestalt-guice/src/test/java/org/github/gestalt/config/guice/GuiceTest.java) for examples of use.
```java
Injector injector = Guice.createInjector(new GestaltModule(gestalt));

  MyService service = injector.getInstance(MyService.class);

// use the InjectConfig along with the path to inject configuration. 
public static class MyService {
  @InjectConfig(path = "db.user") DBConnection connection;

  public DBConnection getConnection() {
    return connection;
  }
}
`````    


## Gestalt Kodein dependency injection
When you are using Kodein you can use it to inject your configurations directly into your objects.
By using the extension method `gestalt` within the scope of the Kodein DI DSL you can specify the path to your configurations, and it will automatically inject configurations into your object.

See the [unit tests](gestalt-kodein-di/src/test/kotlin/org/github/gestalt/config/kotlin/kodein/test/KodeinTest.kt) for examples of use.

```kotlin
  val kodein = DI {
  bindInstance { gestalt!! }
  bindSingleton { DBService1(gestalt("db")) }
  bindSingleton { DBService2(gestalt("db", DBInfoPOJO(port = 1000, password = "default"))) }
}

val dbService1 = kodein.direct.instance<DBService1>()
```


## Gestalt Koin dependency injection
When you are using Koin you can use it to inject your configurations directly into your objects.
By using the extension method `gestalt` within the scope of the koin module DSL you can specify the path to your configurations, and it will automatically inject configurations into your object.

See the [unit tests](gestalt-koin-di/src/test/kotlin/org/github/gestalt/config/kotlin/koin/test/KoinTest.kt) for examples of use.

```kotlin
  val koinModule = module {
  single { gestalt!! }
  single { DBService1(gestalt("db")) }
  single { DBService2(gestalt("db", DBInfoPOJO(port = 1000, password = "default"))) }
}

val myApp = koinApplication {
  modules(koinModule)
}

val dbService1: DBService1 = myApp.koin.get()
```

# Use Cases
## Dynamic overriding config values with command line arguments

Often you may wish to override a configuration value with a value provided on the command line. 
One way to do this is to add a `SystemPropertiesConfigSource` as the last source in Gestalt. This way it will have the highest priority and override all previous sources.

Then when running the project you provide the command line parameter -D<path.to.config=value>. This will override all other config sources with this value. 

In this example we provide a config source for default and dev, but allow for the overriding those with the system properties. 

with the property values
```properties
# default
http.pool.maxTotal=100
# dev
http.pool.maxTotal=1000
```

However, we override with a command line parameter of: `-Dhttp.pool.maxTotal=200`
```java
  // for this to work you need to set the following command line Options
  // -Dhttp.pool.maxTotal=200
  GestaltBuilder builder = new GestaltBuilder();
  Gestalt gestalt = builder
      .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.properties").build())
      .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
      .addSource(SystemPropertiesConfigSourceBuilder.builder().build())
      .build();

  // Load the configurations, this will throw exceptions if there are any errors.
  gestalt.loadConfigs();

  GestaltConfigTest.HttpPool pool = gestalt.getConfig("http.pool", GestaltConfigTest.HttpPool.class);
  
  Assertions.assertEquals(200, pool.maxTotal);
```

In the end we should get the value 200 based on the overridden command line parameter. 


# Example code
For more examples of how to use gestalt see the [gestalt-sample](https://github.com/gestalt-config/gestalt/tree/main/gestalt-examples/gestalt-sample/src/test) or for Java 17 + samples [gestalt-sample-java-latest](https://github.com/gestalt-config/gestalt/tree/main/gestalt-examples/gestalt-sample-java-latest/src/test)


# Architectural details
This section is more for those wishing to know more about how Gestalt works, or how to add their own functionality. If you only wish to get configuration from Gestalt As Is, then feel free to skip it.


## ConfigSource
A config source provides an interface for providing a configuration that a ConfigLoader can understand and parse into a config node. Each ConfigSource has a format that a specific ConfigLoader will understand. So a ConfigLoader that loads "property" files can load them from multiple sources. A ConfigSource can provide either a InputStream or list of pairs of paths and values.
You can write your own ConfigSource by implementing the interface and passing though the format that represents your source. For example, you could add a new URL ConfigSource that loads from a URL, depending on the file extension, has a different format.
Each source must have a unique ID, that Gestalt uses to keep track of the source, the config node tree built from the source and when reloading the id of the source.
```java
  /**
 * The format of the config source, for example this can be envVars, the extension of a file (properties, json, ect).
 *
 * @return The format of the config source
 */
  String format();

    /**
     * If this config source has a stream, this will return the stream of data.
     * Or if not supported it will throw an exception.
     *
     * @return input stream of data
     * @throws GestaltException if there are any IO or if this is an unsupported operation
     */
    InputStream loadStream() throws GestaltException;

    /**
     * provides a list of config values.
     *
     * @return provides a list of config values
     * @throws GestaltException if there are any IO or if this is an unsupported operation
     */
    List<Pair<String, String>> loadList() throws GestaltException;
```


## ConfigLoader
A ConfigLoader accepts a specific source format. It reads in the config source as either a list or input stream. It is then responsible for converting the sources into a ValidateOf with either a config node tree or validation errors.
You can write your own ConfigLoader by implementing the interface and accepting a specific format. Then read in the provided ConfigSource InputStream or list and parse the values. For example you can add a json loader that takes an InputStream and uses Jackson to load and build a config tree.
```java
  /**
 * True if the config loader accepts the format.
 *
 * @param format config format.
 * @return True if the config loader accepts the format.
 */
  boolean accepts(String format);

    /**
     * Load a ConfigSource then build the validated config node.
     *
     * @param source source we want to load with this config loader.
     * @return the validated config node.
     * @throws GestaltException any exceptions
     */
    ValidateOf<ConfigNode> loadSource(ConfigSource source) throws GestaltException;
```


## SentenceLexer
Gestalt uses a SentenceLexer's in several places, to convert a string path into tokens that can be followed and to in the ConfigParser to turn the configuration paths into tokens then into config nodes.
You can customize the SentenceLexer to use your own format of path. For example in Gestalt Environment Variables use a '_' to delimitate the tokens whereas property files use '.'. If you wanted to use camel case you could build a sentence lexer for that.


## Decoder
Decoders allow Gestalt to decode a config node into a specific value, class or collection. A Decoder can either work on a leaf and decode a single value, or it can work on a Map or Array node and decode a class or collection.
You can create your own decoder by implementing the Decoder interface. By returning true for the matches Gestalt will ask your decoder to decode the current node by calling your Decoders decode method. Gestalt will pass in the current path, the current node to decode and the DecoderService so we can decode any subnodes.
```java
  /**
 * true if this decoder matches the type capture.
 *
 * @param path           the current path
 * @param tags           the tags for the current request
 * @param node           the current node we are decoding.
 * @param type           the type of object we are decoding.
 * @return true if this decoder matches the type capture
 */
  boolean canDecode(path: String, tags: Tags, configNode:ConfigNode, TypeCapture<?> klass);

  /**
   * Decode the current node. If the current node is a class or list we may need to decode sub nodes.
   *
   * @param path the current path
   * @param node the current node we are decoding.
   * @param type the type of object we are decoding.
   * @param decoderService decoder Service used to decode members if needed. Such as class fields.
   * @return ValidateOf the current node with details of either success or failures.
   */
  ValidateOf<T> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService);
```


## ConfigReloadStrategy
You are able to reload a single source and rebuild the config tree by implementing your own ConfigReloadStrategy.


## ConfigNodeService
The ConfigNodeService is the central storage for the merged config node tree along with holding the original config nodes stored in a ConfigNodeContainer with the original source id. This is so when we reload a config source, we can link the source being reloaded with the config tree it produces.
Gestalt uses the ConfigNodeService to save, merge, validate the config tree, navigate and find the node Gestalt is looking for.


## Gestalt
The external facing portion of Java Config Library, it is the keystone of the system and is responsible for bringing together all the pieces of project. Since Gestalt relies on multiple services, the Builder makes it simple to construct a functional and default version of Gestalt.


### loadConfigs
The built Gestalt is used to load the config sources by adding them to the builder and then passed through to the Gestalt constructor.
Gestalt will use the ConfigLoaderService to find a ConfigLoader that will load the source by a format. It will add the config node tree loaded to the ConfigNodeService to be added with the rest of the config trees. The new config tree will be merged and where applicable overwrite any of the existing config nodes.


### reload
When a source needs to be reloaded, it will be passed into the reload function. The sources will then be converted into a Config node as in the loading. Then Gestalt will use the ConfigNodeService to reload the source. Since the ConfigNodeService holds onto the source ID with the ConfigNodeContainer we are able to determine with config node to reload then take all the config nodes and re-merge them in the same order to rebuild the config tree with the newly loaded node.


# Post Processors
To implement your own Post Processor you need to inherit from PostProcessor.

```java
/**
 * Interface for the Post Processing of Config nodes. This will be run against every node in the tree. 
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public interface PostProcessor {
  ValidateOf<ConfigNode> process(String path, ConfigNode currentNode);

  /**
   * Apply the GestaltConfig to the Post Processor. Needed when building via the ServiceLoader
   * It is a default method as most Post Processor don't need to apply configs.
   *
   * @param config GestaltConfig to update the Post Processor
   */
  default void applyConfig(GestaltConfig config) {
  }
}
```

When you write your own applyConfig method, each node of the config tree will be passed into the process method. You can either modify the current node or return it as is. The return value will be used to replace the tree, so if you return nothing your tree will be lost.
You can re-write any intermediate node or only modify the leaf nodes as TransformerPostProcessor does.
To register your own default PostProcessor, add it to a file in META-INF\services\org.github.gestalt.config.post.process.PostProcessor and add the full path to your PostProcessor.

The TransformerPostProcessor is a specific type of PostProcessor that allows you to replace strings in a leaf node that match ${transformer:key} into a config value. where the transformer is the name of a Transformer registered with the TransformerPostProcessor, such as in the above PostProcessor section with envMap, sys, and map. The key is a string lookup into the transformer.
To implement your own Transformer you need to implement the Transformer class.

```java
/**
 * Allows you to add your own custom source for the TransformerPostProcessor.
 * Whenever the TransformerPostProcessor sees a value ${name:key} the transform is selected that matches the same name
 */
public interface Transformer {
  /**
   * the name that will match the ${name:key} the transform is selected that matches the same name
   * @return
   */
  String name();

  /**
   * When a match is found for ${name:key} the key and the path are passed into the process method.
   * The returned value replaces the whole ${name:key}
   * @param path the current path
   * @param key the key to lookup int this transform.
   * @return the value to replace the ${name:key}
   */
  Optional<String> process(String path, String key);
}
```

To register your own default Transformer, add it to a file in META-INF\services\org.github.gestalt.config.post.process.transform.Transformer and add the full path to your Transformer.

the annotation @ConfigPriority(100), specifies the descending priority order to check your transformer when a substitution has been made without specifying the source ${key}
