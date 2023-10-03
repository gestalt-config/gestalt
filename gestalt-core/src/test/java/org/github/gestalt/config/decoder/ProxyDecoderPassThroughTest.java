package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reload.ManualConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.test.classes.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ProxyDecoderPassThroughTest {

    @BeforeEach
    void setup() throws GestaltConfigurationException {

    }

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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();


        DBInfoInterface results = gestalt.getConfig("db", DBInfoInterface.class);

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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();
        gestalt.loadConfigs();

        DBInfoInterface results = gestalt.getConfig("db", DBInfoInterface.class);

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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();
        gestalt.loadConfigs();

        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoInterface.class));

        Assertions.assertEquals("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterface\n" +
                " - level: ERROR, message: Unable to parse a number on Path: db.port, from node: LeafNode{value='aaaa'} " +
                "attempting to decode Integer",
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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();
        gestalt.loadConfigs();

        DBPoolInterface results = gestalt.getConfig("db", DBPoolInterface.class);

        Assertions.assertEquals(100, results.getMaxTotal());
        Assertions.assertEquals(10, results.getMaxPerRoute());
        Assertions.assertEquals(60, results.getValidateAfterInactivity());
        Assertions.assertEquals(123, results.getKeepAliveTimeoutMs());
        Assertions.assertEquals(1000, results.getIdleTimeoutSec());
        Assertions.assertTrue(results.isEnabled());

        try {
            results.getDefaultWait();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals("Failed to get pass through object from proxy config while calling method: getDefaultWait " +
                    "with type: float in path: db.",
                e.getUndeclaredThrowable().getMessage());
        }
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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();
        gestalt.loadConfigs();

        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> gestalt.getConfig("db", DBPoolInterface.class));

        Assertions.assertEquals("Failed getting config path: db, for class: " +
                "org.github.gestalt.config.test.classes.DBPoolInterface\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.defaultWait, for class: ObjectToken, " +
                "during navigating to next node\n" +
                " - level: ERROR, message: Decoding object : DBPoolInterface on path: db.defaultWait, " +
                "field defaultWait results in null value",
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
        configs.put("db.enabled", "true");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();
        gestalt.loadConfigs();

        DBPoolGenericInterface results = gestalt.getConfig("db", DBPoolGenericInterface.class);

        Assertions.assertEquals(100, results.getMaxTotal());
        Assertions.assertEquals(10, results.getMaxPerRoute());
        Assertions.assertEquals(60, results.getValidateAfterInactivity());
        Assertions.assertEquals(123, results.getKeepAliveTimeoutMs());
        Assertions.assertEquals(1000, results.getIdleTimeoutSec().get());
        Assertions.assertTrue(results.isEnabled());

        try {
            results.getDefaultWait();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals("Failed to get pass through object from proxy config while calling method: getDefaultWait " +
                    "with type: float in path: db.",
                e.getUndeclaredThrowable().getMessage());
        }
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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();
        gestalt.loadConfigs();

        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> gestalt.getConfig("db", IDBInfoBadAnnotations.class));
        Assertions.assertEquals("Failed getting config path: db, for class: " +
            "org.github.gestalt.config.test.classes.IDBInfoBadAnnotations\n" +
            " - level: MISSING_VALUE, message: Unable to find node matching path: db.channel, for class: ObjectToken, " +
            "during navigating to next node\n" +
            " - level: ERROR, message: Unable to parse a number on Path: db.channel, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer\n" +
            " - level: ERROR, message: Decoding object : IDBInfoBadAnnotations on path: db.channel, field channel " +
            "results in null value", exception.getMessage());
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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
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
            .addSource(new MapConfigSource(configs))
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
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

        ConfigSource cs = new MapConfigSource(configs);
        ManualConfigReloadStrategy reload = new ManualConfigReloadStrategy(cs);

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(cs)
            .addReloadStrategy(reload)
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        DBInfoInterface results = gestalt.getConfig("db", DBInfoInterface.class);

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
}

