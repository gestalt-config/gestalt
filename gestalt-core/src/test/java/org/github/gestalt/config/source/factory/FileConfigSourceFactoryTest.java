package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.FileConfigSource;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FileConfigSourceFactoryTest {

    private FileConfigSourceFactory factory;
    private File file;

    @BeforeEach
    public void setUp() {
        factory = new FileConfigSourceFactory();

        URL testFileURL = FileConfigSourceFactoryTest.class.getClassLoader().getResource("test.properties");
        file = new File(testFileURL.getFile());
    }

    @Test
    public void testSupportsSource() {
        Assertions.assertTrue(factory.supportsSource("file"));
        Assertions.assertFalse(factory.supportsSource("other"));
    }

    @Test
    public void testBuildWithValidPath() {
        Map<String, String> params = new HashMap<>();
        params.put("path", file.getAbsolutePath());

        GResultOf<ConfigSource> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertNotNull(result.results());

        Assertions.assertInstanceOf(FileConfigSource.class, result.results());
        Assertions.assertEquals(file.getAbsolutePath(), ((FileConfigSource) result.results()).getPath().toString());
    }

    @Test
    public void testBuildWithValidFile() {

        Map<String, String> params = new HashMap<>();
        params.put("file", file.getAbsolutePath());

        GResultOf<ConfigSource> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertNotNull(result.results());

        Assertions.assertInstanceOf(FileConfigSource.class, result.results());
        Assertions.assertEquals(file.getAbsolutePath(), ((FileConfigSource) result.results()).getPath().toString());
    }

    @Test
    public void testBuildWithUnknownParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("unknown", "value");
        params.put("file", file.getAbsolutePath());

        GResultOf<ConfigSource> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryUnknownParameter.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.DEBUG, result.getErrors().get(0).level());
        Assertions.assertEquals("Unknown Config Source Factory parameter for: file Parameter key: unknown, value: value",
            result.getErrors().get(0).description());
    }

    @Test
    public void testBuildWithException() {
        Map<String, String> params = new HashMap<>();
        params.put("path", "/invalid/path");

        GResultOf<ConfigSource> result = factory.build(params);

        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryException.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertTrue(result.getErrors().get(0).description().startsWith("Exception while building Config Source Factory: file, " +
            "exception: File does not exist from path"));
    }
}

