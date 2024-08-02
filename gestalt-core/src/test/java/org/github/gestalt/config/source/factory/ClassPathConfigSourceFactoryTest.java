package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.source.ClassPathConfigSource;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ClassPathConfigSourceFactoryTest {

    private ClassPathConfigSourceFactory factory;
    private String resource;

    @BeforeEach
    public void setUp() {
        factory = new ClassPathConfigSourceFactory();

        resource = "test.properties";
    }

    @Test
    public void testSupportsSource() {
        Assertions.assertTrue(factory.supportsSource("classPath"));
        Assertions.assertFalse(factory.supportsSource("other"));
    }

    @Test
    public void testBuildWithValidPath() {
        Map<String, String> params = new HashMap<>();
        params.put("resource", resource);

        GResultOf<ConfigSource> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertNotNull(result.results());

        Assertions.assertInstanceOf(ClassPathConfigSource.class, result.results());
    }

    @Test
    public void testBuildWithUnknownParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("unknown", "value");
        params.put("resource", resource);

        GResultOf<ConfigSource> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryUnknownParameter.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.DEBUG, result.getErrors().get(0).level());
        Assertions.assertEquals("Unknown Config Source Factory parameter for: classPath Parameter key: unknown, value: value",
            result.getErrors().get(0).description());
    }

    @Test
    public void testBuildWithException() {
        Map<String, String> params = new HashMap<>();

        GResultOf<ConfigSource> result = factory.build(params);

        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryException.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Exception while building Config Source Factory: classPath, " +
            "exception: Class path resource cannot be null", result.getErrors().get(0).description());
    }
}

