package org.config.gestalt.source;

import org.config.gestalt.exceptions.GestaltException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class EnvironmentConfigSourceTest {

    @Test
    void testDefaultTransformsLoad() throws GestaltException {
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
}

