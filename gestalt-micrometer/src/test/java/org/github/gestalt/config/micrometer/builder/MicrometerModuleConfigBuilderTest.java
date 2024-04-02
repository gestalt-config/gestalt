package org.github.gestalt.config.micrometer.builder;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MicrometerModuleConfigBuilderTest {

    @Test
    public void builderTest() {
        MicrometerModuleConfigBuilder builder = MicrometerModuleConfigBuilder
            .builder()
            .setIncludeClass(true)
            .setIncludePath(true)
            .setIncludeOptional(true)
            .setIncludeTags(true)
            .setPrefix("test")
            .setMeterRegistry(new SimpleMeterRegistry());

        Assertions.assertTrue(builder.getIncludeClass());
        Assertions.assertTrue(builder.getIncludeOptional());
        Assertions.assertTrue(builder.getIncludeTags());
        Assertions.assertEquals("test", builder.getPrefix());
        Assertions.assertTrue(builder.getIncludePath());
        Assertions.assertNotNull(builder.getMeterRegistry());

        var config = builder.build();

        Assertions.assertTrue(config.isIncludeClass());
        Assertions.assertTrue(config.isIncludeOptional());
        Assertions.assertTrue(config.isIncludeTags());
        Assertions.assertEquals("test", config.getPrefix());
        Assertions.assertTrue(config.isIncludePath());
        Assertions.assertEquals("micrometer", config.name());
    }
}
