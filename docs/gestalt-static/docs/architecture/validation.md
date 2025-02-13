---
sidebar_position: 8
---

# Validation
Validations are implemented as a Result Processor as well in `ValidationResultProcessor`.
To ensure a simple API for validations it does not use the ResultProcessor interface but a `ConfigValidator` interface.

To automatically register your own default `ConfigValidator`, add it to a file in `META-INF\services\org.github.gestalt.config.processor.result.validation.ConfigValidator` and add the full package of classpath `ConfigValidator`. This is how `gestalt-validator-hibernate` automatically is discovered.

Alternatively, you can implement the interface and register it with the gestalt builder `addValidators(List<ConfigValidator> validatorsSet)`.

```java
/**
 * Interface for validating objects.
 *
 *  @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface ConfigValidator {

    /**
     * If your Validator needs access to the Gestalt Config.
     *
     * @param config Gestalt configuration
     */
    default void applyConfig(GestaltConfig config) {}

    /**
     * Returns the {@link GResultOf} with the validation results. If the object is ok it will return the result with no errors.
     * If there are validation errors they will be returned.
     *
     * @param obj object to validate.
     * @param path path the object was located at
     * @param klass the type of object.
     * @param tags any tags used to retrieve te object
     * @return The validation results with either errors or a successful  obj.
     * @param <T> Class of the object.
     */
    <T> GResultOf<T> validator(T obj, String path, TypeCapture<T> klass, Tags tags);
}
```
