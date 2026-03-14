---
sidebar_position: 4
---

## Google Cloud Module

Gestalt provides a Google Cloud module for loading configuration from Google Cloud services using various config sources. This module integrates with Google Cloud SDK to provide seamless configuration loading from cloud services.

To use the Google Cloud module, add `gestalt-google-cloud` to your build configuration.

The module supports:
- Loading configuration from Google Cloud Storage (GCS)
- Using Google Cloud services in configuration sources

---

### Loading Configuration from Google Cloud Storage

To load configuration from Google Cloud Storage, use the `GCSConfigSource` in your `GestaltBuilder`.

You can provide a custom `Storage` client or use the default instance.

### Example

```java
// Using default Storage client
Gestalt gestalt = new GestaltBuilder()
  .addSource(GCSConfigSourceBuilder.builder()
    .setBucketName("my-config-bucket")
    .setObjectName("config.properties")
    .build())
  .build();

// Or provide a custom Storage client
Storage storage = StorageOptions.newBuilder()
  .setProjectId("my-project")
  .build()
  .getService();

Gestalt gestalt2 = new GestaltBuilder()
  .addSource(GCSConfigSourceBuilder.builder()
    .setStorage(storage)
    .setBucketName("my-config-bucket")
    .setObjectName("config.properties")
    .build())
  .build();

gestalt.loadConfigs();
```