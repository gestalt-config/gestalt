---
sidebar_position: 3
---

## Azure Module

Gestalt provides an Azure module for loading configuration from Azure services using various config sources. This module integrates with Azure SDK to provide seamless configuration loading from cloud services.

To use the Azure module, add `gestalt-azure` to your build configuration.

The module supports:
- Loading configuration from Azure Blob Storage
- Using Azure services in configuration sources

---

### Loading Configuration from Azure Blob Storage

To load configuration from Azure Blob Storage, use the `BlobConfigSource` in your `GestaltBuilder`.

You can provide either a pre-configured `BlobClient` or specify the connection details (endpoint, credentials, container name, blob name) and the builder will create the client for you.

### Example

```java
// Using a pre-configured BlobClient
BlobClient blobClient = new BlobClientBuilder()
  .endpoint("https://myaccount.blob.core.windows.net")
  .credential(new StorageSharedKeyCredential("accountName", "accountKey"))
  .containerName("my-container")
  .blobName("config.properties")
  .buildClient();

Gestalt gestalt = new GestaltBuilder()
  .addSource(BlobConfigSourceBuilder.builder()
    .setBlobClient(blobClient)
    .build())
  .build();

// Or specify details and let the builder create the client
Gestalt gestalt2 = new GestaltBuilder()
  .addSource(BlobConfigSourceBuilder.builder()
    .setEndpoint("https://myaccount.blob.core.windows.net")
    .setCredential(new StorageSharedKeyCredential("accountName", "accountKey"))
    .setContainerName("my-container")
    .setBlobName("config.properties")
    .build())
  .build();

gestalt.loadConfigs();
```