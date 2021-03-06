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
- **Receive all errors up front:** When there is an error with your config, you will receive multiple errors in a friendly log. So you can fix multiple errors at once instead of one at a time waiting for the next error. 
- **Modular support for features** Only include what you need into your build, so if you dont need the Kotlin module, dont include it. 

# Getting Started
1. Add the Bintray repository:
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
Or
```kotlin
testImplementation("com.github.gestalt-config:gestalt-core:$version")
testImplementation("com.github.gestalt-config:gestalt-kotlin:$version")
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
  long maxConnectionsPerRoute = gestalt.getConfig("http.pool.maxPerRoute", 24, Long.class);


  // get a list of objects, or an empty collection if there is no hosts found.
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
  // get a list of objects, or an empty collection if there is no hosts found.
  val hosts: List<Host> = gestalt.getConfig("db.hosts", emptyList())
```   

# Gestalt getConfig path options
Gestalt is **not case sensitive**. Since Gestalt interops between Environment Variables and other sources with various cases, all strings in Gestalt are normalized to a lower case. 
Gestalt uses a SentenceLexer provided by the builder to convert the path passed to the Gestalt getConfig interface into tokens that Gestalt can use to navigate to your sub node. The default SentenceLexer supports paths seperated by the '.' and indexing into arrays using a '[0]' format. 
If you want to use a different path style you can provide your own SentenceLexer to Gestalt.

```java
  // load a whole class, this works best with pojo's
  HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);
  // or get a specific config value from a class
  short maxTotal  gestalt.getConfig("HTTP.pool.maxTotal", Short.class);
  // get with a default if you want a fallback from code
  long maxConnectionsPerRoute = gestalt.getConfig("http.Pool.maxPerRoute", 24, Long.class);

  // get a list of Host objects, or an empty collection if there is no hosts found.
  List<Host> hosts = gestalt.getConfig("db.hosts", Collections.emptyList(), 
    new TypeCapture<List<Host>>() {});

  // Get a class at a specific list index. 
  Host host = gestalt.getConfig("db.hosts[2]", Host.class);
  // get a value of a class from a specific list index.
  String password = gestalt.getConfig("db.hosts[2].password", String.class);
```

# Kotlin
Using the GestaltBuilder to create the Gestalt library you should use the addDefaultDecodersAndKotlin() extension method to ensure we add all Kotlin specific Decoders.  This method will also add the default Java decoders as well. 

```kotlin
  val gestalt = builder
    .addSource(FileConfigSource(defaultFile))
    .addDefaultDecodersAndKotlin()
    .build()
```

For Kotlin Gestalt includes several extension methods that allow easier use of Gestalt by way of reified functions to better capture the generic type information. 
Using the extension functions you don't need to specify the type if the return type has enough information to be inferred.
```kotlin
  val pool: HttpPool = gestalt.getConfig("http.pool")
  val hosts: List<Host> = gestalt.getConfig("db.hosts", emptyList())
```
# Config Sources
Adding a ConfigSource to the builder is the minimum step needed to build the Gestalt Library. 
You can add several ConfigSources to the builder and Gestalt, they will be loaded in the order they are added. Where each new source will be merged with the existing source and where applicable overwrite the values of the previous sources. 

```java
  Gestalt gestalt = builder
    .addSource(new FileConfigSource(devFile))
    .addSource(new EnvironmentConfigSource())
    .build();
```
In the above example we first load a file devFile then overwrite any values from the Environment Variables. 

| Config Source | Details |
| --------------- | ------- | 
| ClassPathConfigSource | Load a file from the java class path. Uses getResourceAsStream to find and load the InputStream. |
| EnvironmentConfigSource | Loads all Environment Variables in the system, will convert them to a list of key values from the Env Map for the config loader. |
| FileConfigSource | Loads a file from the local file system. The format for the source will depend on the file extension of the file. For example if it is dev.properties, the format will be properties. Returns a InpuStream for the config loader.  |
| GitConfigSource | Syncs a remote repo locally then uses the files to build a configuration. This uses jgit and supports several forms of authentication. See GitConfigSourceTest.java for examples of use. |
| MapConfigSource | Allows you to pass in your own map, it will convert the map into a list of path and value for the config loader. |
| StringConfigSource | Takes any string and converts it into a InputStream. You must also provide the format type so we can match it to a loader. |
| SystemPropertiesConfigSource | Loads the Java System Properties and convert them to a list of key values or the config loader. |
| S3ConfigSource | Loads a config source from AWS S3, Must include package com.github.gestalt-config:gestalt-s3:version. |
| URLConfigSource | Loads a config source from a URL. |

