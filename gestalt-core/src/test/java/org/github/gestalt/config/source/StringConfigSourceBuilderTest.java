package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StringConfigSourceBuilderTest {

    @Test
    void buildStringConfigSource() throws GestaltException {
        String config = "db.port = 1234\ndb.password = password\ndb.user = notroot";
        String format = "properties";

        StringConfigSourceBuilder builder = StringConfigSourceBuilder.builder();
        builder.setConfig(config)
            .setFormat(format);

        assertEquals(config, builder.getConfig());
        assertEquals(format, builder.getFormat());
        ConfigSourcePackage<StringConfigSource> configSourcePackage = builder.build();

        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());



        StringConfigSource stringConfigSource = configSourcePackage.getConfigSource();
        assertTrue(stringConfigSource.hasStream());
        assertEquals(format, stringConfigSource.format());
    }

    @Test
    void buildStringConfigSourceNullConfig() {
        // Arrange
        String format = "properties";

        StringConfigSourceBuilder builder = StringConfigSourceBuilder.builder();
        builder.setFormat(format);

        // Act and Assert
        GestaltException e = assertThrows(GestaltException.class, builder::build);
        assertEquals("The string provided was null", e.getMessage());
    }

    @Test
    void buildStringConfigSourceNullFormat() {
        // Arrange
        String config = "db.port = 1234\ndb.password = password\ndb.user = notroot";

        StringConfigSourceBuilder builder = StringConfigSourceBuilder.builder();
        builder.setConfig(config);

        // Act and Assert
        GestaltException e = assertThrows(GestaltException.class, builder::build);
        assertEquals("The string format provided was null", e.getMessage());
    }

    @Test
    void buildStringConfigSourceEmptyConfig() throws GestaltException {
        // Arrange
        String config = "";
        String format = "properties";

        StringConfigSourceBuilder builder = StringConfigSourceBuilder.builder();
        builder.setConfig(config)
            .setFormat(format);

        // Act
        ConfigSourcePackage<StringConfigSource> configSourcePackage = builder.build();

        // Assert
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        StringConfigSource stringConfigSource = configSourcePackage.getConfigSource();
        assertTrue(stringConfigSource.hasStream());
        assertEquals(format, stringConfigSource.format());
    }
}
