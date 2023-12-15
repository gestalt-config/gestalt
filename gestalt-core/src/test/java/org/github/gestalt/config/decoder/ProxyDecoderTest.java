package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.ManualConfigReloadStrategy;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.*;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

class ProxyDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new BooleanDecoder(), new ProxyDecoder(), new OptionalDecoder()),
            configNodeService, lexer, List.of(new StandardPathMapper()));
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
    void canDecode() {
        ProxyDecoder decoder = new ProxyDecoder();

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Set<Long>>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Map<Long, String>>() {
        }));

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfoInterface.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBPoolInterface.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<DBPoolInterface>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfo.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfoExtended.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
    }

    @Test
    void decode() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoInterface.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfoInterface results = (DBInfoInterface) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeDefaultMethodValues() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterface.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, during navigating to next node",
            validate.getErrors().get(0).description());

        DBInfoInterface results = (DBInfoInterface) validate.results();
        Assertions.assertEquals(10, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadNodeNotAnInt() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("aaaa"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterface.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.port, from node: " +
                "LeafNode{value='aaaa'} attempting to decode Integer",
            validate.getErrors().get(0).description());

        DBInfoInterface results = (DBInfoInterface) validate.results();
        Assertions.assertEquals(10, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeNullLeafNodeValue() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode(null));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterface.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.host.uri, has no value attempting to decode String",
            validate.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(1).level());
        Assertions.assertEquals("Decoding object : DBInfoInterface on path: db.host.uri, field uri results in null value",
            validate.getErrors().get(1).description());

        DBInfoInterface results = (DBInfoInterface) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());

        try {
            results.getUri();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getUri " +
                    "with type: class java.lang.String in path: db.host.",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeNullLeafNode() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", null);
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterface.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, " +
                "during navigating to next node",
            validate.getErrors().get(0).description());

        DBInfoInterface results = (DBInfoInterface) validate.results();
        Assertions.assertEquals(10, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWrongNodeType() {
        ProxyDecoder decoder = new ProxyDecoder();

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new LeafNode("mysql.com"), TypeCapture.of(DBInforNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : LEAF",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() {
        ProxyDecoder decoder = new ProxyDecoder();

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), null,
            TypeCapture.of(DBInforNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : null",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeHttpPool() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));
        configs.put("enabled", new LeafNode("true"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPoolInterface.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.defaultWait, for class: ObjectToken," +
            " during navigating to next node", validate.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.defaultWait, for class: ObjectToken," +
            " during navigating to next node", validate.getErrors().get(0).description());

        DBPoolInterface results = (DBPoolInterface) validate.results();
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
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getDefaultWait " +
                    "with type: float in path: db.host.",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeHttpPoolGeneric() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));
        configs.put("enabled", new LeafNode("true"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPoolGenericInterface.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.defaultWait, for class: ObjectToken," +
            " during navigating to next node", validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(1).level());
        Assertions.assertEquals("Decoding object : DBPoolGenericInterface on path: db.host.defaultWait, " +
            "field defaultWait results in null value", validate.getErrors().get(1).description());

        DBPoolGenericInterface results = (DBPoolGenericInterface) validate.results();
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
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getDefaultWait " +
                    "with type: float in path: db.host.",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeHttpPoolDefault() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));
        configs.put("enabled", new LeafNode("true"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPoolInterfaceDefault.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.defaultWait, for class: ObjectToken," +
            " during navigating to next node", validate.getErrors().get(0).description());

        DBPoolInterfaceDefault results = (DBPoolInterfaceDefault) validate.results();
        Assertions.assertEquals(100, results.getMaxTotal());
        Assertions.assertEquals(10, results.getMaxPerRoute());
        Assertions.assertEquals(60, results.getValidateAfterInactivity());
        Assertions.assertEquals(123, results.getKeepAliveTimeoutMs());
        Assertions.assertEquals(1000, results.getIdleTimeoutSec());
        Assertions.assertTrue(results.isEnabled());
        Assertions.assertEquals(0.26f, results.getDefaultWait());
    }


    @Test
    void decodeHttpPoolDefaultGeneric() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));
        configs.put("enabled", new LeafNode("true"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPoolInterfaceDefaultGeneric.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.defaultWait, for class: ObjectToken," +
            " during navigating to next node", validate.getErrors().get(0).description());

        DBPoolInterfaceDefaultGeneric results = (DBPoolInterfaceDefaultGeneric) validate.results();
        Assertions.assertEquals(100, results.getMaxTotal());
        Assertions.assertEquals(10, results.getMaxPerRoute());
        Assertions.assertEquals(60, results.getValidateAfterInactivity());
        Assertions.assertEquals(123, results.getKeepAliveTimeoutMs());
        Assertions.assertEquals(1000, results.getIdleTimeoutSec());
        Assertions.assertTrue(results.isEnabled());
        Assertions.assertEquals(List.of(1, 2, 3, 4), results.getDefaultWait());
    }

    @Test
    void decodeAnnotations() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(IDBInfoAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        IDBInfoAnnotations results = (IDBInfoAnnotations) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationsDefault() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(IDBInfoAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        IDBInfoAnnotations results = (IDBInfoAnnotations) validate.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadAnnotationsDefault() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate =
            decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(IDBInfoBadAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(3, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(1).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.channel, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer", validate.getErrors().get(1).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(2).level());
        Assertions.assertEquals("Decoding object : IDBInfoBadAnnotations on path: db.host.channel, " +
            "field channel results in null value", validate.getErrors().get(2).description());

        IDBInfoBadAnnotations results = (IDBInfoBadAnnotations) validate.results();
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());

        try {
            results.getPort();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getPort " +
                    "with type: int in path: db.host.",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeAnnotationsLong() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new MapNode(Map.of("port", new LeafNode("100"))));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(IDBInfoAnnotationsLong.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        IDBInfoAnnotationsLong results = (IDBInfoAnnotationsLong) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationsOnlyDefault() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(IDBInfoMethodAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        IDBInfoMethodAnnotations results = (IDBInfoMethodAnnotations) validate.results();
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
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();


        DBInfoInterface results = gestalt.getConfig("db", DBInfoInterface.class);

        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());

        configs.put("db.port", "200");
        reload.reload();

        Assertions.assertEquals(200, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeReloadDefault() throws GestaltException {

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
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();


        DBInfoInterface results = gestalt.getConfig("db", DBInfoInterface.class);

        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());

        configs.remove("db.port");
        configs.put("db.uri", "postgresql.org");
        reload.reload();

        Assertions.assertEquals(10, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("postgresql.org", results.getUri());
    }

    @Test
    void decodeReloadDAnnotationDefault() throws GestaltException {

        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.channel", "100");
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
            .setTreatNullValuesInClassAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();


        IDBInfoAnnotations results = gestalt.getConfig("db", IDBInfoAnnotations.class);

        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());

        configs.remove("db.channel");
        configs.put("db.uri", "postgresql.org");
        reload.reload();

        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("postgresql.org", results.getUri());
    }
}

