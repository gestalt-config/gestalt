---
sidebar_position: 2
---

# Retrieving Complex Objects

To retrieve a complex object, you need to pass in the class for Gestalt to return. Gestalt will automatically use reflection to create the object, determine all the fields in the requested class, and then lookup the values in the configurations to inject into the object. It will attempt to use the setter fields first, then fallback to directly setting the fields.

There are two configuration options that allow you to control when errors are thrown when decoding complex objects.

```java
HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);
```

### `treatMissingValuesAsErrors`

Treat missing field values in an object, proxy, record, or data object as errors. This will cause the API to either throw errors or return an empty optional.

- If this is `true`, any time a value that is not discretionary is missing, it will fail and throw an exception.
- If this is `false`, a missing value will be returned as `null` or the default initialization. `Null` for objects and `0` for primitives.

### `treatMissingDiscretionaryValuesAsErrors`

Treat missing discretionary values (optional, fields with defaults, fields with default annotations) in an object, proxy, record, or data object as errors.

- If this is `false`, you will be able to get the configuration with default values or an empty Optional.
- If this is `true`, if a field is missing and would have had a default, it will fail and throw an exception.


### `@Nullable` annotations

If a field or method is annotated with a `@Nullable` annotation, it will treat a missing value as a discretionary value. So as long as `treatMissingDiscretionaryValuesAsErrors` is not enabled, `@Nullable` fields will allow null values without throwing errors.

There are multiple `@Nullable` annotations and for this to work the annotations must use `@Retention(RetentionPolicy.RUNTIME)` so the annotation is available at runtime for Gestalt.
One good library to use is `jakarta.annotation:jakarta.annotation-api` that has a `@Nullable` with `@Retention(RetentionPolicy.RUNTIME)`.

#### Examples of required and discretionary fields.

Here are some examples of required and discretionary fields and which setting can control if they are treated as errors or allowed.

```java
public class DBInfo {
  // discretionary value controlled by treatMissingValuesAsErrors
  private Optional<Integer> port;                   // default value Optional.empty()
  private String uri = "my.sql.db";                 // default value "my.sql.db"
  private  @Config(defaultVal = "100") Integer connections; // default value 100

  // required value controlled by treatMissingDiscretionaryValuesAsErrors
  private String password;                         // default value null
}

public interface DBInfoInterface {
  Optional<Integer> getPort();                      // default value Optional.empty()
  default String getUri() {                         // default value "my.sql.db"
     return  "my.sql.db";
  }
  @Config(defaultVal = "100")
  Integer getConnections();                         // default value 100

  // required value controlled by treatMissingDiscretionaryValuesAsErrors
  String getPassword();                            // default value null
}

public record DBInfoRecord(
  // discretionary value controlled by treatMissingDiscretionaryValuesAsErrors
  @Config(defaultVal = "100") Integer connections,  // default value 100
  Optional<Integer> port,                           // default value Optional.empty()
  
  // required value controlled by treatMissingDiscretionaryValuesAsErrors
  String uri,                                      // default value null
  String password                                  // default value null
) {}
```

```kotlin
data class DBInfoDataDefault(
  // discretionary value controlled by treatMissingValuesAsErrors
    var port: Int?,                                 // default value null
    var uri: String = "my.sql.db",                  // default value "my.sql.db"
    @Config(defaultVal = "100")  var connections: Integer, // default value 100

    // required value cam not disable treatMissingDiscretionaryValuesAsErrors and allow nulls. 
    var password: String,                           // required, can not be null.   
)
```
