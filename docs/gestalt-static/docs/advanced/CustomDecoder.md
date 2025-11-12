---
sidebar_position: 8
---

# Custom Decoder
By default, Gestalt uses its built-in decoder to decode configuration values into complex objects. However, you can provide your own custom decoder by implementing the [Decoder](https://github.com/gestalt-config/gestalt/blob/main/gestalt-core/src/main/java/org/github/gestalt/config/decoder/Decoder.java) interface. This allows you to have full control over how configuration values are decoded into objects.

The custom decode has several methods that need to be implemented.

The method `Priority priority();` allows you to set the priority of the decoder. This is useful when you have multiple decoders that can handle the same type. The decoder with the highest priority will be used. It is also important that your decoder has a higher priority than object decoder that has a priority `Priority.VERY_LOW`. The object decoder will match most classes so you want your to be selected first. 

The method `String name();` allows you to set the name of the decoder. This is useful for logging and debugging purposes.

the method `default void applyConfig(GestaltConfig config)` is optional and allows you to get the gestalt configuration when the decoder is registered. 

The method `boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type)` is used to determine if the decoder can handle the given type. This is called before the `decode(...)` method. If this method returns true, then the `decode(...)` method will be called.

This method provides the path of the value being decoded, the tags for the current request, the current node being decoded, and the type of object being decoded.

the tags are the `tags` for the current request, which can be used to determine if the decoder should be used based on the tags.

The node is a `ConfigNode` is the specific node we are trying to decode, so you can for example ensure that if you are decoding a single value that the node is a LeafNode.

The type is a `TypeCapture` that contains the raw type and any generic type information. You can use this to ensure that you are decoding the correct type.

Here is an example of a `canDecode` method that only decodes instances of `MyClass` when the node is a `LeafNode`.
```java
  if (node instanceof LeafNode) {
    MyClass.class.isAssignableFrom(type.getRawType())
  } else {
    return false;
  }
```

The method `GResultOf<T> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext)` is used to decode the given node into the desired type. This method is called if the `canDecode(...)` method returns true.
 
You are passed in the path and tags for the current request, the node to be decoded, the type of object to decode to, and a `DecoderContext` that can be used to decode nested objects.

The ConfigNode is the specific node we are trying to decode. It can be of type LeafNode, MapNode, or ArrayNode. You will need to handle the different node types accordingly.
The Specific node should have the methods needed to get the value to decode. For example, a LeafNode has a `getValue()` method that returns the string value of the node.

For an example of how to use a LeafNode see the reusable base class for leafs: [LeafDecoder](https://github.com/gestalt-config/gestalt/blob/main/gestalt-core/src/main/java/org/github/gestalt/config/decoder/LeafDecoder.java) and the specific implementation for a Integer [IntegerDecoder](https://github.com/gestalt-config/gestalt/blob/main/gestalt-core/src/main/java/org/github/gestalt/config/decoder/IntegerDecoder.java)

For an example of how to use a ListNode see the reusable base class for array type collections: [CollectionDecoder](https://github.com/gestalt-config/gestalt/blob/main/gestalt-core/src/main/java/org/github/gestalt/config/decoder/CollectionDecoder.java) and a specific implementation [ListDecoder](https://github.com/gestalt-config/gestalt/blob/main/gestalt-core/src/main/java/org/github/gestalt/config/decoder/ListDecoder.java)

For an example of how to use a MapNode see the map type collections: [MapDecoder](https://github.com/gestalt-config/gestalt/blob/main/gestalt-core/src/main/java/org/github/gestalt/config/decoder/MapDecoder.java)


## Registering a Custom Decoder
To register your custom decoder with Gestalt, you can use the `GestaltBuilder` and the `addDecoder` method. If you add your own decoder you need to call `addDefaultDecoders()` as well to ensure the built-in decoders are also registered. Otherwise, gestalt will only use your custom decoder(s).
Here is an example of how to register a custom decoder:

```java
  GestaltBuilder builder = new GestaltBuilder()
    .addDecoder(new LongDecoder())
    .addDefaultDecoders();
```
