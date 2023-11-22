package org.github.gestalt.config.source;

import org.junit.jupiter.api.Test;
import org.github.gestalt.config.exceptions.GestaltException;

import static org.junit.jupiter.api.Assertions.*;

class URLConfigSourceBuilderTest {

    @Test
    void buildURLConfigSource() throws GestaltException {
        // Arrange
        String sourceURL = "https://example.com/config.properties";

        URLConfigSourceBuilder builder = URLConfigSourceBuilder.builder();
        builder.setSourceURL(sourceURL);

        // Act
        ConfigSourcePackage<URLConfigSource> configSourcePackage = builder.build();

        assertEquals(sourceURL, builder.getSourceURL());

        // Assert
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        URLConfigSource urlConfigSource = configSourcePackage.getConfigSource();
        assertTrue(urlConfigSource.hasStream());
    }


    @Test
    void buildURLConfigSourceNullSourceURL() {
        // Arrange
        URLConfigSourceBuilder builder = URLConfigSourceBuilder.builder();

        // Act and Assert
        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("The url string provided was null", e.getMessage());
    }

    @Test
    void buildURLConfigSourceEmptySourceURL() {
        // Arrange
        URLConfigSourceBuilder builder = URLConfigSourceBuilder.builder();
        builder.setSourceURL("");

        // Act and Assert
        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("Exception creating URL , with error: URI is not absolute", e.getMessage());
    }
}
