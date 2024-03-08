package org.github.gestalt.config;

import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.decoder.ProxyDecoderMode;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.model.DBInfoInterface2;
import org.github.gestalt.config.model.DBInfoInterfaceOptional;
import org.github.gestalt.config.model.DBInfoOptional;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.test.classes.DBInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MissingValuesConsistencyTest {

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }


    @Test
    public void testPrimitiveOptionalStringForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        try {
            Optional<String> uri = gestalt.getConfig("db.uri", new TypeCapture<>() {
            });
            Assertions.assertTrue(uri.isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testPrimitiveOptionalStringResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        try {
            Optional<String> uri = gestalt.getConfig("db.uri", new TypeCapture<>() {
            });
            Assertions.assertTrue(uri.isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }

    }

    @Test
    public void testPrimitiveOptionalStringResultsForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        try {
            Optional<String> uri = gestalt.getConfig("db", new TypeCapture<>() {
            });
            Assertions.assertTrue(uri.isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testPrimitiveOptionalStringResultsForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        try {
            Optional<String> uri = gestalt.getConfig("db.uri", new TypeCapture<>() {
            });
            Assertions.assertTrue(uri.isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testPrimitiveResultsStringForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db.uri", new TypeCapture<String>() {
        }));
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db.uri, for class: java.lang.String\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: ObjectToken, " +
                "during navigating to next node");
    }

    @Test
    public void testPrimitiveResultsStringForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db.uri", String.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db.uri, for class: java.lang.String\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: ObjectToken, " +
                "during navigating to next node");

    }

    @Test
    public void testPrimitiveResultsStringForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db.uri", String.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db.uri, for class: java.lang.String\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: ObjectToken, " +
                "during navigating to next node");
    }

    @Test
    public void testPrimitiveResultsStringForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();


        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db.uri", String.class));
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db.uri, for class: java.lang.String\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: ObjectToken, " +
                "during navigating to next node");

    }

    @Test
    public void testObjectOptionalResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoOptional dbInfo = gestalt.getConfig("db", DBInfoOptional.class);
            Assertions.assertEquals("test", dbInfo.getPassword().get());
            Assertions.assertEquals(3306, dbInfo.getPort().get());
            Assertions.assertTrue(dbInfo.getUri().isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testObjectOptionalResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoOptional.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding Object on path: db.uri, from node: " +
                "MapNode{mapNode={password=LeafNode{value='test'}, port=LeafNode{value='3306'}}}, with class: DBInfoOptional");

    }

    @Test
    public void testObjectOptionalResultsForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoOptional dbInfo = gestalt.getConfig("db", DBInfoOptional.class);
            Assertions.assertEquals("test", dbInfo.getPassword().get());
            Assertions.assertEquals(3306, dbInfo.getPort().get());
            Assertions.assertTrue(dbInfo.getUri().isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testObjectOptionalResultsForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoOptional.class));
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding Object on path: db.uri, from node: " +
                "MapNode{mapNode={password=LeafNode{value='test'}, port=LeafNode{value='3306'}}}, with class: DBInfoOptional");
    }

    @Test
    public void testObjectResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfo dbInfo = gestalt.getConfig("db", DBInfo.class);
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(3306, dbInfo.getPort());
            Assertions.assertNull(dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testObjectResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfo.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfo\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfo, during object decoding");

    }

    @Test
    public void testObjectResultsForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfo.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfo\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfo, during object decoding");
    }

    @Test
    public void testObjectResultsForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfo dbInfo = gestalt.getConfig("db", DBInfo.class);
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(3306, dbInfo.getPort());
            Assertions.assertNull(dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfaceOptionalResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterfaceOptional dbInfo = gestalt.getConfig("db", DBInfoInterfaceOptional.class);
            Assertions.assertEquals("test", dbInfo.getPassword().get());
            Assertions.assertEquals(3306, dbInfo.getPort().get());
            Assertions.assertTrue(dbInfo.getUri().isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfaceOptionalResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoInterfaceOptional.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoInterfaceOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding proxy on path: db.uri, from node: " +
                "MapNode{mapNode={password=LeafNode{value='test'}, port=LeafNode{value='3306'}}}, with class: DBInfoInterfaceOptional");

    }

    @Test
    public void testInterfaceOptionalResultsForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterfaceOptional dbInfo = gestalt.getConfig("db", DBInfoInterfaceOptional.class);
            Assertions.assertEquals("test", dbInfo.getPassword().get());
            Assertions.assertEquals(3306, dbInfo.getPort().get());
            Assertions.assertTrue(dbInfo.getUri().isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfaceOptionalResultsForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoInterfaceOptional.class));
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoInterfaceOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding proxy on path: db.uri, from node: " +
                "MapNode{mapNode={password=LeafNode{value='test'}, port=LeafNode{value='3306'}}}, with class: DBInfoInterfaceOptional");
    }

    @Test
    public void testInterfaceResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterface2 dbInfo = gestalt.getConfig("db", DBInfoInterface2.class);
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(3306, dbInfo.getPort());
            Assertions.assertNull(dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfaceResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoInterface2.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoInterface2\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfoInterface2, " +
                "during proxy decoding");

    }

    @Test
    public void testInterfaceResultsForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoInterface2.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoInterface2\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfoInterface2, " +
                "during proxy decoding");
    }

    @Test
    public void testInterfaceResultsForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterface2 dbInfo = gestalt.getConfig("db", DBInfoInterface2.class);
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(3306, dbInfo.getPort());
            Assertions.assertNull(dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfacePassThroughOptionalResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterfaceOptional dbInfo = gestalt.getConfig("db", DBInfoInterfaceOptional.class);
            Assertions.assertEquals("test", dbInfo.getPassword().get());
            Assertions.assertEquals(3306, dbInfo.getPort().get());
            Assertions.assertTrue(dbInfo.getUri().isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfacePassThroughOptionalResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoInterfaceOptional.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoInterfaceOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding proxy on path: db.uri, from node: " +
                "MapNode{mapNode={password=LeafNode{value='test'}, port=LeafNode{value='3306'}}}, with class: DBInfoInterfaceOptional");

    }

    @Test
    public void testInterfacePassThroughOptionalResultsForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterfaceOptional dbInfo = gestalt.getConfig("db", DBInfoInterfaceOptional.class);
            Assertions.assertEquals("test", dbInfo.getPassword().get());
            Assertions.assertEquals(3306, dbInfo.getPort().get());
            Assertions.assertTrue(dbInfo.getUri().isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfacePassThroughOptionalResultsForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoInterfaceOptional.class));
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoInterfaceOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding proxy on path: db.uri, from node: " +
                "MapNode{mapNode={password=LeafNode{value='test'}, port=LeafNode{value='3306'}}}, with class: DBInfoInterfaceOptional");
    }

    @Test
    public void testInterfacePassThroughResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterface2 dbInfo = gestalt.getConfig("db", DBInfoInterface2.class);
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(3306, dbInfo.getPort());
            Assertions.assertNull(dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfacePassThroughResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoInterface2.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoInterface2\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfoInterface2, " +
                "during proxy decoding");

    }

    @Test
    public void testInterfacePassThroughResultsForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoInterface2.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.model.DBInfoInterface2\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfoInterface2, " +
                "during proxy decoding");
    }

    @Test
    public void testInterfacePassThroughResultsForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterface2 dbInfo = gestalt.getConfig("db", DBInfoInterface2.class);
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(3306, dbInfo.getPort());
            Assertions.assertNull(dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }


    @Test
    public void testRecordOptionalResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        try {
            ValidMissingValuesTest.DBInfoOptionalRecord dbInfo = gestalt.getConfig("db", ValidMissingValuesTest.DBInfoOptionalRecord.class);
            Assertions.assertEquals("test", dbInfo.password().get());
            Assertions.assertEquals(3306, dbInfo.port().get());
            Assertions.assertTrue(dbInfo.uri().isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testRecordOptionalResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", ValidMissingValuesTest.DBInfoOptionalRecord.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.ValidMissingValuesTest$DBInfoOptionalRecord\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding Record on path: db.uri, from node: " +
                "MapNode{mapNode={password=LeafNode{value='test'}, port=LeafNode{value='3306'}}}, with class: DBInfoOptionalRecord");

    }

    @Test
    public void testRecordOptionalResultsForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        try {
            ValidMissingValuesTest.DBInfoOptionalRecord dbInfo = gestalt.getConfig("db", ValidMissingValuesTest.DBInfoOptionalRecord.class);
            Assertions.assertEquals("test", dbInfo.password().get());
            Assertions.assertEquals(3306, dbInfo.port().get());
            Assertions.assertTrue(dbInfo.uri().isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testRecordOptionalResultsForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", ValidMissingValuesTest.DBInfoOptionalRecord.class));
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.ValidMissingValuesTest$DBInfoOptionalRecord\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding Record on path: db.uri, from node: " +
                "MapNode{mapNode={password=LeafNode{value='test'}, port=LeafNode{value='3306'}}}, with class: DBInfoOptionalRecord");
    }

    @Test
    public void testRecordResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        try {
            ValidMissingValuesTest.DBInfoRecord dbInfo = gestalt.getConfig("db", ValidMissingValuesTest.DBInfoRecord.class);
            Assertions.assertEquals("test", dbInfo.password());
            Assertions.assertEquals(3306, dbInfo.port());
            Assertions.assertNull(dbInfo.uri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testRecordResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", ValidMissingValuesTest.DBInfoRecord.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.ValidMissingValuesTest$DBInfoRecord\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfoRecord, " +
                "during record decoding");

    }

    @Test
    public void testRecordResultsForMissingOkNullFailMissingValuesAsErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", ValidMissingValuesTest.DBInfoRecord.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.ValidMissingValuesTest$DBInfoRecord\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfoRecord, " +
                "during record decoding");
    }

    @Test
    public void testRecordResultsForMissingFailMissingValuesAsNotErrors() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        try {
            ValidMissingValuesTest.DBInfoRecord dbInfo = gestalt.getConfig("db", ValidMissingValuesTest.DBInfoRecord.class);
            Assertions.assertEquals("test", dbInfo.password());
            Assertions.assertEquals(3306, dbInfo.port());
            Assertions.assertNull(dbInfo.uri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }
}
