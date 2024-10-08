---
sidebar_position: 7
---

# Result Processors

Result Processors are used to modify the result of getting a configuration and decoding it.
Each processor has an annotation `@ConfigPriority` so we run them in order passing the output of one Result Processor as the input to the next.

Gestalt has two core result processors `ErrorResultProcessor` and `DefaultResultProcessor`.
The `ErrorResultProcessor` throws a `GestaltException` if there is an unrecoverable error.
The `DefaultResultProcessor` will convert the result into a default value if there is no result.

To implement your own Result Processors you need to inherit from ResultProcessor.

To automatically register your own default `ResultProcessor`, add it to a file in `META-INF\services\org.github.gestalt.config.processor.result.ResultProcessor` and add the full package of classpath your `ResultProcessor`.

Alternatively, you can implement the interface and register it with the gestalt builder `addResultProcessors(List<ResultProcessor> resultProcessorSet)`.

```java
public interface ResultProcessor {

  /**
   * If your Result Processor needs access to the Gestalt Config.
   *
   * @param config Gestalt configuration
   */
  default void applyConfig(GestaltConfig config) {}

  /**
   * Returns the {@link GResultOf} with any processed results.
   * You can modify the results, errors or any combination.
   * If your post processor does nothing to the node, return the original node.
   *
   * @param results GResultOf to process.
   * @param path path the object was located at
   * @param isOptional if the result is optional (an Optional or has a default.
   * @param defaultVal value to return in the event of failure.
   * @param klass the type of object.
   * @param tags any tags used to retrieve te object
   * @return The validation results with either errors or a successful  obj.
   * @param <T> Class of the object.
   * @throws GestaltException for any exceptions while processing the results, such as if there are errors in the result.
   */
  <T> GResultOf<T> processResults(GResultOf<T> results, String path, boolean isOptional, 
                                  T defaultVal, TypeCapture<T> klass, Tags tags)
    throws GestaltException;
}
```
