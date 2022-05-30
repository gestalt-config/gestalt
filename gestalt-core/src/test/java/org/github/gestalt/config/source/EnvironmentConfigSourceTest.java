package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class EnvironmentConfigSourceTest {

    @Test
    void testDefaultTransformsLoad() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();

        Assertions.assertTrue(envConfig.hasList());
        Assertions.assertNotNull(envConfig.loadList());
        Assertions.assertTrue(envConfig.loadList().size() > 0);
    }


    @Test
    void format() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
        Assertions.assertEquals("envVars", envConfig.format());
    }

    @Test
    void name() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
        Assertions.assertEquals("envVars", envConfig.name());
    }

    @Test
    void unsupportedStream() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();

        Assertions.assertFalse(envConfig.hasStream());
        Assertions.assertThrows(GestaltException.class, envConfig::loadStream);
    }

    @Test
    void equals() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
        EnvironmentConfigSource envConfig2 = new EnvironmentConfigSource();
        Assertions.assertEquals(envConfig, envConfig);
        Assertions.assertNotEquals(envConfig, envConfig2);
        Assertions.assertNotEquals(envConfig, null);
        Assertions.assertNotEquals(envConfig, 1L);
    }

    @Test
    void hash() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
        Assertions.assertTrue(envConfig.hashCode() != 0);
    }
}

