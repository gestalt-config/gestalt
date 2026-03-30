# Gestalt JFR Module

This module provides Java Flight Recorder (JFR) integration for Gestalt configuration management.

## Features

- Records configuration access events to JFR for profiling and monitoring
- Tracks timing information for configuration retrievals
- Records success/failure status and error messages
- Optional metadata tagging (path, class, tags, optional flag)
- Minimal performance overhead when not actively profiling

## Dependencies

This module requires Java 11+ (JFR is available in Java 11 and later).

## Configuration

### Basic Setup

```java
Gestalt gestalt = new GestaltBuilder()
  .addSource(ClassPathConfigSourceBuilder.builder().setResource("/config.properties").build())
  .addModuleConfig(JfrModuleConfigBuilder.builder().build())
  .build();

gestalt.loadConfigs();
```

### Advanced Configuration

```java
Gestalt gestalt = new GestaltBuilder()
  .addSource(ClassPathConfigSourceBuilder.builder().setResource("/config.properties").build())
  .addModuleConfig(JfrModuleConfigBuilder.builder()
    .setIncludePath(true)
    .setIncludeClass(true)
    .setIncludeOptional(true)
    .setIncludeTags(true)
    .setEventLabel("Application Configuration Access")
    .build())
  .build();

gestalt.loadConfigs();
```

## Viewing Events

### Using JDK Mission Control

1. Start your application with JFR recording:
   ```bash
   java -XX:StartFlightRecording=disk=true,duration=60s,filename=recording.jfr MyApp
   ```

2. Open the recording in JMC:
   ```bash
   jmc
   ```

3. Open the recording file and navigate to Events > Gestalt Configuration Access

### Using Command Line

```bash
jfr dump --events "Gestalt/*" recording.jfr
```

## Performance

JFR has virtually no overhead when events are not being collected. When actively profiling:
- Minimal overhead for basic event recording
- High cardinality fields (path, class) can increase memory and disk usage
- Consider disabling high cardinality fields in production unless actively profiling

## Notes

- JFR is available in Java 11+
- Events are only recorded if JFR is enabled in the JVM
- The module gracefully handles environments where JFR is not available
