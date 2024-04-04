package org.github.gestalt.config.loader;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.LogManager;

class ConfigLoaderRegistryTest {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = ConfigLoaderRegistryTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    void testLoader() throws GestaltConfigurationException {
        ConfigLoader loader = Mockito.mock(ConfigLoader.class);
        Mockito.when(loader.accepts("test")).thenReturn(true);
        Mockito.when(loader.accepts("noMatch")).thenReturn(false);

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();

        configLoaderRegistry.addLoader(loader);

        Assertions.assertEquals(loader, configLoaderRegistry.getLoader("test"));

        Assertions.assertThrows(GestaltConfigurationException.class,
            () -> configLoaderRegistry.getLoader("noMatch"));
    }

    @Test
    void testAddLoader() throws GestaltConfigurationException {
        ConfigLoader loader2 = Mockito.mock(ConfigLoader.class);
        Mockito.when(loader2.accepts("test2")).thenReturn(true);
        Mockito.when(loader2.accepts("noMatch")).thenReturn(false);

        ConfigLoader loader3 = Mockito.mock(ConfigLoader.class);
        Mockito.when(loader3.accepts("test3")).thenReturn(true);
        Mockito.when(loader3.accepts("noMatch")).thenReturn(false);

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();

        configLoaderRegistry.addLoaders(List.of(loader2, loader3));

        Assertions.assertEquals(loader2, configLoaderRegistry.getLoader("test2"));

        Assertions.assertThrows(GestaltConfigurationException.class,
            () -> configLoaderRegistry.getLoader("noMatch"));
    }

    @Test
    void testSetAndGetLoader() {
        ConfigLoader loader = Mockito.mock(ConfigLoader.class);
        Mockito.when(loader.accepts("test")).thenReturn(true);
        Mockito.when(loader.accepts("noMatch")).thenReturn(false);

        ConfigLoader loader2 = Mockito.mock(ConfigLoader.class);
        Mockito.when(loader2.accepts("test2")).thenReturn(true);
        Mockito.when(loader2.accepts("noMatch")).thenReturn(false);

        ConfigLoader loader3 = Mockito.mock(ConfigLoader.class);
        Mockito.when(loader3.accepts("test3")).thenReturn(true);
        Mockito.when(loader3.accepts("noMatch")).thenReturn(false);

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();

        configLoaderRegistry.addLoader(loader);
        configLoaderRegistry.setLoaders(List.of(loader2, loader3));
        Assertions.assertEquals(2, configLoaderRegistry.getConfigLoaders().size());

        Assertions.assertThrows(GestaltConfigurationException.class,
            () -> configLoaderRegistry.getLoader("test"));
    }

    @Test
    void testMultipleLoadercanDecode() throws GestaltConfigurationException {
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
