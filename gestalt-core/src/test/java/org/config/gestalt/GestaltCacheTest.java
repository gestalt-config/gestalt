package org.config.gestalt;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.reflect.TypeCapture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GestaltCacheTest {

    Gestalt mockGestalt = Mockito.mock(Gestalt.class);

    @BeforeEach
    void setUp() {
    }

    @Test
    void loadConfigs() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt);

        cache.loadConfigs();

        Mockito.verify(mockGestalt, Mockito.times(1)).loadConfigs();
    }

    @Test
    void getConfig() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class))).thenReturn(100);

        Integer port = cache.getConfig("db.port", Integer.class);
        Integer port2 = cache.getConfig("db.port", Integer.class);

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class));
    }

    @Test
    void getConfig2() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class))).thenReturn(100);

        Integer port = cache.getConfig("db.port", TypeCapture.of(Integer.class));
        Integer port2 = cache.getConfig("db.port", TypeCapture.of(Integer.class));

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class));
    }

    @Test
    void getConfigDefault() {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfig("db.port", 200, TypeCapture.of(Integer.class))).thenReturn(100);

        Integer port = cache.getConfig("db.port", 200, Integer.class);
        Integer port2 = cache.getConfig("db.port", 200, Integer.class);

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", 200, TypeCapture.of(Integer.class));
    }

    @Test
    void getConfig2Default() {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfig("db.port", 200, TypeCapture.of(Integer.class))).thenReturn(100);

        Integer port = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));
        Integer port2 = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", 200, TypeCapture.of(Integer.class));
    }
}
