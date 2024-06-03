package org.github.gestalt.config;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.TagMergingStrategyFallback;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.LogManager;

class GestaltCacheTest {

    final Gestalt mockGestalt = Mockito.mock(Gestalt.class);

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = GestaltCacheTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(mockGestalt);
    }

    @Test
    void loadConfigs() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null, new GestaltConfig(),
            new TagMergingStrategyFallback());

        cache.loadConfigs();

        Mockito.verify(mockGestalt, Mockito.times(1)).loadConfigs();
    }

    @Test
    void getConfig() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(100);

        Integer port = cache.getConfig("db.port", Integer.class);
        Integer port2 = cache.getConfig("db.port", Integer.class);
        Integer port3 = cache.getConfig("db.port", 200, Integer.class);
        Optional<Integer> port4 = cache.getConfigOptional("db.port", Integer.class);

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(100, port4.get());

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfig2() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(100);

        Integer port = cache.getConfig("db.port", TypeCapture.of(Integer.class));
        Integer port2 = cache.getConfig("db.port", TypeCapture.of(Integer.class));
        Integer port3 = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));
        Optional<Integer> port4 = cache.getConfigOptional("db.port", Integer.class);

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(100, port4.get());

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfigTags() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Tags tags = Tags.of("toys", "ball");
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), tags)).thenReturn(100);
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(500);

        Integer port = cache.getConfig("db.port", TypeCapture.of(Integer.class), tags);
        Integer port2 = cache.getConfig("db.port", TypeCapture.of(Integer.class));

        Integer port3 = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class), tags);
        Integer port4 = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));

        Optional<Integer> port5 = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class), tags);
        Optional<Integer> port6 = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));


        Assertions.assertEquals(100, port);
        Assertions.assertEquals(500, port2);
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(500, port4);
        Assertions.assertEquals(100, port5.get());
        Assertions.assertEquals(500, port6.get());

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), tags);
        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfigDefault() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(Optional.of(100));

        Integer port = cache.getConfig("db.port", 200, Integer.class);
        Integer port2 = cache.getConfig("db.port", 200, Integer.class);
        Integer port3 = cache.getConfig("db.port", Integer.class);
        Optional<Integer> port4 = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(100, port4.get());

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfig2Default() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(Optional.of(100));

        Integer port = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));
        Integer port2 = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));
        Integer port3 = cache.getConfig("db.port", TypeCapture.of(Integer.class));
        Optional<Integer> port4 = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(100, port4.get());

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
    }


    @Test
    void getConfigDefaultTags() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Tags tags = Tags.of("toys", "ball");
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), tags)).thenReturn(Optional.of(100));
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(Optional.of(500));

        Integer port = cache.getConfig("db.port", 200, Integer.class, tags);
        Integer port2 = cache.getConfig("db.port", 200, Integer.class);

        Integer port3 = cache.getConfig("db.port", Integer.class, tags);
        Integer port4 = cache.getConfig("db.port", Integer.class);

        Optional<Integer> port5 = cache.getConfigOptional("db.port", Integer.class, tags);
        Optional<Integer> port6 = cache.getConfigOptional("db.port", Integer.class);

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(500, port2);
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(500, port4);

        Assertions.assertEquals(100, port5.get());
        Assertions.assertEquals(500, port6.get());

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), tags);
    }

    @Test
    void getConfigDefaultMissing() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of()))
            .thenReturn(Optional.empty());
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), Tags.of()))
            .thenThrow(new GestaltException("Does Not Exist"));

        Integer port = cache.getConfig("db.port", 100, TypeCapture.of(Integer.class));
        Integer port2 = cache.getConfig("db.port", 300, TypeCapture.of(Integer.class));
        Optional<Integer> port3 = cache.getConfigOptional("db.port", Integer.class);

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(300, port2);
        Assertions.assertTrue(port3.isEmpty());

        GestaltException ex = Assertions.assertThrows(GestaltException.class, () -> cache.getConfig("db.port", Integer.class));
        Assertions.assertEquals("Does Not Exist", ex.getMessage());

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfigOptional() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(Optional.of(100));

        Optional<Integer> port = cache.getConfigOptional("db.port", Integer.class);
        Optional<Integer> port2 = cache.getConfigOptional("db.port", Integer.class);
        Integer port3 = cache.getConfig("db.port", Integer.class);
        Integer port4 = cache.getConfig("db.port", 500, Integer.class);

        Assertions.assertEquals(100, port.get());
        Assertions.assertEquals(100, port2.get());
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(100, port4);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfig2Optional() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of()))
            .thenReturn(Optional.of(100));

        Optional<Integer> port = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));
        Optional<Integer> port2 = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));
        Integer port3 = cache.getConfig("db.port", TypeCapture.of(Integer.class));
        Integer port4 = cache.getConfig("db.port", 500, Integer.class);

        Assertions.assertEquals(100, port.get());
        Assertions.assertEquals(100, port2.get());
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(100, port4);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfigOptionalEmpty() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of()))
            .thenReturn(Optional.empty());
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), Tags.of()))
            .thenThrow(new GestaltException("Does Not Exist"));

        Optional<Integer> port = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));
        Optional<Integer> port2 = cache.getConfigOptional("db.port", TypeCapture.of(Integer.class));
        Integer port3 = cache.getConfig("db.port", 200, TypeCapture.of(Integer.class));

        Assertions.assertEquals(Optional.empty(), port);
        Assertions.assertEquals(Optional.empty(), port2);
        Assertions.assertEquals(200, port3);

        GestaltException ex = Assertions.assertThrows(GestaltException.class, () -> cache.getConfig("db.port", Integer.class));
        Assertions.assertEquals("Does Not Exist", ex.getMessage());


        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfigOptionalTags() throws GestaltException {
        GestaltCache cache = new GestaltCache(mockGestalt, Tags.of(), null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Tags tags = Tags.of("toys", "ball");
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), tags)).thenReturn(Optional.of(100));
        Mockito.when(mockGestalt.getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(Optional.of(500));

        Optional<Integer> port = cache.getConfigOptional("db.port", Integer.class, tags);
        Optional<Integer> port2 = cache.getConfigOptional("db.port", Integer.class);

        Integer port3 = cache.getConfig("db.port", 500, Integer.class, tags);
        Integer port4 = cache.getConfig("db.port", 500, Integer.class);

        Integer port5 = cache.getConfig("db.port", Integer.class, tags);
        Integer port6 = cache.getConfig("db.port", Integer.class);

        Assertions.assertEquals(100, port.get());
        Assertions.assertEquals(500, port2.get());
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(500, port4);
        Assertions.assertEquals(100, port5);
        Assertions.assertEquals(500, port6);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfigOptional("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getConfigDefaultTags2() throws GestaltException {
        Tags defaultTags = Tags.of("env", "dev");
        GestaltCache cache = new GestaltCache(mockGestalt, defaultTags, null,
            new GestaltConfig(), new TagMergingStrategyFallback());
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), defaultTags)).thenReturn(100);
        Mockito.when(mockGestalt.getConfig("db.port", TypeCapture.of(Integer.class), Tags.of())).thenReturn(200);

        Integer port = cache.getConfig("db.port", Integer.class);
        Integer port2 = cache.getConfig("db.port", Integer.class);
        Integer port3 = cache.getConfig("db.port", 200, Integer.class);
        Optional<Integer> port4 = cache.getConfigOptional("db.port", Integer.class);
        Integer port5 = cache.getConfig("db.port", Integer.class, Tags.of());

        Assertions.assertEquals(100, port);
        Assertions.assertEquals(100, port2);
        Assertions.assertEquals(100, port3);
        Assertions.assertEquals(100, port4.get());
        Assertions.assertEquals(200, port5);

        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), defaultTags);
        Mockito.verify(mockGestalt, Mockito.times(1)).getConfig("db.port", TypeCapture.of(Integer.class), Tags.of());
    }

    @Test
    void getListeners() throws GestaltException {
        Tags defaultTags = Tags.of("env", "dev");
        GestaltCache cache = new GestaltCache(mockGestalt, defaultTags, null,
            new GestaltConfig(), new TagMergingStrategyFallback());

        CoreReloadListener listener = () -> {

        };
        cache.registerListener(listener);
        cache.removeListener(listener);

        Mockito.verify(mockGestalt, Mockito.times(1)).registerListener(listener);
        Mockito.verify(mockGestalt, Mockito.times(1)).removeListener(listener);
    }

    @Test
    void debugPrint() throws GestaltException {
        Tags defaultTags = Tags.of("env", "dev");
        GestaltCache cache = new GestaltCache(mockGestalt, defaultTags, null,
            new GestaltConfig(), new TagMergingStrategyFallback());

        Mockito.when(mockGestalt.debugPrint()).thenReturn("test");
        Mockito.when(mockGestalt.debugPrint(Tags.environment("dev"))).thenReturn("dev");

        Assertions.assertEquals("test", cache.debugPrint());
        Assertions.assertEquals("dev", cache.debugPrint(Tags.environment("dev")));
    }
}


