---
sidebar_position: 2
---

# Node Substitution
Using the `$include` keyword as part of a config path, you can include the referenced config node tree into the path provided. By default, the node is merged into the provided node under the current node as defaults that will be overridden. You can control the order of the nodes, by including a number where < 0 is included below the current node and > 0 is included above the current node. The root node is always 0. Having two nodes share the same order is undefined. For example: `$include:-1` for included under the current node, and `$include:1` for included over the current node.
If you are included multiple nodes each node must have an order, or the results are undefined, and some includes may be lost.

You can include into the root or any sub node. It also supports nested include.

The include node must provide a source that is used to determine how to include the source. Each source accepts different parameters that can be provided in the form of a key value with a comma separated list. One of the key value pairs must be `source` that is used to determine the source type.
For example a classPath source with the resource `includes.properties` would look like:

```properties
$include=source=classPath,resource=includes.properties
```

Example of include a classPath Node into a sub path with properties file `imports.properties`.
```properties
b=b changed
c=c
```

In the first example we include the loaded file node with default settings of order -1 `$include:-1`, where the root node is always order 0. So the node will be loaded under the current root nodes so will provide defaults that will be overwritten.
```java
  Map<String, String> configs = new HashMap<>();
  configs.put("a", "a");
  configs.put("b", "b");
  configs.put("$include", "source=classPath,resource=includes.properties");
  
 
  Gestalt gestalt = new GestaltBuilder()
      .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
      .build();
  
  gestalt.loadConfigs();
  
  Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
  Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
  Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
```
That is why we don't see `b=b changed` as it will be overwritten by `b=b`, but we still see `c=c` as it was in the included defaults and not overwritten.


In this second example we include the node with `$include:1`. Since the root node is always order 0, the included nodes will override the root.
```java
  Map<String, String> configs = new HashMap<>();
  configs.put("a", "a");
  configs.put("b", "b");
  configs.put("$include:1", "source=classPath,resource=includes.properties");

  Gestalt gestalt = new GestaltBuilder()
      .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
      .build();

  gestalt.loadConfigs();

  Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
  Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
  Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
```
That is why we see `b=b changed` as it is overwritten the root `b=b`.


In the final example, we include the loaded file node in the sub path `sub`.
```java
  Map<String, String> configs = new HashMap<>();
  configs.put("a", "a");
  configs.put("b", "b");
  configs.put("sub.a", "a");
  configs.put("sub.$include:1", "source=classPath,resource=includes.properties");
  
  Gestalt gestalt = new GestaltBuilder()
    .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
    .build();
  
  gestalt.loadConfigs();
  
  Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
  Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
  Assertions.assertEquals("a", gestalt.getConfig("sub.a", String.class));
  Assertions.assertEquals("b changed", gestalt.getConfig("sub.b", String.class));
  Assertions.assertEquals("c", gestalt.getConfig("sub.c", String.class));
```
As you can see the nodes from the file `includes.properties` were included in the sub path `sub`. As can bee seen with `sub.b = b changed` and `sub.c = c`.


Supported substitution sources:

| Source Type | Module               | Parameter          | Description                                                                                                                                                                                                                                                                                     |
|-------------|----------------------|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| classPath   | gestalt-core         | resource           | The name of the classpath resource to load.                                                                                                                                                                                                                                                     |
| node        | gestalt-core         | path               | Load an node at the given path into the current node.                                                                                                                                                                                                                                           |
| env         | gestalt-core         | failOnErrors       | If we should fail on errors. Since Env Vars may not always conform to Gestalt expectations we can disable the errors and make it more lenient while loading Env Vars.                                                                                                                           |
|             |                      | prefix             | Only include Env Vars that match the prefix.                                                                                                                                                                                                                                                    |
|             |                      | ignoreCaseOnPrefix | When matching the prefix should it ignore case.                                                                                                                                                                                                                                                 |
|             |                      | removePrefix       | If we should remove the prefix after matching.                                                                                                                                                                                                                                                  |
| file        | gestalt-core         | file               | Load a file at a given location to the current node.                                                                                                                                                                                                                                            |
|             |                      | path               | Load a file as a path at a given location to the current node.                                                                                                                                                                                                                                  |
| k8Secret    | gestalt-core         | path               | The directory to scan for kubernetes secrets.                                                                                                                                                                                                                                                   |
|             |                      | file               | The file directory to scan for kubernetes secrets.                                                                                                                                                                                                                                              |
| system      | gestalt-core         | failOnErrors       | If we should fail on errors. Since System Variables may not always conform to Gestalt expectations we can disable the errors and make it more lenient while loading System Vars.                                                                                                                |
| s3          | gestalt-aws          | Module Config      | To use S3 with the include node feature you must register an `S3Client` via the AWSModuleConfig: ```Gestalt gestalt = builder.addModuleConfig(AWSBuilder.builder().setRegion("us-east-1").setS3Client(s3Client).build()).build();```                                                            |
|             |                      | bucket             | The S3 bucket to search in.                                                                                                                                                                                                                                                                     |
|             |                      | key                | The Key of the config file to load.                                                                                                                                                                                                                                                             |
| blob        | gestalt-azure        | Module Config      | To use Azure Blob with the include node feature you must register an `BlobClient` or a `StorageSharedKeyCredential` via the AzureModuleBuilder : ```Gestalt gestalt = builder.addModuleConfig(AzureModuleBuilder.builder().setBlobClient(blobClient).build())).build();```                      |
|             |                      | endpoint           | Azure endpoint to access the blob storage.                                                                                                                                                                                                                                                      |
|             |                      | container          | Azure Container containing the blob.                                                                                                                                                                                                                                                            |
|             |                      | blob               | The blob with the file.                                                                                                                                                                                                                                                                         |
| git         | gestalt-git          | Module Config      | When accessing private repos you must register the `GitModuleConfig` with Gestalt. ```Gestalt gestalt = new GestaltBuilder().addModuleConfig(GitModuleConfigBuilder.builder().setCredentials(new UsernamePasswordCredentialsProvider(userName, password)).build()).build(); ```                 |
|             |                      | repoURI            | Where to locate the repo                                                                                                                                                                                                                                                                        |
|             |                      | branch             | What branch to find the config files.                                                                                                                                                                                                                                                           |
|             |                      | configFilePath     | The subpath in the repo URI to find the config file.                                                                                                                                                                                                                                            |
|             |                      | localRepoDirectory | Where to save the git files Gestalt Syncs.                                                                                                                                                                                                                                                      |
| gcs         | gestalt-google-cloud | Module Config      | To use GCS with the include node feature you can register a `Storage` client via the GoogleModuleConfig : ```Gestalt gestalt = builder.addModuleConfig(GoogleModuleConfigBuilder.builder().setStorage(storage).build())).build();```. Otherwise it will fallback to the default storage client. |
|             |                      | bucketName         | What bucket to find the config files.                                                                                                                                                                                                                                                           |
|             |                      | objectName         | The specific config file to include.                                                                                                                                                                                                                                                            |
