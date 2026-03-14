---
sidebar_position: 7
---

## TOML Module

Gestalt provides a TOML module for loading configuration from TOML files. This module uses Jackson to parse TOML files and convert them into a ConfigNode tree.

To use the TOML module, add `gestalt-toml` to your build configuration.

The module supports:
- Loading configuration from TOML files
- Parsing TOML from any InputStream source

---

### Loading Configuration from TOML Files

The TOML module provides a `TomlLoader` that can load TOML files from any config source that provides an InputStream.

### Example

```toml
[db]
host = "localhost"
port = 5432

[db.credentials]
user = "admin"
password = "secret"

[[servers]]
host = "server1"
port = 8080

[[servers]]
host = "server2"
port = 8081
```

```java
Gestalt gestalt = new GestaltBuilder()
  .addSource(FileConfigSourceBuilder.builder()
    .setFile(new File("config.toml"))
    .build())
  .build();

gestalt.loadConfigs();

// Access the configuration
String dbHost = gestalt.getConfig("db.host", String.class);
int dbPort = gestalt.getConfig("db.port", Integer.class);
List<Map<String, Object>> servers = gestalt.getConfig("servers", new TypeCapture<List<Map<String, Object>>>() {});
```