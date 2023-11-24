package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.StringConfigSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ManualConfigReloadStrategyBuilderTest {

    @Test
    void buildWithValidConfigurationReturnsManualConfigReloadStrategy() throws GestaltException {
        ConfigSource source = new StringConfigSource("abc=def", "properties");

        ManualConfigReloadStrategyBuilder builder = ManualConfigReloadStrategyBuilder.builder();
        builder.setSource(source);

        Assertions.assertEquals(source, builder.getSource());

        // Act
        try {
            Assertions.assertNotNull(builder.build());
        } catch (GestaltConfigurationException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void buildWithoutSourceThrowsGestaltConfigurationException() {
        // Arrange
        ManualConfigReloadStrategyBuilder builder = ManualConfigReloadStrategyBuilder.builder();

        // Act & Assert
        GestaltConfigurationException exception =  Assertions.assertThrows(GestaltConfigurationException.class, builder::build);
        Assertions.assertEquals("When building a Manual Change Reload Strategy with the builder you must set a source",
            exception.getMessage());
    }
}
