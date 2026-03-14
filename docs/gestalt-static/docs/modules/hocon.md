---
sidebar_position: 8
---

## HOCON Module

Gestalt provides a HOCON module for loading configuration from HOCON (Human-Optimized Config Object Notation) files. This module uses the Typesafe Config library to parse HOCON files and supports substitutions.

To use the HOCON module, add `gestalt-hocon` to your build configuration.

The module supports:
- Loading configuration from HOCON files (.config)
- Parsing HOCON from any InputStream source
- Variable substitutions within the configuration

---

### Loading Configuration from HOCON Files

The HOCON module provides a `HoconLoader` that can load HOCON files from any config source that provides an InputStream.

### Example

```hocon
db {
  host = localhost
  port = 5432
  credentials {
    user = admin
    password = secret
  }
}

servers = [
  { host = server1, port = 8080 },
  { host = server2, port = 8081 }
]

# Variable substitution
app {
  name = "MyApp"
  version = "1.0"
  fullName = ${app.name} ${app.version}
}
```

```java
Gestalt gestalt = new GestaltBuilder()
  .addSource(FileConfigSourceBuilder.builder()
    .setFile(new File("application.config"))
    .build())
  .build();

gestalt.loadConfigs();

// Access the configuration
String dbHost = gestalt.getConfig("db.host", String.class);
String fullName = gestalt.getConfig("app.fullName", String.class); // "MyApp 1.0"
```