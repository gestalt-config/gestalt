---
sidebar_position: 3
---

# Relaxed path parsing to support all case paths.
By default, we expect all paths to be dot notation. So in a properties file dot notation would look like `db.uri=my-sql.dev.myCompany.com` and produce a config tree with a map node `db` that has a map node `uri` with a value node `my-sql.dev.myCompany.com`.

For A properties file with `db.pool-size=10` the `db` path would translate into a map node, and the `pool-size` would also be a map node with a value node of `10`.  `pool-size` would not be translated into a path `pool` and `size`, but during decoding the [path mappers](#searching-for-path-while-decoding-objects) will attempt to map the variable `poolSize` to `pool-size`.
However, this only works for the nodes after the path we are looking for. It does not map nodes earlier in the path.
So `connection-pool.size=10` and `connection.pool.size=50` are two separate paths and will not be merged.

By modifying the delimiter in the default lexer, you can support converting snake, kebab and dot notation into similar paths. Where the lexer will split the path into tokens based on any of the `([._-])|(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[0-9])(?=[A-Z][a-z])|(?<=[a-zA-Z])(?=[0-9])`. Or split them based on CamelCase, Snake Case, Dot Notation, or Kebab Case.

```java
Map<String, String> configs = Map.of("db.uri", "test"); 
String json = "{\"db_port\":3306}";
String toml = "db-password = \"abc123\"";

SentenceLexer relaxedLexer = PathLexerBuilder.builder()
  .setDelimiter("([._-])|(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[0-9])(?=[A-Z][a-z])|(?<=[a-zA-Z])(?=[0-9])")
  .build();

Gestalt gestalt = new GestaltBuilder()
  .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
  .addSource(StringConfigSourceBuilder.builder().setConfig(json).setFormat("json").build())
  .addSource(StringConfigSourceBuilder.builder().setConfig(toml).setFormat("toml").build())
  .useCacheDecorator(false)
  // do not normalize the sentence return it as is.
  .setSentenceLexer(relaxedLexer)
  .build();
```

The Environment Variable Loader doesn't use the default lexer as it supports Screaming Snake Case by default, whereas the default lexer for the rest of Gestalt is dot notation.
To modify the Environment variable's lexer you need to register a `EnvironmentVarsLoaderModuleConfig` with the new lexer.

```properties
Gestalt gestalt = new GestaltBuilder()
  .addModuleConfig(EnvironmentVarsLoaderModuleConfigBuilder
    .builder()
    .setLexer(relaxedLexer)
    .build())
```
