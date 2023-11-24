package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.FileConfigSource;
import org.github.gestalt.config.source.StringConfigSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class FileConfigReloadStrategyBuilderTest {

    @Test
    void buildWithValidConfigurationReturnsFileChangeReloadStrategy() throws GestaltException {

        URL testFileURL = FileConfigReloadStrategyBuilderTest.class.getClassLoader().getResource("test.properties");
        Path filePath = new File(testFileURL.getFile()).toPath();

        ConfigSource source = new FileConfigSource(filePath);

        FileConfigReloadStrategyBuilder builder = FileConfigReloadStrategyBuilder.builder();
        builder.setSource(source);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        builder.setExecutor(executorService);

        Assertions.assertEquals(source, builder.getSource());
        Assertions.assertEquals(executorService, builder.getExecutor());

        try {
            Assertions.assertNotNull(builder.build());
        } catch (GestaltConfigurationException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void buildWithoutSourceThrowsGestaltConfigurationException() {
        FileConfigReloadStrategyBuilder builder = FileConfigReloadStrategyBuilder.builder();

        GestaltConfigurationException exception =  Assertions.assertThrows(GestaltConfigurationException.class, builder::build);
        Assertions.assertEquals("When building a File Change Reload Strategy with the builder you must set a source",
            exception.getMessage());
    }

    @Test
    void buildNonFileSourceThrowsGestaltConfigurationException() throws GestaltException {
        ConfigSource source = new StringConfigSource("abc=def", "properties");
        FileConfigReloadStrategyBuilder builder = FileConfigReloadStrategyBuilder.builder();
        builder.setSource(source);

        GestaltConfigurationException exception =  Assertions.assertThrows(GestaltConfigurationException.class, builder::build);
        Assertions.assertEquals("When building a File Change Reload Strategy with the builder the source must be a file",
            exception.getMessage());
    }

    @Test
    void buildWithoutExecutorUsesSingleThreadExecutor() throws GestaltException {
        URL testFileURL = FileConfigReloadStrategyBuilderTest.class.getClassLoader().getResource("test.properties");
        Path filePath = new File(testFileURL.getFile()).toPath();

        ConfigSource source = new FileConfigSource(filePath);

        FileConfigReloadStrategyBuilder builder = FileConfigReloadStrategyBuilder.builder();
        builder.setSource(source);

        try {
            Assertions.assertNotNull(builder.build());
        } catch (GestaltConfigurationException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }
}

