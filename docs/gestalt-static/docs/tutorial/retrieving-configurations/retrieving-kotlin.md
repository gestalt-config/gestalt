---
sidebar_position: 4
---

# Kotlin
For Kotlin Gestalt includes several extension methods that allow easier use of Gestalt by way of reified functions to better capture the generic type information.
Using the extension functions you don't need to specify the type if the return type has enough information to be inferred. If nothing is found it will throw a GestaltException unless the type is nullable, then it will return null.
```kotlin
  val pool: HttpPool = gestalt.getConfig("http.pool")
  val hosts: List<Host> = gestalt.getConfig("db.hosts", emptyList())
```
| Gestalt Version  | Kotlin Version |
|------------------|----------------|
| 0.35.0 +         | 2.1            |
| 0.25.0 +         | 1.9            |
| 0.17.0 +         | 1.8            |
| 0.13.0 to 0.16.6 | 1.7            |
| 0.10.0 to 0.12.0 | 1.6            |
| 0.9.0 to 0.9.3   | 1.5            |
| 0.1.0 to 0.8.1   | 1.4            |


# Retrieving Kotlin
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
