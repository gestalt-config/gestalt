---
sidebar_position: 5
---

## JSON Module

Gestalt provides a JSON module for loading configuration from JSON files. This module uses Jackson to parse JSON files and convert them into a ConfigNode tree.

To use the JSON module, add `gestalt-json` to your build configuration.

The module supports:
- Loading configuration from JSON files
- Parsing JSON from any InputStream source

---

### Loading Configuration from JSON Files

The JSON module provides a `JsonLoader` that can load JSON files from any config source that provides an InputStream.

### Example

```json
{
  "db": {
    "host": "localhost",
    "port": 5432,
    "credentials": {
      "user": "admin",
      "password": "secret"
    }
  },
  "servers": [
    {"host": "server1", "port": 8080},
    {"host": "server2", "port": 8081}
  ]
}
```

```java
Gestalt gestalt = new GestaltBuilder()
  .addSource(FileConfigSourceBuilder.builder()
    .setFile(new File("config.json"))
    .build())
  .build();

gestalt.loadConfigs();

// Access the configuration
String dbHost = gestalt.getConfig("db.host", String.class);
int dbPort = gestalt.getConfig("db.port", Integer.class);
List<Map<String, Object>> servers = gestalt.getConfig("servers", new TypeCapture<List<Map<String, Object>>>() {});
```