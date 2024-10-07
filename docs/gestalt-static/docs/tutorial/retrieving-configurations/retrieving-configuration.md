---
sidebar_position: 1
---

# Retrieving a configuration

To retrieve a configuration from Gestalt we need the path to the configuration as well as what type of class.

### getConfig path options
Gestalt is **not case sensitive**. Since Gestalt interops between Environment Variables and other sources with various cases, all strings in Gestalt are normalized to a lower case.
By default, Gestalt uses dot notation and allows indexing into arrays using a '[0]'.  
If you want to use a different path style you can provide your own [custom lexer](#relaxed-path-parsing-to-support-all-case-paths) to Gestalt. The SentenceLexer is used to convert the path passed to the Gestalt getConfig interface into tokens that Gestalt can use to navigate to your sub node.

```java
  // load a whole class, this works best with pojo's
  HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);
  // or get a specific config value from a class
  short maxTotal  gestalt.getConfig("HTTP.pool.maxTotal", Short.class);
  // get with a default if you want a fallback from code
  long maxConnectionsPerRoute = gestalt.getConfig("http.Pool.maxPerRoute", 24, Long.class);

  // get a list of Host objects, or an PlaceHolder collection if there is no hosts found.
  LinkedList<Host> hosts = gestalt.getConfig("db.hosts", new LinkedList(), new TypeCapture<LinkedList<Host>>() {});

  // Get a class at a specific list index. 
  Host host = gestalt.getConfig("db.hosts[2]", Host.class);
  // get a value of a class from a specific list index.
  String password = gestalt.getConfig("db.hosts[2].password", String.class);
```

When decoding a path we do use path mappers to try different cases, but that only applies to the sub-tree starting at the path asked for. So if you call `gestalt.getConfig("http.pool", HttpPool.class)` it will try and map the cases for nodes under path `http.pool` but not the nodes in the path `http-pool`.


```java
  // given the record. 
  public record HttpPool(String poolSize, int timeout) {}
```
And the properties:
```properties
booking.service.pool.size = 10
booking-service.timeout = 10
```

When getting the config will fail, because `booking.service` will not match `booking-service` so it will not find the timeout.
```java
  HttpPool pool = gestalt.getConfig("booking.service", HttpPool.class);
```

But given the properties
```properties
booking.service.pool.size = 10
booking.service.timeout = 10
```

These calls will succeed. When decoding `poolSize` it will first try a lowercase match of `poolsize`, but will not find the node, so it will try `pool.size` for a combined path of `booking.service.pool.size` which it will find.
```java
  HttpPool pool = gestalt.getConfig("booking.service", HttpPool.class);
```

### Retrieving Primitive and boxed types
Getting primitive and boxed types involves calling Gestalt and providing the class of the type you are trying to retrieve.

```java
Short myShortWrapper = gestalt.getConfig("http.pool.maxTotal", Short.class);
short myShort = gestalt.getConfig("http.pool.maxTotal", short.class);
String serviceMode = gestalt.getConfig("serviceMode", String.class);
```

Gestalt will automatically decode and provide the value in the type you requested. 

