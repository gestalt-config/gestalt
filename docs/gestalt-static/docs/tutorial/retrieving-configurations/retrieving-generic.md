---
sidebar_position: 3
---

# Retrieving Interfaces and Generics

# Retrieving Interfaces
To get an interface you need to pass in the interface class for gestalt to return.
Gestalt will use a proxy object when requesting an interface. When you call a method on the proxy it will look up the similarly named property, decode and return it.

```java
iHttpPool pool = gestalt.getConfig("http.pool", iHttpPool.class);
```

### Retrieving Generic objects
To get an interface you need to pass in a TypeCapture with the Generic value of the class for gestalt to return.
Gestalt supports getting Generic objects such as Lists, Maps or Sets. However, due to type erasure we need to capture the type using the TypeCapture class. The Generic can be any type Gestalt supports decoding such as a primitive wrapper or an Object.

```java
List<HttpPool> httpPoolList = gestalt.getConfig("http.pool", new TypeCapture<>() { });
var httpPoolMap = gestalt.getConfig("http.pool", new TypeCapture<Map<String, HttpPool>>() { });
```

Gestalt supports multiple varieties of List such as AbstractList, CopyOnWriteArrayList, ArrayList, LinkedList, Stack, Vector, and SequencedCollection. If asked for a List it will default to an ArrayList.
Gestalt supports multiple varieties of Maps such as HashMap, TreeMap, ArrayList, LinkedHashMap and SequencedMap. If asked for a Map it will default to an HashMap.
Gestalt supports multiple varieties of Sets such as HashSet, TreeSet, LinkedHashSet, LinkedHashMap and SequencedSet. If asked for a Set it will default to an HashSet.

#### Config Data Type
For non-generic classes you can use the interface that accepts a class `HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);`, for Generic classes you need to use the interface that accepts a TypeCapture `List<HttpPool> pools = gestalt.getConfig("http.pools", Collections.emptyList(),
new TypeCapture<List<HttpPool>>() {});` to capture the generic type. This allows you to decode Lists, Sets and Maps with a generic type.

There are multiple ways to get a configuration with either a default, an Optional or the straight value. With the default and Optional Gestalt will not throw an exception if there is an error, instead returning a default or an PlaceHolder Option and log any warnings or errors.
