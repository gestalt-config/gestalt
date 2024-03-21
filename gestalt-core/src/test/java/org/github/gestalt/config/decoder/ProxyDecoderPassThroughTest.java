package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reload.ManualConfigReloadStrategy;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.test.classes.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyDecoderPassThroughTest {

    @Test
    void name() {
        ProxyDecoder decoder = new ProxyDecoder();
        Assertions.assertEquals("proxy", decoder.name());
    }

    @Test
    void priority() {
        ProxyDecoder decoder = new ProxyDecoder();
        Assertions.assertEquals(Priority.LOW, decoder.priority());
    }

    @Test
    void decode() throws GestaltException {

        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.port", "100");
        configs.put("db.uri", "mysql.com");
        configs.put("db.password", "pass");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        DBInfoInterfaceDefault results = gestalt.getConfig("db", DBInfoInterfaceDefault.class);

        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeDefaultMethodValues() throws GestaltException {

        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.uri", "mysql.com");
        configs.put("db.password", "pass");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        DBInfoInterfaceDefault results = gestalt.getConfig("db", DBInfoInterfaceDefault.class);

        Assertions.assertEquals(10, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadNodeNotAnInt() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.port", "aaaa");
        configs.put("db.uri", "mysql.com");
        configs.put("db.password", "pass");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> gestalt.getConfig("db", DBInfoInterfaceDefault.class));

        Assertions.assertEquals(
            "Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterfaceDefault\n" +
                " - level: ERROR, message: Unable to parse a number on Path: db.port, from node: LeafNode{value='aaaa'} " +
                "attempting to decode Integer\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding proxy on path: db.port, with node: " +
                "MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='aaaa'}, uri=LeafNode{value='mysql.com'}}, " +
                "with class: DBInfoInterfaceDefault",
            exception.getMessage());
    }

    @Test
    void decodeDbPool() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.maxtotal", "100");
        configs.put("db.maxperroute", "10");
        configs.put("db.validateafterinactivity", "60");
        configs.put("db.keepalivetimeoutms", "123");
        configs.put("db.idletimeoutsec", "1000");
        configs.put("db.enabled", "true");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .setTreatMissingValuesAsErrors(false)
            .build();
        gestalt.loadConfigs();

        DBPoolInterface results = gestalt.getConfig("db", DBPoolInterface.class);

        Assertions.assertEquals(100, results.getMaxTotal());
        Assertions.assertEquals(10, results.getMaxPerRoute());
        Assertions.assertEquals(60, results.getValidateAfterInactivity());
        Assertions.assertEquals(123, results.getKeepAliveTimeoutMs());
        Assertions.assertEquals(1000, results.getIdleTimeoutSec());
        Assertions.assertTrue(results.isEnabled());

        Assertions.assertEquals(0.0f, results.getDefaultWait());
    }

    @Test
    void decodeDbPoolMissingException() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.maxtotal", "100");
        configs.put("db.maxperroute", "10");
        configs.put("db.validateafterinactivity", "60");
        configs.put("db.keepalivetimeoutms", "123");
        configs.put("db.idletimeoutsec", "1000");
        configs.put("db.enabled", "true");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> gestalt.getConfig("db", DBPoolInterface.class));

        Assertions.assertEquals("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBPoolInterface\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.defaultWait, for class: DBPoolInterface, " +
                "during proxy decoding",
            exception.getMessage());
    }

    @Test
    void decodeHttpPoolGeneric() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.maxtotal", "100");
        configs.put("db.maxperroute", "10");
        configs.put("db.validateafterinactivity", "60");
        configs.put("db.keepalivetimeoutms", "123");
        configs.put("db.idletimeoutsec", "1000");
        //configs.put("db.defaultWait", "30.1");
        configs.put("db.enabled", "true");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder
                .builder()
                .setCustomConfig(configs)
                .build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .setTreatMissingValuesAsErrors(false)
            .build();
        gestalt.loadConfigs();

        DBPoolGenericInterface results = gestalt.getConfig("db", DBPoolGenericInterface.class);

        Assertions.assertEquals(100, results.getMaxTotal());
        Assertions.assertEquals(10, results.getMaxPerRoute());
        Assertions.assertEquals(60, results.getValidateAfterInactivity());
        Assertions.assertEquals(123, results.getKeepAliveTimeoutMs());
        //Assertions.assertEquals(30.1f, results.getDefaultWait());
        Assertions.assertEquals(1000, results.getIdleTimeoutSec().get());
        Assertions.assertTrue(results.isEnabled());
        Assertions.assertEquals(0.0f, results.getDefaultWait());
    }

    @Test
    void decodeHttpPoolDefault() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.maxtotal", "100");
        configs.put("db.maxperroute", "10");
        configs.put("db.validateafterinactivity", "60");
        configs.put("db.keepalivetimeoutms", "123");
        configs.put("db.idletimeoutsec", "1000");
        configs.put("db.enabled", "true");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        DBPoolInterfaceDefault results = gestalt.getConfig("db", DBPoolInterfaceDefault.class);

        Assertions.assertEquals(100, results.getMaxTotal());
        Assertions.assertEquals(10, results.getMaxPerRoute());
        Assertions.assertEquals(60, results.getValidateAfterInactivity());
        Assertions.assertEquals(123, results.getKeepAliveTimeoutMs());
        Assertions.assertEquals(1000, results.getIdleTimeoutSec());
        Assertions.assertTrue(results.isEnabled());
        Assertions.assertEquals(0.26f, results.getDefaultWait());
    }


    @Test
    void decodeHttpPoolDefaultGeneric() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.maxtotal", "100");
        configs.put("db.maxperroute", "10");
        configs.put("db.validateafterinactivity", "60");
        configs.put("db.keepalivetimeoutms", "123");
        configs.put("db.idletimeoutsec", "1000");
        configs.put("db.enabled", "true");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        DBPoolInterfaceDefaultGeneric results = gestalt.getConfig("db", DBPoolInterfaceDefaultGeneric.class);

        Assertions.assertEquals(100, results.getMaxTotal());
        Assertions.assertEquals(10, results.getMaxPerRoute());
        Assertions.assertEquals(60, results.getValidateAfterInactivity());
        Assertions.assertEquals(123, results.getKeepAliveTimeoutMs());
        Assertions.assertEquals(1000, results.getIdleTimeoutSec());
        Assertions.assertTrue(results.isEnabled());
        Assertions.assertEquals(List.of(1, 2, 3, 4), results.getDefaultWait());
    }

    @Test
    void decodeAnnotations() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.channel", "100");
        configs.put("db.uri", "mysql.com");
        configs.put("db.password", "pass");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        IDBInfoAnnotations results = gestalt.getConfig("db", IDBInfoAnnotations.class);

        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationsDefault() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.uri", "mysql.com");
        configs.put("db.password", "pass");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        IDBInfoAnnotations results = gestalt.getConfig("db", IDBInfoAnnotations.class);

        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadAnnotationsDefault() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.uri", "mysql.com");
        configs.put("db.password", "pass");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> gestalt.getConfig("db", IDBInfoBadAnnotations.class));
        Assertions.assertEquals(
            "Failed getting config path: db, for class: org.github.gestalt.config.test.classes.IDBInfoBadAnnotations\n" +
            " - level: ERROR, message: Unable to parse a number on Path: db.channel, from node: LeafNode{value='abc'} " +
                "attempting to decode Integer\n" +
            " - level: MISSING_VALUE, message: Unable to find node matching path: db.channel, for class: IDBInfoBadAnnotations, " +
                "during proxy decoding", exception.getMessage());
    }

    @Test
    void decodeAnnotationsLong() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.channel.port", "100");
        configs.put("db.uri", "mysql.com");
        configs.put("db.password", "pass");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        IDBInfoAnnotationsLong results = gestalt.getConfig("db", IDBInfoAnnotationsLong.class);

        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationsOnlyDefault() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.uri", "mysql.com");
        configs.put("db.password", "pass");


        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();
        gestalt.loadConfigs();

        IDBInfoMethodAnnotations results = gestalt.getConfig("db", IDBInfoMethodAnnotations.class);

        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }


    @Test
    void decodeReload() throws GestaltException {

        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.port", "100");
        configs.put("db.uri", "mysql.com");
        configs.put("db.password", "pass");

        ManualConfigReloadStrategy reload = new ManualConfigReloadStrategy();

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(reload)
                .build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        DBInfoInterfaceDefault results = gestalt.getConfig("db", DBInfoInterfaceDefault.class);

        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());

        configs.put("db.port", "200");
        configs.put("db.password", "thequickbrownfox");
        reload.reload();

        Assertions.assertEquals(200, results.getPort());
        Assertions.assertEquals("thequickbrownfox", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
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
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterfaceOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding proxy on path: db.uri, with node: " +
                "MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, with class: DBInfoInterfaceOptional");

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
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterfaceOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding proxy on path: db.uri, with node: " +
                "MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, with class: DBInfoInterfaceOptional");
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
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterface2\n" +
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
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterface2\n" +
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
    public void testInterfacePassThroughExceptionOnProxyCall() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

        var reload = new ManualConfigReloadStrategy();
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(reload)
                .build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        try {
            // get the config when we have no errors.
            DBInfoInterface2 dbInfo = gestalt.getConfig("db", DBInfoInterface2.class);

            // then reload with errors.
            configs.remove("db.uri");
            reload.reload();

            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(3306, dbInfo.getPort());

            try {
                dbInfo.getUri();
                Assertions.fail("Should throw an exception");
            } catch (UndeclaredThrowableException e) {
                Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
                Assertions.assertEquals("Failed to get pass through object from proxy config while calling method: getUri with " +
                        "type: class java.lang.String in path: db.",
                    e.getUndeclaredThrowable().getMessage());
            }
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfacePassThroughExceptionWithIntegerOnProxyCall() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

        var reload = new ManualConfigReloadStrategy();
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(reload)
                .build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        try {
            // get the config when we have no errors.
            DBInfoInterface2 dbInfo = gestalt.getConfig("db", DBInfoInterface2.class);

            // then reload with errors.
            configs.remove("db.port");
            reload.reload();

            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals("mysql.com", dbInfo.getUri());

            try {
                dbInfo.getPort();
                Assertions.fail("Should throw an exception");
            } catch (UndeclaredThrowableException e) {
                Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
                Assertions.assertEquals("Failed to get pass through object from proxy config while calling method: getPort " +
                        "with type: class java.lang.Integer in path: db.",
                    e.getUndeclaredThrowable().getMessage());
            }
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfacePassThroughExceptionWithIntOnProxyCall() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

        var reload = new ManualConfigReloadStrategy();
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(reload)
                .build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        try {
            // get the config when we have no errors.
            DBInfoInterface dbInfo = gestalt.getConfig("db", DBInfoInterface.class);

            // then reload with errors.
            configs.remove("db.port");
            reload.reload();

            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals("mysql.com", dbInfo.getUri());

            try {
                dbInfo.getPort();
                Assertions.fail("Should throw an exception");
            } catch (UndeclaredThrowableException e) {
                Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
                Assertions.assertEquals("Failed to get pass through object from proxy config while calling method: getPort with " +
                        "type: int in path: db.",
                    e.getUndeclaredThrowable().getMessage());
            }
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfacePassThroughExceptionWithOptionalOnProxyCall() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

        var reload = new ManualConfigReloadStrategy();
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(reload)
                .build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        try {
            // get the config when we have no errors.
            DBInfoInterfaceOptional dbInfo = gestalt.getConfig("db", DBInfoInterfaceOptional.class);

            // then reload with errors.
            configs.remove("db.port");
            reload.reload();

            Assertions.assertEquals("test", dbInfo.getPassword().get());
            Assertions.assertEquals("mysql.com", dbInfo.getUri().get());

            try {
                dbInfo.getPort();
                Assertions.fail("Should throw an exception");
            } catch (UndeclaredThrowableException e) {
                Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
                Assertions.assertEquals("Failed to get pass through object from proxy config while calling method: getPort with " +
                        "type: class java.util.Optional in path: db.",
                    e.getUndeclaredThrowable().getMessage());
            }
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

}