# Config Loader
Each config loader understands how to load a specific type of config. Often this is associated with a specific ConfigSource. For example the EnvironmentVarsLoader only loads the EnvironmentConfigSource. However, some loaders expect a format of the config, but accept it from multiple sources. For example the PropertyLoader expects the typical java property file, but it can come from any source as long as it is an input stream. It may be the system properties, local file, github, or S3.   

| Config Loader | Formats supported | details |
| ------------- | ----------------- | ------- |
| EnvironmentVarsLoader | envVars | Loads Environment Variables from the EnvironmentConfigSource, it expects a list not a InputStream. By default, it splits the paths using a "_". You can also enable treatErrorsAsWarnings if you are receiving errors from the environment variables, as you can not always control what is present. By treating Errors as warnings it will not fail if it finds a configuration the parser doesn't understand. Instead it will ignore the specific config. |
| MapConfigLoader | mapConfig | Loads a user provided Map from the MapConfigSource, it expects a list not a InputStream. By default, it splits the paths using a "." and tokenizes arrays with a numeric index as "[0]". |
| PropertyLoader | properties, props, and systemProperties  | Loads a standard property file from an InputStream. By default, it splits the paths using a "." and tokenizes arrays with a numeric index as "[0]". |
| JsonLoader | json | Leverages Jackson to load json files and convert them into a ConfigNode tree. Must include package com.github.gestalt-config:gestalt-json:version. |
| YamlLoader | yml and yaml | Leverages Jackson to load yaml files and convert them into a ConfigNode tree. Must include package com.github.gestalt-config:gestalt-yaml:version. |
| HoconLoader | config | Leverages com.typesafe:config to load hocon files, supports substitutions. Must include package com.github.gestalt-config:gestalt-hocon:version. |

If you didn't manually add any ConfigLoaders as part of the GestaltBuilder, it will add the defaults. The GestaltBuilder uses the service loader to create instances of the Config loaders. It will configure them by passing in the GestaltConfig to applyConfig. 
To register your own default add it to a file in META-INF\services\org.github.gestalt.config.loader.ConfigLoader and add the full path to your ConfigLoader 

# Decoders
| Type | details |
| ---- | ------- |
| Array | Java primitive array type with any generic class, Can decode simple types from a single comma separated value, or from an array node |
| BigDecimal | |
| BigInteger| |
| Boolean | Boolean and boolean |
| Byte | Byte and byte |
| Char | Char and char |
| Date | takes a DateTimeFormatter as a parameter, by default it uses DateTimeFormatter.ISO_DATE_TIME |
| Double | Double and double |
| Duration | |
| Enum | |
| File | |
| Float | Float and float |
| Instant | |
| Integer | Integer and int |
| List | a Java list with any Generic class, Can decode simple types from a single comma separated value, or from an array node |
| LocalDate | Takes a DateTimeFormatter as a parameter, by default it uses DateTimeFormatter.ISO_LOCAL_DATE |
| LocalDateTime | Takes a DateTimeFormatter as a parameter, by default it uses DateTimeFormatter.ISO_DATE_TIME |
| Long | Long or long |
| Map | A map, Assumes that the key is a simple class that can be decoded from a single string. ie a Boolean, String, Int. The value can be any type we can decode. |
| Object | Decodes a java Bean style class, although it will work with any java class.  Will fail if the constructor is private. Will construct the class even if there are missing values, the values will be null or the default. Then it will return errors which you can disable using treatMissingValuesAsErrors = true. Decodes member classes and lists as well. |
| Path | |
| Pattern | |
| Set | a Java list with any Generic class, Can decode simple types from a single comma separated value, or from an array node  |
| Short | Short or short |
| String | |
| UUID | |

For Kotlin, it has the following decoders. The decoders are only selected when calling from the Kotlin Gestalt extension function, or when using KTypeCapture. Otherwise, will match the Java Boolean
Kotlin decoders: Boolean, Byte, Char, Data, Double, Duration, Float, Integer, Long, Short, String

