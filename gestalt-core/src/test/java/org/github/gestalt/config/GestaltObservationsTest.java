package org.github.gestalt.config;

import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.observations.ObservationRecorder;
import org.github.gestalt.config.observations.TestObservationRecorder;
import org.github.gestalt.config.processor.TestResultProcessor;
import org.github.gestalt.config.processor.TestValidationProcessor;
import org.github.gestalt.config.reload.ManualConfigReloadStrategy;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.test.classes.DBInfoOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GestaltObservationsTest {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = GestaltObservationsTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    public void testMetricsGetOk() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));

        Assertions.assertEquals("db.password", metricsRecorder.metrics.get("db.password").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db.password").data);
        Assertions.assertEquals(Tags.environment("dev"), metricsRecorder.metrics.get("db.password").tags);
    }

    @Test
    public void testMetricsGetOkNoMetricRecorder() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build()).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));
    }

    @Test
    public void testMetricsGetOkNullMetricRecorder() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        var metricRecordersArray = new ArrayList<ObservationRecorder>();
        metricRecordersArray.add(null);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build()).setObservationsEnabled(true).addObservationsRecorders(metricRecordersArray).build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));
    }

    @Test
    public void testMetricsGetCache() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));

        Assertions.assertEquals("db.password", metricsRecorder.metrics.get("db.password").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db.password").data);
        Assertions.assertEquals(Tags.environment("dev"), metricsRecorder.metrics.get("db.password").tags);

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));

        Assertions.assertEquals("db.password", metricsRecorder.metrics.get("db.password").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db.password").data);
        Assertions.assertEquals(Tags.environment("dev"), metricsRecorder.metrics.get("db.password").tags);

        Assertions.assertEquals(1, metricsRecorder.metrics.get("cache.hit").data);
        Assertions.assertEquals(Tags.of(), metricsRecorder.metrics.get("cache.hit").tags);

        Assertions.assertEquals("test2", gestalt.getConfigOptional("db.password", String.class, Tags.environment("dev")).get());

        Assertions.assertEquals("db.password", metricsRecorder.metrics.get("db.password").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db.password").data);
        Assertions.assertEquals(Tags.environment("dev"), metricsRecorder.metrics.get("db.password").tags);

        Assertions.assertEquals(2, metricsRecorder.metrics.get("cache.hit").data);
        Assertions.assertEquals(Tags.of(), metricsRecorder.metrics.get("cache.hit").tags);

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", "abc", String.class, Tags.environment("dev")));

        Assertions.assertEquals("db.password", metricsRecorder.metrics.get("db.password").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db.password").data);
        Assertions.assertEquals(Tags.environment("dev"), metricsRecorder.metrics.get("db.password").tags);

        Assertions.assertEquals(3, metricsRecorder.metrics.get("cache.hit").data);
        Assertions.assertEquals(Tags.of(), metricsRecorder.metrics.get("cache.hit").tags);
    }

    @Test
    public void testMetricsGetMissingRequired() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfo.class));

        Assertions.assertEquals("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfo\n" + " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfo, during object decoding", ex.getMessage());
        Assertions.assertEquals("db", metricsRecorder.metrics.get("db").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db").data);
        Assertions.assertEquals(Tags.of("exception", "org.github.gestalt.config.exceptions.GestaltException"), metricsRecorder.metrics.get("db").tags);

        Assertions.assertEquals(1.0D, metricsRecorder.metrics.get("get.config.missing").data);
        Assertions.assertEquals(Tags.of("optional", "false"), metricsRecorder.metrics.get("get.config.missing").tags);
    }

    @Test
    public void testMetricsGetMissingOptional() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        Assertions.assertTrue(gestalt.getConfig("db", DBInfoOptional.class).getUri().isEmpty());

        Assertions.assertEquals("db", metricsRecorder.metrics.get("db").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db").data);
        Assertions.assertEquals(Tags.of(), metricsRecorder.metrics.get("db").tags);

        Assertions.assertEquals(1.0D, metricsRecorder.metrics.get("get.config.missing").data);
        Assertions.assertEquals(Tags.of("optional", "true"), metricsRecorder.metrics.get("get.config.missing").tags);
    }

    @Test
    public void testMetricsGetMissingRequiredAsOptional() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        Assertions.assertTrue(gestalt.getConfigOptional("db", DBInfo.class).isEmpty());

        Assertions.assertEquals("db", metricsRecorder.metrics.get("db").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db").data);
        Assertions.assertEquals(Tags.of("default", "true"), metricsRecorder.metrics.get("db").tags);

        Assertions.assertEquals(1.0D, metricsRecorder.metrics.get("get.config.missing").data);
        Assertions.assertEquals(Tags.of("optional", "false"), metricsRecorder.metrics.get("get.config.missing").tags);
    }

    @Test
    public void testMetricsGetMissingRequiredAsOptionalFailure() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        Assertions.assertTrue(gestalt.getConfigOptional("db", DBInfo.class).isEmpty());

        Assertions.assertEquals("db", metricsRecorder.metrics.get("db").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db").data);
        Assertions.assertEquals(Tags.of("default", "true"), metricsRecorder.metrics.get("db").tags);

        Assertions.assertEquals(1.0D, metricsRecorder.metrics.get("get.config.missing").data);
        Assertions.assertEquals(Tags.of("optional", "false"), metricsRecorder.metrics.get("get.config.missing").tags);
    }

    @Test
    public void testMetricsGetError() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db.password", Integer.class, Tags.environment("dev")));

        Assertions.assertEquals("Failed getting config path: db.password, for class: java.lang.Integer\n" + " - level: ERROR, message: Unable to parse a number on Path: db.password, from node: LeafNode{value='test2'} " + "attempting to decode Integer", ex.getMessage());
        Assertions.assertEquals("db.password", metricsRecorder.metrics.get("db.password").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db.password").data);
        Assertions.assertEquals(Tags.of("environment", "dev", "exception", "org.github.gestalt.config.exceptions.GestaltException"), metricsRecorder.metrics.get("db.password").tags);

        Assertions.assertEquals(1, metricsRecorder.metrics.get("get.config.error").data);
        Assertions.assertEquals(Tags.of(), metricsRecorder.metrics.get("get.config.error").tags);
    }

    @Test
    public void testMetricsGetWarning() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        Assertions.assertEquals((byte) 't', gestalt.getConfig("db.password", Byte.class, Tags.environment("dev")));

        Assertions.assertEquals("db.password", metricsRecorder.metrics.get("db.password").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db.password").data);
        Assertions.assertEquals(Tags.of("environment", "dev"), metricsRecorder.metrics.get("db.password").tags);

        Assertions.assertEquals(1, metricsRecorder.metrics.get("get.config.warning").data);
        Assertions.assertEquals(Tags.of(), metricsRecorder.metrics.get("get.config.warning").tags);
    }

    @Test
    public void testMetricsGetFailValidation() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).setValidationEnabled(true).addValidator(new TestValidationProcessor(false)).build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfo.class, Tags.environment("dev")));


        Assertions.assertEquals("Validation failed for config path: db, and class: " + "org.github.gestalt.config.test.classes.DBInfo\n" + " - level: ERROR, message: something broke", ex.getMessage());
        Assertions.assertEquals("db", metricsRecorder.metrics.get("db").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db").data);
        Assertions.assertEquals(Tags.of("environment", "dev", "exception", "org.github.gestalt.config.exceptions.GestaltException"), metricsRecorder.metrics.get("db").tags);

        Assertions.assertEquals(1.0, metricsRecorder.metrics.get("get.config.validation.error").data);
        Assertions.assertEquals(Tags.of(), metricsRecorder.metrics.get("get.config.validation.error").tags);
    }

    @Test
    public void testMetricsGetFailValidationOptional() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).addResultProcessor(new TestResultProcessor(false)).build();

        gestalt.loadConfigs();

        Assertions.assertTrue(gestalt.getConfigOptional("db", DBInfo.class, Tags.environment("dev")).isEmpty());

        Assertions.assertEquals("db", metricsRecorder.metrics.get("db").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db").data);
        Assertions.assertEquals(Tags.of("environment", "dev", "default", "true"), metricsRecorder.metrics.get("db").tags);
    }

    @Test
    public void testMetricsReload() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        var metricsRecorder = new TestObservationRecorder(0);
        var reload = new ManualConfigReloadStrategy();

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).addConfigReloadStrategy(reload).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.password", String.class));

        Assertions.assertEquals("db.password", metricsRecorder.metrics.get("db.password").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db.password").data);

        reload.reload();

        Assertions.assertEquals("reload", metricsRecorder.metrics.get("reload").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("reload").data);
        Assertions.assertEquals(Tags.of("source", "mapConfig"), metricsRecorder.metrics.get("reload").tags);
    }

    @Test
    public void testMetricsReloadException() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        var metricsRecorder = new TestObservationRecorder(0);
        var reload = new ManualConfigReloadStrategy();

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).addConfigReloadStrategy(reload).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.password", String.class));

        Assertions.assertEquals("db.password", metricsRecorder.metrics.get("db.password").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("db.password").data);

        configs.put("admin[3a]", "Jane");

        var ex = Assertions.assertThrows(GestaltException.class, () -> reload.reload());

        Assertions.assertEquals("Failed to load configs from source: mapConfig\n" + " - level: ERROR, message: Unable to tokenize element admin[3a] for path: admin[3a]", ex.getMessage());
        Assertions.assertEquals("reload", metricsRecorder.metrics.get("reload").path);
        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("reload").data);
        Assertions.assertEquals(Tags.of("source", "mapConfig", "exception", "org.github.gestalt.config.exceptions.GestaltConfigurationException"), metricsRecorder.metrics.get("reload").tags);
    }

    @Test
    public void testMetricsAddOk() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        gestalt.addConfigSourcePackage(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build());

        Assertions.assertNotNull(metricsRecorder.metrics.get("addSource").data);
        Assertions.assertEquals(Tags.of("source", "mapConfig", "environment", "dev"), metricsRecorder.metrics.get("addSource").tags);
    }

    @Test
    public void testMetricsAddException() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("users[a]", "test2");

        var metricsRecorder = new TestObservationRecorder(0);

        Gestalt gestalt = new GestaltBuilder().addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()).setObservationsRecorders(List.of(metricsRecorder)).setObservationsEnabled(true).build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt
            .addConfigSourcePackage(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs2)
                .setTags(Tags.environment("dev"))
                .build())
        );

        Assertions.assertEquals("Failed to load configs from source: mapConfig\n" +
            " - level: ERROR, message: Unable to tokenize element users[a] for path: users[a]", ex.getMessage());

        Assertions.assertEquals(10.0D, metricsRecorder.metrics.get("addSource").data);
        Assertions.assertEquals(Tags.of("source", "mapConfig", "environment", "dev",
            "exception", "org.github.gestalt.config.exceptions.GestaltConfigurationException"),
            metricsRecorder.metrics.get("addSource").tags);

    }
}
