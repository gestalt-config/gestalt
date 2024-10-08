---
sidebar_position: 5
---

# Decoder

Decoders allow Gestalt to decode a config node into a specific value, class or collection. A Decoder can either work on a leaf and decode a single value, or it can work on a Map or Array node and decode a class or collection.
You can create your own decoder by implementing the Decoder interface. By returning true for the matches Gestalt will ask your decoder to decode the current node by calling your Decoders decode method. Gestalt will pass in the current path, the current node to decode and the DecoderService so we can decode any subnodes.
```java
  /**
 * true if this decoder matches the type capture.
 *
 * @param path           the current path
 * @param tags           the tags for the current request
 * @param node           the current node we are decoding.
 * @param type           the type of object we are decoding.
 * @return true if this decoder matches the type capture
 */
  boolean canDecode(path: String, tags: Tags, configNode:ConfigNode, TypeCapture<?> klass);

  /**
   * Decode the current node. If the current node is a class or list we may need to decode sub nodes.
   *
   * @param path the current path
   * @param node the current node we are decoding.
   * @param type the type of object we are decoding.
   * @param decoderService decoder Service used to decode members if needed. Such as class fields.
   * @return GResultOf the current node with details of either success or failures.
   */
  GResultOf<T> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService);
```
