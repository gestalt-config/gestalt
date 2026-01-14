---
sidebar_position: 1
---

## Dotenv Module

Gestalt provides a Dotenv module for loading configuration from a `.env` file using `DotenvConfigSource`. This module is built on top of the
[`dotenv-java`](https://github.com/cdimascio/dotenv-java) library.

To use the Dotenv module, add `gestalt-dotenv` to your build configuration.

The module supports:
- Loading a `.env` file as a Gestalt configuration source
- Using `.env` values in string substitution

---

### Loading Configuration from a `.env` File

To load configuration from a `.env` file, add a `DotenvConfigSource` to your `GestaltBuilder` using `DotenvSourceBuilder`.

`DotenvSourceBuilder` allows you to:
- Specify the `Dotenv` instance to use
- Apply a filter to control which variables are loaded
- Override the source format

By default, the `.env` file is loaded using the **environment variable** format, but it can also be loaded as a **properties** format.

### Dotenv String Substitution

To enable string substitution using values from a .env file, add a `DotenvModuleConfig` to your `GestaltBuilder` via `addModuleConfig`.

The `DotenvModuleConfig` can use the same Dotenv instance as the configuration source or a different one.

### Example

```java
  Dotenv dotenv = Dotenv.configure()
    .directory("src/test/resources")
    .filename(".env")
    .load();
  
  
  Gestalt gestalt = new GestaltBuilder()
    .addSource(DotenvSourceBuilder.builder().setDotenv(dotenv).setFilter(Dotenv.Filter.DECLARED_IN_ENV_FILE).build())
    .addModuleConfig(new DotenvModuleConfig(dotenv))
    .build();
  
  gestalt.loadConfigs();
```
