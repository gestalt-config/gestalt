package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemPropertiesConfigSourceBuilderTest {

    @Test
    void buildSystemPropertiesConfigSource() throws GestaltException {
        // Arrange
        SystemPropertiesConfigSourceBuilder builder = SystemPropertiesConfigSourceBuilder.builder();

        // Act
        ConfigSourcePackage configSourcePackage = builder.build();

        // Assert
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        SystemPropertiesConfigSource systemPropertiesConfigSource = (SystemPropertiesConfigSource) configSourcePackage.getConfigSource();
        assertFalse(systemPropertiesConfigSource.failOnErrors());
    }


    @Test
    void buildSystemPropertiesConfigSourceFailOnErrors() throws GestaltException {
        // Arrange
        SystemPropertiesConfigSourceBuilder builder = SystemPropertiesConfigSourceBuilder.builder();
        builder.setFailOnErrors(true);

        // Act
        ConfigSourcePackage configSourcePackage = builder.build();

        // Assert
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        SystemPropertiesConfigSource systemPropertiesConfigSource = (SystemPropertiesConfigSource) configSourcePackage.getConfigSource();
        assertTrue(systemPropertiesConfigSource.failOnErrors());
        assertTrue(builder.failOnErrors());
    }
}

