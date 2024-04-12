package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class InputStreamConfigSourceBuilderTest {

    @Test
    void buildStringConfigSource() throws GestaltException {
        String config = "db.port = 1234\ndb.password = password\ndb.user = notroot";
        String format = "properties";

        InputStreamConfigSourceBuilder builder = InputStreamConfigSourceBuilder.builder();
        builder.setConfig(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)))
            .setFormat(format);

        assertNotNull(builder.getConfig());
        assertEquals(format, builder.getFormat());
        ConfigSourcePackage configSourcePackage = builder.build();

        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        InputStreamConfigSource stringConfigSource = (InputStreamConfigSource) configSourcePackage.getConfigSource();
        assertTrue(stringConfigSource.hasStream());
        assertEquals(format, stringConfigSource.format());
    }

    @Test
    void buildStringConfigSourceNullConfig() {
        // Arrange
        String format = "properties";

        InputStreamConfigSourceBuilder builder = InputStreamConfigSourceBuilder.builder();
        builder.setFormat(format);

        // Act and Assert
        GestaltException e = assertThrows(GestaltException.class, builder::build);
        assertEquals("The InputStream provided was null", e.getMessage());
    }

    @Test
    void buildStringConfigSourceNullFormat() {
        // Arrange
        String config = "db.port = 1234\ndb.password = password\ndb.user = notroot";

        InputStreamConfigSourceBuilder builder = InputStreamConfigSourceBuilder.builder();
        builder.setConfig(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));

        // Act and Assert
        GestaltException e = assertThrows(GestaltException.class, builder::build);
        assertEquals("The InputStream format provided was null", e.getMessage());
    }

    @Test
    void buildStringConfigSourceEmptyConfig() throws GestaltException {
        // Arrange
        String config = "";
        String format = "properties";

        InputStreamConfigSourceBuilder builder = InputStreamConfigSourceBuilder.builder();
        builder.setConfig(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)))
            .setFormat(format);

        // Act
        ConfigSourcePackage configSourcePackage = builder.build();

        // Assert
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        InputStreamConfigSource stringConfigSource = (InputStreamConfigSource) configSourcePackage.getConfigSource();
        assertTrue(stringConfigSource.hasStream());
        assertEquals(format, stringConfigSource.format());
    }
}
