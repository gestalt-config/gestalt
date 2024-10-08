---
sidebar_position: 4
---

# Kodein Dependency Injection
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
