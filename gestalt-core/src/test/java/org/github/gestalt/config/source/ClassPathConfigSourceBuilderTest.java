package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathConfigSourceBuilderTest {

    @Test
    void buildValidResourcePathConfigSourcePackageBuiltSuccessfully() throws GestaltException {
        // Arrange
        String resourcePath = "example-config.yml";
        ClassPathConfigSourceBuilder builder = ClassPathConfigSourceBuilder.builder().setResource(resourcePath);

        // Act
        ConfigSourcePackage configSourcePackage = builder.build();

        // Assert
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());
        assertEquals("Class Path resource: " + resourcePath, configSourcePackage.getConfigSource().name());
        assertEquals("example-config.yml", builder.getResource());
    }

    @Test
    void buildNllResourcePathThrowException() {
        // Arrange
        ClassPathConfigSourceBuilder builder = ClassPathConfigSourceBuilder.builder();

        // Act and Assert
        assertThrows(GestaltException.class, builder::build);
    }
}

