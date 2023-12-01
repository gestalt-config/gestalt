package org.github.gestalt.config.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KubernetesSecretConfigSourceBuilderTest {

    private Path kubernetesPath;

    @BeforeAll
    public void setup() throws URISyntaxException {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        Path testFileDir = Paths.get(testFileURL.toURI());
        kubernetesPath = testFileDir.getParent().resolve("kubernetes");
    }

    @Test
    void testBuild() {
        // Given
        Path folderPath = kubernetesPath;

        // When
        KubernetesSecretConfigSourceBuilder builder = KubernetesSecretConfigSourceBuilder.builder()
            .setPath(folderPath);

        // Then
        assertEquals(folderPath, builder.getPath());

        File k8File = folderPath.toFile();
        builder = builder.setFile(k8File);
        assertEquals(k8File, builder.getFile());

        // When
        try {
            ConfigSourcePackage configSourcePackage = builder.build();
            KubernetesSecretConfigSource kubernetesSecretConfigSource =
                (KubernetesSecretConfigSource) configSourcePackage.getConfigSource();

            // Then
            assertEquals(folderPath, kubernetesSecretConfigSource.getPath());
            assertEquals(folderPath.toFile(), builder.getFile());

            // Additional checks for ConfigSourcePackage
            assertTrue(configSourcePackage.getConfigReloadStrategies().isEmpty());

        } catch (Exception e) {
            fail("Exception thrown during build: " + e.getMessage());
        }
    }
}
