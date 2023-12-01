package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapConfigSourceBuilderTest {

    @Test
    void buildMapConfigSource() throws GestaltException {
        // Arrange
        Map<String, String> customConfig = new HashMap<>();
        customConfig.put("db.port", "1234");
        customConfig.put("db.password", "password");
        customConfig.put("db.user", "notroot");

        MapConfigSourceBuilder builder = MapConfigSourceBuilder.builder();
        builder.setCustomConfig(customConfig);

        // Act
        ConfigSourcePackage configSourcePackage = builder.build();

        // Assert
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        MapConfigSource configSource = (MapConfigSource) configSourcePackage.getConfigSource();
        assertTrue(configSource.hasList());
        assertEquals(3, configSource.loadList().size());
    }

    @Test
    void addCustomConfig() {
        // Arrange
        MapConfigSourceBuilder builder = MapConfigSourceBuilder.builder();

        // Act
        builder.addCustomConfig("db.port", "1234")
            .addCustomConfig("db.password", "password")
            .addCustomConfig("db.user", "notroot");

        // Assert
        Map<String, String> customConfig = builder.getCustomConfig();
        assertNotNull(customConfig);
        assertEquals(3, customConfig.size());
        assertEquals("1234", customConfig.get("db.port"));
        assertEquals("password", customConfig.get("db.password"));
        assertEquals("notroot", customConfig.get("db.user"));
    }

    @Test
    void addCustomConfigNullMap() {
        // Arrange
        MapConfigSourceBuilder builder = MapConfigSourceBuilder.builder();

        // Act
        builder.addCustomConfig("db.port", "1234");

        // Assert
        Map<String, String> customConfig = builder.getCustomConfig();
        assertNotNull(customConfig);
        assertEquals(1, customConfig.size());
        assertEquals("1234", customConfig.get("db.port"));
    }

    @Test
    void buildMapConfigSourceEmptyCustomConfig() throws GestaltException {
        // Arrange
        MapConfigSourceBuilder builder = MapConfigSourceBuilder.builder();

        // Act
        ConfigSourcePackage configSourcePackage = builder.build();

        // Assert
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        MapConfigSource configSource = (MapConfigSource) configSourcePackage.getConfigSource();
        assertNotNull(configSource.hasList());
        assertTrue(configSource.loadList().isEmpty());
    }
}
