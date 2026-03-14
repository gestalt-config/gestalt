---
sidebar_position: 2
---

## AWS Module

Gestalt provides an AWS module for loading configuration from AWS services using various config sources. This module integrates with AWS SDK to provide seamless configuration loading from cloud services.

To use the AWS module, add `gestalt-aws` to your build configuration.

The module supports:
- Loading configuration from AWS S3 buckets
- Loading secrets from AWS Secrets Manager
- Using AWS services in configuration sources

---

### Loading Configuration from AWS S3

To load configuration from an S3 bucket, use the `S3ConfigSource` in your `GestaltBuilder`.

You need to provide:
- The S3 bucket name
- The S3 object key
- Optional AWS region and credentials

### Loading Secrets from AWS Secrets Manager

The AWS module also supports loading secrets from AWS Secrets Manager as part of your configuration.

### Example

```java
S3Client s3Client = S3Client.builder().build();

Gestalt gestalt = new GestaltBuilder()
  .addSource(S3ConfigSourceBuilder.builder()
    .setS3(s3Client)
    .setBucketName("my-config-bucket")
    .setKeyName("config.properties")
    .build())
  .build();

gestalt.loadConfigs();
```