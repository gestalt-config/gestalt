---
sidebar_position: 6
---

## YAML Module

Gestalt provides a YAML module for loading configuration from YAML files. This module uses Jackson to parse YAML files and convert them into a ConfigNode tree.

To use the YAML module, add `gestalt-yaml` to your build configuration.

The module supports:
- Loading configuration from YAML files (.yml, .yaml)
- Parsing YAML from any InputStream source

---

### Loading Configuration from YAML Files

The YAML module provides a `YamlLoader` that can load YAML files from any config source that provides an InputStream.

### Example

```yaml
db:
  host: localhost
  port: 5432
  credentials:
    user: admin
    password: secret

servers:
  - host: server1
    port: 8080
  - host: server2
    port: 8081
```

```java
Gestalt gestalt = new GestaltBuilder()
  .addSource(FileConfigSourceBuilder.builder()
    .setFile(new File("config.yaml"))
    .build())
  .build();

gestalt.loadConfigs();

// Access the configuration
String dbHost = gestalt.getConfig("db.host", String.class);
int dbPort = gestalt.getConfig("db.port", Integer.class);
List<Map<String, Object>> servers = gestalt.getConfig("servers", new TypeCapture<List<Map<String, Object>>>() {});
```