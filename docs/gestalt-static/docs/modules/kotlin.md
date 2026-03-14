---
sidebar_position: 9
---

## Kotlin Module

Gestalt provides a Kotlin module that adds Kotlin-specific extensions and utilities for easier configuration loading in Kotlin projects.

To use the Kotlin module, add `gestalt-kotlin` to your build configuration.

The module supports:
- Reified generic functions for type-safe configuration retrieval
- Kotlin reflection-based type capture
- Nullable type handling

---

### Using Kotlin Extensions

The Kotlin module provides inline reified functions that automatically capture generic types at compile time, eliminating the need for explicit `TypeCapture` instances.

### Example

```kotlin
import org.github.gestalt.config.kotlin.getConfig

val gestalt = GestaltBuilder()
  .addSource(ClassPathConfigSourceBuilder.builder().setResource("/config.properties").build())
  .build()

gestalt.loadConfigs()

// Using reified generics - no need for TypeCapture
val dbHost: String = gestalt.getConfig("db.host")
val dbPort: Int = gestalt.getConfig("db.port")
val servers: List<Map<String, Any>> = gestalt.getConfig("servers")

// Nullable types are handled automatically
val optionalFeature: String? = gestalt.getConfig("optional.feature")

// With default values
val timeout: Int = gestalt.getConfig("timeout", 30)
```