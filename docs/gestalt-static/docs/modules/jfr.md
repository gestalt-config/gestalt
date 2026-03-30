---
sidebar_position: 12
---

## JFR Module

Gestalt provides a Java Flight Recorder (JFR) module for recording configuration access events to JFR for monitoring and profiling purposes.

To use the JFR module, add `gestalt-jfr` to your build configuration.

The module supports:
- Recording configuration access events to JFR
- Tracking timing information for configuration retrievals
- Recording success/failure status of configuration access
- Optional tagging of events with configuration metadata

---

### Configuring JFR Integration

To use JFR event recording, configure a `JfrModuleConfig` and register it with your Gestalt builder.

### Example

```java
import org.github.gestalt.config.jfr.builder.JfrModuleConfigBuilder;

// Configure Gestalt with JFR event recording
Gestalt gestalt = new GestaltBuilder()
  .addSource(ClassPathConfigSourceBuilder.builder().setResource("/config.properties").build())
  .addModuleConfig(JfrModuleConfigBuilder.builder()
    .setIncludePath(false)
    .setIncludeClass(false)
    .setIncludeOptional(false)
    .setIncludeTags(false)
    .setEventLabel("Gestalt Config Access")
    .build())
  .build();

gestalt.loadConfigs();

// Configuration access events are now recorded to JFR
String dbHost = gestalt.getConfig("db.host", String.class);
```

### Viewing JFR Events

To view the recorded JFR events:

1. **Using JDK Mission Control (JMC):**
   - Start your application with JFR enabled: `java -XX:StartFlightRecording=disk=true,duration=60s,filename=recording.jfr MyApp`
   - Open the recording file in JMC
   - Navigate to the "Events" tab and search for "Gestalt Configuration Access" events

2. **Using `jfr` command-line tool:**
   ```bash
   jfr dump --events "Gestalt/* recording.jfr
   ```

### Configuration Options

- `setIncludePath`: Include the configuration path in JFR events (default: false). Warning: High cardinality field
- `setIncludeClass`: Include the target class type in JFR events (default: false). Warning: High cardinality field
- `setIncludeOptional`: Include whether the configuration was accessed as optional (default: false)
- `setIncludeTags`: Include configuration tags in JFR events (default: false)
- `setEventLabel`: Custom label for JFR events (default: "Gestalt Config Access")

### Event Details

Each recorded event contains:
- **Path**: The configuration path being accessed (if enabled)
- **Class**: The target class for deserialization (if enabled)
- **Is Optional**: Whether the configuration was optional (if enabled)
- **Tags**: Configuration tags applied to this access (if enabled)
- **Duration**: Time taken to retrieve the configuration (in nanoseconds)
- **Success**: Whether the configuration retrieval was successful
- **Error**: Error message if the retrieval failed

### Performance Considerations

JFR recording has minimal overhead when events are not actively being collected. High cardinality fields (path, class) should be disabled in production unless actively profiling, as they can significantly increase event volume.