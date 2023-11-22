package org.github.gestalt.config.builder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.reload.ConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ConfigReloadStrategyBuilderTest {

    @Test
    void setSourceValidConfigSourceSourceSetSuccessfully() {
        // Arrange
        ConfigReloadStrategyBuilderMock builder = new ConfigReloadStrategyBuilderMock();

        // Act
        ConfigReloadStrategyBuilderMock resultBuilder = builder.setSource(Mockito.mock(ConfigSource.class));

        // Assert
        assertNotNull(resultBuilder.getSource());
    }

    @Test
    void setSourceNullConfigSourceThrowException() {
        // Arrange
        ConfigReloadStrategyBuilderMock builder = new ConfigReloadStrategyBuilderMock();

        // Act and Assert
        assertThrows(NullPointerException.class, () -> builder.setSource(null));
    }

    @Test
    void buildValidConfigSourceBuildSuccessfully() throws GestaltConfigurationException {
        // Arrange
        ConfigReloadStrategyBuilderMock builder = new ConfigReloadStrategyBuilderMock();
        ConfigSource mockSource = Mockito.mock(ConfigSource.class);
        builder.setSource(mockSource);

        // Act
        ConfigReloadStrategy result = builder.build();

        // Assert
        assertNotNull(result);
    }

    @Test
    void buildNullConfigSourceThrowException() {
        // Arrange
        ConfigReloadStrategyBuilderMock builder = new ConfigReloadStrategyBuilderMock();

        // Act and Assert
        assertThrows(GestaltConfigurationException.class, builder::build);
    }

    private static class ConfigReloadStrategyBuilderMock
        extends ConfigReloadStrategyBuilder<ConfigReloadStrategyBuilderMock, ConfigReloadStrategy> {
        @Override
        public ConfigReloadStrategy build() throws GestaltConfigurationException {
            if (source == null) {
                throw new GestaltConfigurationException("ConfigSource cannot be null");
            }
            // Mock implementation for testing purposes
            return Mockito.mock(ConfigReloadStrategy.class);
        }
    }
}
