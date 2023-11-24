package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.StringConfigSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

class TimedConfigReloadStrategyBuilderTest {

    @Test
    void buildWithValidConfigurationReturnsTimedConfigReloadStrategy() throws GestaltException {

        ConfigSource source = new StringConfigSource("abc=def", "properties");
        TimedConfigReloadStrategyBuilder builder = TimedConfigReloadStrategyBuilder.builder();
        builder.setSource(source);
        builder.setReloadDelay(Duration.ofSeconds(5));


        Assertions.assertEquals(source, builder.getSource());
        Assertions.assertEquals(Duration.ofSeconds(5), builder.getReloadDelay());

        try {
            Assertions.assertNotNull(builder.build());

        } catch (GestaltConfigurationException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void buildWithoutSourceThrowsGestaltConfigurationException() {
        TimedConfigReloadStrategyBuilder builder = TimedConfigReloadStrategyBuilder.builder();
        builder.setReloadDelay(Duration.ofSeconds(5));

        GestaltConfigurationException exception =  Assertions.assertThrows(GestaltConfigurationException.class, builder::build);
        Assertions.assertEquals("When building a Timed Change Reload Strategy with the builder you must set a source",
            exception.getMessage());
    }

    @Test
    void buildWithoutReloadDelayThrowsGestaltConfigurationException() throws GestaltException {
        ConfigSource source = new StringConfigSource("abc=def", "properties");
        TimedConfigReloadStrategyBuilder builder = TimedConfigReloadStrategyBuilder.builder();
        builder.setSource(source);

        // Act & Assert
        GestaltConfigurationException exception =  Assertions.assertThrows(GestaltConfigurationException.class, builder::build);
        Assertions.assertEquals("When building a Timed Change Reload Strategy with the builder the Reload Delay must be set",
            exception.getMessage());
    }
}