For kotlin data classes it builds a Kotlin Data class by creating a map of parameters. If there are any missing required parameters it will fail.

Required parameters are ones that don't have a default and are not nullable. An exception will be thrown in this case.

If all members are optional, and we have no parameters we will try and create the class with the default empty constructor.

If you didn't manually add any Decoders as part of the GestaltBuilder, it will add the defaults. The GestaltBuilder uses the service loader to create instances of the Decoders. It will configure them by passing in the GestaltConfig to applyConfig.
To register your own default add it to a file in META-INF\services\org.github.gestalt.config.decoder.Decoder and add the full path to your Decoder

# Reload Strategies
When adding a ConfigSource to the builder, if can you also add a reload strategy for the ConfigSource, when the source changes, or we receive an event to reload the config source Gestalt will get a notification and automatically attempt to reload the config. 
Once Gestalt has reloaded the config it will send out its own Gestalt Core Reload event. you can add a listener to the builder to get a notification when a Gestalt Core Reload has completed. The Gestalt Cache uses this to clear the cache when a Config Source has changed.  

```java
  ConfigSource devFileSource = new FileConfigSource(devFile);
  Gestalt gestalt = builder
    .addSource(devFileSource)
    .addReloadStrategy(new FileChangeReloadStrategy(devFileSource))
    .addCoreReloadListener(reloadListener)
    .build();
```

| Reload Strategy | Details |
| --------------- | ------- | 
| FileChangeReload | Specify a FileConfigSource, and the  FileChangeReload will listen for changes on that file. When the file changes it will tell Gestalt to reload the file. Also works with symlink and will reload if the symlink change.  |
| TimedConfigReloadStrategy | Provide a ConfigSource and a Duration then the Reload Strategy will reload every period defined by the Duration |

# Gestalt configuration

| Configuration | default | Details |
| ------------- | ------- | ------- | 
| treatWarningsAsErrors | false | if we treat warnings as errors Gestalt will fail on any warnings |
| treatMissingArrayIndexAsError | false | By default Gestalt will insert null values into an array or list that is missing an index. By enabling this you will get an exception instead |
| treatMissingValuesAsErrors | false | By default Gestalt will insert null values into an object. By enabling this you will get an exception instead | 
| envVarsTreatErrorsAsWarnings | false | Since Environment Variables are sometimes hard to control, there may be un-correctable errors while parsing. This disables errors while parsing, instead it will make a best attempt. If there are any errors on a path it will ignore the path. So use with caution. |
| dateDecoderFormat | null | Pattern for a DateTimeFormatter, if left blank will use the default for the decoder |
| localDateTimeFormat | null | Pattern for a DateTimeFormatter, if left blank will use the default for the decoder |
| localDateFormat | null | Pattern for a DateTimeFormatter, if left blank will use the default for the decoder |

# Example code
For more examples of how to use gestalt see the [gestalt-sample](https://github.com/credmond-git/gestalt/tree/main/gestalt-sample/src/test)

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
   * @param klass TypeCapture we are looking for a decoder.
   * @return true if this decoder matches the type capture
   */
  boolean matches(TypeCapture<?> klass);

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

When a source needs to be reloaded, it will be passed into the reload function. The sources will then be converted into a Config node as in the loading. Then Gestalt will use the ConfigNodeService to reload the source. Since the ConfigNodeService holds onto the source ID with the ConfigNodeContainer we are able to determine with config node to reload then take all the config nodes and re-merge them in the same order to rebuild teh config tree with the newly loaded node.

### getConfig

To get a config Gestalt needs to know what type of config to get. For simple classes you can use the interface for classes, for Generic classes you need to use the `new TypeCapture<List<Host>>() {}` to capture the generic type. This allows you to decode Lists, and Sets with a generic type. 
There are multiple ways to get a config with either a default, an Optional or the straight value. With the default and Optional Gestalt will not throw an exception if there is an error, instead returning a default or an empty Option.
Gestal uses the SentenceLexer provided by the builder to tokenize the path then use the ConfigNodeService to navigate to the node. With the node Gestalt calls the decoderService to convert the node into the appropriate type.  
