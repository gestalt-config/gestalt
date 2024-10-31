package org.github.gestalt.config;

import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.observations.TestObservationRecorder;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GestaltAnnotationProcessorTest {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = GestaltObservationsTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    public void testSecretAnnotationViaPrint() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123@{secret}");
        configs.put("db.uri", "my.sql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("tags: Tags{[]} = MapNode{db=MapNode{password=LeafNode{value='*****'}, " +
            "port=LeafNode{value='*****'}, uri=LeafNode{value='my.sql.com'}}}", gestalt.debugPrint());
    }

    @Test
    public void testNoCacheAnnotationViaObservability() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123@{noCache}");
        configs.put("db.uri", "my.sql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setObservationsRecorders(List.of(metricsRecorder))
            .setObservationsEnabled(true)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(123, gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(123, gestalt.getConfig("db.port", Integer.class));
        // there should be no cache hits.
        Assertions.assertFalse(metricsRecorder.metrics.containsKey("cache.hit"));
    }

    @Test
    public void testNoCacheAnnotationTrim() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql @{noCache} .com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setObservationsRecorders(List.of(metricsRecorder))
            .setObservationsEnabled(true)
            .setAnnotationTrimWhiteSpace(true)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("my.sql.com", gestalt.getConfig("db", DBInfo.class).getUri());
        Assertions.assertEquals("my.sql.com", gestalt.getConfig("db", DBInfo.class).getUri());
        // there should be no cache hits.
        Assertions.assertFalse(metricsRecorder.metrics.containsKey("cache.hit"));
    }

    @Test
    public void testNoCacheAnnotationNoTrim() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql @{noCache} .com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setObservationsRecorders(List.of(metricsRecorder))
            .setObservationsEnabled(true)
            .setAnnotationTrimWhiteSpace(false)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("my.sql  .com", gestalt.getConfig("db", DBInfo.class).getUri());
        Assertions.assertEquals("my.sql  .com", gestalt.getConfig("db", DBInfo.class).getUri());
        // there should be no cache hits.
        Assertions.assertFalse(metricsRecorder.metrics.containsKey("cache.hit"));
    }

    @Test
    public void testNoCacheAnnotationObjectViaObservability() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123@{noCache}");
        configs.put("db.uri", "my.sql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setObservationsRecorders(List.of(metricsRecorder))
            .setObservationsEnabled(true)
            .build();

        gestalt.loadConfigs();


        Assertions.assertEquals(123, gestalt.getConfig("db", DBInfo.class).getPort());
        Assertions.assertEquals(123, gestalt.getConfig("db", DBInfo.class).getPort());
        // there should be no cache hits.
        Assertions.assertFalse(metricsRecorder.metrics.containsKey("cache.hit"));
    }

    @Test
    public void testTempAnnotationObjectViaObservability() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123@{temp:2}");
        configs.put("db.uri", "my.sql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setObservationsRecorders(List.of(metricsRecorder))
            .setObservationsEnabled(true)
            .build();

        gestalt.loadConfigs();


        Assertions.assertEquals(123, gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(123, gestalt.getConfig("db", DBInfo.class).getPort());
        Assertions.assertTrue(gestalt.getConfigOptional("db.port", Integer.class).isEmpty());
        // there should be no cache hits.
        Assertions.assertFalse(metricsRecorder.metrics.containsKey("cache.hit"));

        Assertions.assertEquals("tags: Tags{[]} = MapNode{db=MapNode{password=LeafNode{value='*****'}, " +
            "port=TemporaryLeafNode{value='*****'}, uri=LeafNode{value='my.sql.com'}}}", gestalt.debugPrint());
    }

    @Test
    public void testEncryptedAnnotationObjectViaObservability() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123@{encrypt}");
        configs.put("db.uri", "my.sql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setObservationsRecorders(List.of(metricsRecorder))
            .setObservationsEnabled(true)
            .build();

        gestalt.loadConfigs();


        Assertions.assertEquals(123, gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(123, gestalt.getConfig("db", DBInfo.class).getPort());
        // there should be no cache hits.
        Assertions.assertFalse(metricsRecorder.metrics.containsKey("cache.hit"));

        Assertions.assertEquals("tags: Tags{[]} = MapNode{db=MapNode{password=LeafNode{value='*****'}, " +
            "port=EncryptedLeafNode{value='*****'}, uri=LeafNode{value='my.sql.com'}}}", gestalt.debugPrint());
    }

    @Test
    public void testSecretAnnotationGetConfigResult() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123@{noCache}");
        configs.put("db.uri", "my.sql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        var results = gestalt.getConfigResult("db.port", TypeCapture.of(Integer.class), Tags.of());

        Assertions.assertEquals(123, results.results());
        Assertions.assertEquals(1, results.getMetadata().size());
        Assertions.assertTrue(results.getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertTrue((boolean) results.getMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }
}
