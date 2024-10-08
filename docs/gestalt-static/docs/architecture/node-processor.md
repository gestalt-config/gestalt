---
sidebar_position: 6
---

# Config Node Processors
To implement your own Config Node Processor you need to inherit from ConfigNodeProcessor.

```java
/**
 * Interface for the Config Node Processing. This will be run against every node in the tree.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface ConfigNodeProcessor {

  /**
   * run the config node process the current node. You need to return a node, so if your config node processor does nothing to the node
   * return the original node.
   *
   * @param path        current path
   * @param currentNode current node to process.
   * @return the node after running through the processor.
   */
  GResultOf<ConfigNode> process(String path, ConfigNode currentNode);

  /**
   * Apply the ConfigNodeProcessorConfig to the config node Processor. Needed when building via the ServiceLoader
   * It is a default method as most Config Node Processor don't need to apply configs.
   *
   * @param config GestaltConfig to update the Processor
   */
  default void applyConfig(ConfigNodeProcessorConfig config) {
  }
}
```

When you write your own applyConfig method, each node of the config tree will be passed into the process method. You can either modify the current node or return it as is. The return value will be used to replace the tree, so if you return nothing your tree will be lost.
You can re-write any intermediate node or only modify the leaf nodes as `TransformerConfigNodeProcessor` does.
To register your own default `ConfigNodeProcessor`, add it to a file in `META-INF\services\org.github.gestalt.config.processor.config.ConfigNodeProcessor` and add the full path to your `ConfigNodeProcessor`.

The `TransformerConfigNodeProcessor` is a specific type of `ConfigNodeProcessor` that allows you to replace strings in a leaf node that match `${transformer:key}` into a config value. where the transformer is the name of a Transformer registered with the TransformerConfigNodeProcessor, such as in the above ConfigNodeProcessor section with envMap, sys, and map. The key is a string lookup into the transformer.
To implement your own Transformer you need to implement the Transformer class.

```java
/**
 * Allows you to add your own custom source for the TransformerConfigNodeProcessor.
 * Whenever the TransformerConfigNodeProcessor sees a value ${name:key} the transform is selected that matches the same name
 */
public interface Transformer {
  /**
   * the name that will match the ${name:key} the transform is selected that matches the same name
   * @return
   */
  String name();

  /**
   * When a match is found for ${name:key} the key and the path are passed into the process method.
   * The returned value replaces the whole ${name:key}
   * @param path the current path
   * @param key the key to lookup int this transform.
   * @return the value to replace the ${name:key}
   */
  Optional<String> process(String path, String key);
}
```

To register your own default Transformer, add it to a file in `META-INF\services\org.github.gestalt.config.processor.config.transform.Transformer` and add the full path to your Transformer.

the annotation `@ConfigPriority(100)`, specifies the descending priority order to check your transformer when a substitution has been made without specifying the source `${key}`
