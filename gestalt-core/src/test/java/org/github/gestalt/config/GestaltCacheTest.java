package org.github.gestalt.config;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class GestaltCacheTest {

    final Gestalt mockGestalt = Mockito.mock(Gestalt.class);

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
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(100);

        Integer port = cache.getConfig("db.port", Integer.class);
        Integer port2 = cache.getConfig("db.port", Integer.class);
        Integer port3 = cache.getConfig("db.port", 200, Integer.class);

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);
        Assertions.assertEquals(100, port3);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfig2() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(100);

        Integer port = cache.getConfig("db.port", TypeCapture.of(Integer.class));
        Integer port2 = cache.getConfig("db.port", TypeCapture.of(Integer.class));
        Integer port3 = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);
        Assertions.assertEquals(100, port3);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfigDefault() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfig("db.port", 200, TypeCapture.of(Integer.class), Tags.of())).thenReturn(100);

        Integer port = cache.getConfig("db.port", 200, Integer.class);
        Integer port2 = cache.getConfig("db.port", 200, Integer.class);
        Integer port3 = cache.getConfig("db.port", Integer.class);

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);
        Assertions.assertEquals(100, port3);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", 200, TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfig2Default() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfig("db.port", 200, TypeCapture.of(Integer.class), Tags.of())).thenReturn(100);

        Integer port = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));
        Integer port2 = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));
        Integer port3 = cache.getConfig("db.port", TypeCapture.of(Integer.class));

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);
        Assertions.assertEquals(100, port3);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", 200, TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfigOptional() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(Optional.of(100));

        Optional<Integer> port = cache.getConfigOptional("db.port", Integer.class);
        Optional<Integer> port2 = cache.getConfigOptional("db.port", Integer.class);
        Integer port3 = cache.getConfig("db.port", Integer.class);

        Assertions.assertEquals(100, port.get());
        Assertions.assertEquals(100, port2.get());
        Assertions.assertEquals(100, port3);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfig2Optional() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of()))
               .thenReturn(Optional.of(100));

        Optional<Integer> port = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));
        Optional<Integer> port2 = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));
        Integer port3 = cache.getConfig("db.port", TypeCapture.of(Integer.class));

        Assertions.assertEquals(100, port.get());
        Assertions.assertEquals(100, port2.get());
        Assertions.assertEquals(100, port3);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfig3Optional() {
        GestaltCache cache = new GestaltCache(mockGestalt);
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(Optional.empty());
        Mockito.when(mockGestalt.getConfig("db.port", 200, TypeCapture.of(Integer.class), Tags.of())).thenReturn(200);

        Optional<Integer> port = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));
        Optional<Integer> port2 = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));
        Integer port3 = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));

        Assertions.assertEquals(Optional.empty(), port);
        Assertions.assertEquals(Optional.empty(), port2);
        Assertions.assertEquals(200, port3);

        Mockito.verify(mockGestalt, Mockito.times(2)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", 200, TypeCapture.of(Integer.class), Tags.of());
    }
}
