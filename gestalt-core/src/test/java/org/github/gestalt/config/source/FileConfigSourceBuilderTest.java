package org.github.gestalt.config.source;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

class FileConfigSourceBuilderTest {

    @Test
    void testBuild() {
        // Given
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        Path filePath = new File(testFileURL.getFile()).toPath();

        // When
        FileConfigSourceBuilder builder = FileConfigSourceBuilder.builder()
            .setPath(filePath);

        // Then
        assertEquals(filePath, builder.getPath());

        builder = builder.setFile(filePath.toFile());

        assertEquals(filePath, builder.getPath());
        assertEquals(filePath.toFile(), builder.getFile());

        // When
        try {
            ConfigSourcePackage<FileConfigSource> configSourcePackage = builder.build();
            FileConfigSource fileConfigSource = configSourcePackage.getConfigSource();

            // Then
            assertEquals(filePath, fileConfigSource.getPath());
            assertEquals(filePath.toFile(), builder.getFile());

            // Additional checks for ConfigSourcePackage
            assertTrue(configSourcePackage.getConfigReloadStrategies().isEmpty());

        } catch (Exception e) {
            fail("Exception thrown during build: " + e.getMessage());
        }
    }
}
