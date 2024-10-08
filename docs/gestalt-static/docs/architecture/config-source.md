---
sidebar_position: 2
---

# Config Source

A config source provides an interface for providing a configuration that a ConfigLoader can understand and parse into a config node. Each ConfigSource has a format that a specific ConfigLoader will understand. So a ConfigLoader that loads "property" files can load them from multiple sources. A ConfigSource can provide either a InputStream or list of pairs of paths and values.
You can write your own ConfigSource by implementing the interface and passing though the format that represents your source. For example, you could add a new URL ConfigSource that loads from a URL, depending on the file extension, has a different format.
Each source must have a unique ID, that Gestalt uses to keep track of the source, the config node tree built from the source and when reloading the id of the source.
```java
  /**
 * The format of the config source, for example this can be envVars, the extension of a file (properties, json, ect).
 *
 * @return The format of the config source
 */
  String format();

  /**
   * If this config source has a stream, this will return the stream of data.
   * Or if not supported it will throw an exception.
   *
   * @return input stream of data
   * @throws GestaltException if there are any IO or if this is an unsupported operation
   */
  InputStream loadStream() throws GestaltException;

  /**
   * provides a list of config values.
   *
   * @return provides a list of config values
   * @throws GestaltException if there are any IO or if this is an unsupported operation
   */
  List<Pair<String, String>> loadList() throws GestaltException;
```
