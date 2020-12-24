package org.config.gestalt.loader;

import org.config.gestalt.exceptions.ConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConfigLoaderRegistryTest {

    @Test
    void testLoader() throws ConfigurationException {
        ConfigLoader loader = Mockito.mock(ConfigLoader.class);
        Mockito.when(loader.accepts("test")).thenReturn(true);
        Mockito.when(loader.accepts("noMatch")).thenReturn(false);

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();

        configLoaderRegistry.addLoader(loader);

        Assertions.assertEquals(loader, configLoaderRegistry.getLoader("test"));

        Assertions.assertThrows(ConfigurationException.class,
            () -> configLoaderRegistry.getLoader("noMatch"));
    }

    @Test
    void testMultipleLoaderMatches() throws ConfigurationException {
        ConfigLoader loader = Mockito.mock(ConfigLoader.class);
        Mockito.when(loader.accepts("test")).thenReturn(true);
        Mockito.when(loader.accepts("noMatch")).thenReturn(false);

        ConfigLoader loader2 = Mockito.mock(ConfigLoader.class);
        Mockito.when(loader2.accepts("test")).thenReturn(true);
        Mockito.when(loader2.accepts("noMatch")).thenReturn(false);


        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();

        configLoaderRegistry.addLoader(loader);
        configLoaderRegistry.addLoader(loader2);

        Assertions.assertEquals(loader, configLoaderRegistry.getLoader("test"));

    }
}
