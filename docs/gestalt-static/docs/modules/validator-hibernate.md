---
sidebar_position: 6
---

# Hibernate Validator
Gestalt allows a validator to hook into and validate calls to get a configuration object.  Gestalt includes a [Hibernate Bean Validator](https://hibernate.org/validator/) implementation.

If the object decoded fails to validate, a `GestaltException` is thrown with the details of the failed validations.
For calls to `getConfig` with a default value it will log the failed validations then return the default value.
For calls to `getConfigOptional` it will log the failed validations then return an `Optional.empty()`.

To import the Hibernate Validator implementation add `gestalt-validator-hibernate` to your build files.

In Maven:
```xml
<dependency>
  <groupId>com.github.gestalt-config</groupId>
  <artifactId>gestalt-validator-hibernate</artifactId>
  <version>${version}</version>
</dependency>
```
Or in Gradle
```kotlin
implementation("com.github.gestalt-config:gestalt-validator-hibernate:${version}")
```

Then when building gestalt, you need to register the module config `HibernateModuleConfig` using the `HibernateModuleBuilder`.

```java
ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
Validator validator = factory.getValidator();

Gestalt gestalt = new GestaltBuilder()
  .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
  .setValidationEnabled(true)
  .addModuleConfig(HibernateModuleBuilder.builder()
    .setValidator(validator)
    .build())
  .build();

gestalt.loadConfigs();
```

For details on how to use the [Hibernate Validator](https://hibernate.org/validator/) see their documentation. 
