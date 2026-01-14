---
sidebar_position: 5
---

# Koin Dependency Injection
When you are using Koin you can use it to inject your configurations directly into your objects.
By using the extension method `gestalt` within the scope of the koin module DSL you can specify the path to your configurations, and it will automatically inject configurations into your object.

See the [unit tests](https://github.com/gestalt-config/gestalt/blob/main/gestalt-koin-di/src/test/kotlin/org/github/gestalt/config/kotlin/koin/test/KoinTest.kt) for examples of use.

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
