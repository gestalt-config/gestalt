---
sidebar_position: 3
---

# Config Loader
A ConfigLoader accepts a specific source format. It reads in the config source as either a list or input stream. It is then responsible for converting the sources into a GResultOf with either a config node tree or validation errors.
You can write your own ConfigLoader by implementing the interface and accepting a specific format. Then read in the provided ConfigSource InputStream or list and parse the values. For example you can add a json loader that takes an InputStream and uses Jackson to load and build a config tree.
```java
  /**
 * True if the config loader accepts the format.
 *
 * @param format config format.
 * @return True if the config loader accepts the format.
 */
  boolean accepts(String format);

  /**
   * Load a ConfigSource then build the validated config node.
   *
   * @param source source we want to load with this config loader.
   * @return the validated config node.
   * @throws GestaltException any exceptions
   */
  GResultOf<ConfigNode> loadSource(ConfigSource source) throws GestaltException;
```

