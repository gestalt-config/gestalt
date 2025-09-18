package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.entity.GestaltConfig;
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
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

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

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfoInterfaceDefault.class)));
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
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoInterfaceDefault.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoInterfaceDefault results = (DBInfoInterfaceDefault) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeDefaultMethodValues() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterfaceDefault.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding proxy on path: db.host.port, with node: " +
                "MapNode{password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}, with class: DBInfoInterfaceDefault",
            result.getErrors().get(0).description());

        DBInfoInterfaceDefault results = (DBInfoInterfaceDefault) result.results();
        Assertions.assertEquals(10, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadNodeNotAnInt() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("aaaa"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterfaceDefault.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.port, from node: " +
                "LeafNode{value='aaaa'} attempting to decode Integer",
            result.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Missing Optional Value while decoding proxy on path: db.host.port, with node: " +
                "MapNode{password=LeafNode{value='pass'}, port=LeafNode{value='aaaa'}, uri=LeafNode{value='mysql.com'}}, " +
                "with class: DBInfoInterfaceDefault",
            result.getErrors().get(1).description());

        DBInfoInterfaceDefault results = (DBInfoInterfaceDefault) result.results();
        Assertions.assertEquals(10, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeNullLeafNodeValue() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode(null));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterface.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.host.uri, has no value attempting to decode String",
            result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.uri, for class: DBInfoInterface, during proxy decoding",
            result.getErrors().get(1).description());

        DBInfoInterface results = (DBInfoInterface) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());

        try {
            results.getUri();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getUri " +
                    "with type: class java.lang.String in path: db.host",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeNullLeafNode() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", null);
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterfaceDefault.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding proxy on path: db.host.port, with node: " +
                "MapNode{password=LeafNode{value='pass'}, port='null', uri=LeafNode{value='mysql.com'}}, " +
                "with class: DBInfoInterfaceDefault",
            result.getErrors().get(0).description());

        DBInfoInterfaceDefault results = (DBInfoInterfaceDefault) result.results();
        Assertions.assertEquals(10, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeMissingLeafNodeValueInteger() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        //configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterface2.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: DBInfoInterface2, " +
            "during proxy decoding", result.getErrors().get(0).description());

        DBInfoInterface2 results = (DBInfoInterface2) result.results();
        Assertions.assertEquals("mysql.com", results.getUri());
        Assertions.assertEquals("pass", results.getPassword());

        try {
            results.getPort();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getPort with " +
                "type: class java.lang.Integer in path: db.host", e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeMissingLeafNodeValueInt() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        //configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterface.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: DBInfoInterface, " +
            "during proxy decoding", result.getErrors().get(0).description());

        DBInfoInterface results = (DBInfoInterface) result.results();
        Assertions.assertEquals("mysql.com", results.getUri());
        Assertions.assertEquals("pass", results.getPassword());

        try {
            results.getPort();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: " +
                "getPort with type: int in path: db.host", e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeMissingLeafNodeValueOptionalInteger() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        //configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterfaceOptional.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding proxy on path: db.host.port, with node: " +
                "MapNode{password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}, with class: DBInfoInterfaceOptional",
            result.getErrors().get(0).description());

        DBInfoInterfaceOptional results = (DBInfoInterfaceOptional) result.results();
        Assertions.assertEquals("mysql.com", results.getUri().get());
        Assertions.assertEquals("pass", results.getPassword().get());
        Assertions.assertTrue(results.getPort().isEmpty());
    }

    @Test
    void decodeMissingLeafNodeValueOptionalIntegerAsError() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(true);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        //configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoInterfaceOptional.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding proxy on path: db.host.port, with node: " +
                "MapNode{password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}, with class: DBInfoInterfaceOptional",
            result.getErrors().get(0).description());

        DBInfoInterfaceOptional results = (DBInfoInterfaceOptional) result.results();
        Assertions.assertEquals("mysql.com", results.getUri().get());
        Assertions.assertEquals("pass", results.getPassword().get());

        // this behaviour is a little strange, since we TreatMissingDiscretionaryValuesAsErrors = true, this should
        // throw an error, but it doesn't.
        // instead using gestalt we wouldn't get here as it would throw an error and not give the result.
        Assertions.assertTrue(results.getPort().isEmpty());
    }

    @Test
    void decodeWrongNodeType() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new LeafNode("mysql.com"), TypeCapture.of(DBInfoNoConstructor.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : LEAF",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), null,
            TypeCapture.of(DBInfoNoConstructor.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : null",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeHttpPool() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));
        configs.put("enabled", new LeafNode("true"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPoolInterface.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.defaultWait, for class: DBPoolInterface, " +
            "during proxy decoding", result.getErrors().get(0).description());

        DBPoolInterface results = (DBPoolInterface) result.results();
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
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getDefaultWait " +
                    "with type: float in path: db.host",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeHttpPoolGeneric() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));
        configs.put("enabled", new LeafNode("true"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPoolGenericInterface.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.defaultWait, for class: DBPoolGenericInterface, " +
            "during proxy decoding", result.getErrors().get(0).description());

        DBPoolGenericInterface results = (DBPoolGenericInterface) result.results();
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
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getDefaultWait " +
                    "with type: float in path: db.host",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeHttpPoolGenericWrapper() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));
        configs.put("enabled", new LeafNode("true"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPoolInterfaceWrapper.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());

        assertThat(result.getErrors())
            .anyMatch(item -> item.description().equals("Unable to find node matching path: db.host.isEnabled, " + // NOPMD
                "for class: DBPoolInterfaceWrapper, during proxy decoding") && // NOPMD
                item.level() == ValidationLevel.MISSING_VALUE)
            .anyMatch(item -> item.description().equals("Unable to find node matching path: db.host.isEnabled, " + // NOPMD
                "for class: DBPoolInterfaceWrapper, during proxy decoding") &&  // NOPMD
                item.level() == ValidationLevel.MISSING_VALUE);

        DBPoolInterfaceWrapper results = (DBPoolInterfaceWrapper) result.results();
        Assertions.assertEquals(100, results.getMaxTotal());
        Assertions.assertEquals(10, results.getMaxPerRoute());
        Assertions.assertEquals(60, results.getValidateAfterInactivity());
        Assertions.assertEquals(123, results.getKeepAliveTimeoutMs());
        Assertions.assertEquals(1000, results.getIdleTimeoutSec().get());

        try {
            results.getDefaultWait();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getDefaultWait with type: " +
                    "class java.lang.Float in path: db.host",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeHttpPoolDefault() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));
        configs.put("enabled", new LeafNode("true"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPoolInterfaceDefault.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding proxy on path: db.host.defaultWait, with node: " +
                "MapNode{maxperroute=LeafNode{value='10'}, keepalivetimeoutms=LeafNode{value='123'}, " +
                "idletimeoutsec=LeafNode{value='1000'}, validateafterinactivity=LeafNode{value='60'}, maxtotal=LeafNode{value='100'}, " +
                "enabled=LeafNode{value='true'}}, with class: DBPoolInterfaceDefault",
            result.getErrors().get(0).description());

        DBPoolInterfaceDefault results = (DBPoolInterfaceDefault) result.results();
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
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));
        configs.put("enabled", new LeafNode("true"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPoolInterfaceDefaultGeneric.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding proxy on path: db.host.defaultWait, with node: " +
            "MapNode{maxperroute=LeafNode{value='10'}, keepalivetimeoutms=LeafNode{value='123'}, " +
            "idletimeoutsec=LeafNode{value='1000'}, validateafterinactivity=LeafNode{value='60'}, maxtotal=LeafNode{value='100'}, " +
            "enabled=LeafNode{value='true'}}, with class: DBPoolInterfaceDefaultGeneric", result.getErrors().get(0).description());

        DBPoolInterfaceDefaultGeneric results = (DBPoolInterfaceDefaultGeneric) result.results();
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
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(IDBInfoAnnotations.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        IDBInfoAnnotations results = (IDBInfoAnnotations) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationsDefault() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(IDBInfoAnnotations.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding proxy on path: db.host.channel, with node: " +
                "MapNode{password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}, with class: IDBInfoAnnotations",
            result.getErrors().get(0).description());

        IDBInfoAnnotations results = (IDBInfoAnnotations) result.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadAnnotationsDefault() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result =
            decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(IDBInfoBadAnnotations.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.channel, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer", result.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: IDBInfoBadAnnotations, " +
            "during proxy decoding", result.getErrors().get(1).description());

        IDBInfoBadAnnotations results = (IDBInfoBadAnnotations) result.results();
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());

        try {
            results.getPort();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
            Assertions.assertEquals("Failed to get cached object from proxy config while calling method: getPort " +
                    "with type: int in path: db.host",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeAnnotationsLong() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new MapNode(Map.of("port", new LeafNode("100"))));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(IDBInfoAnnotationsLong.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        IDBInfoAnnotationsLong results = (IDBInfoAnnotationsLong) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationsOnlyDefault() {
        ProxyDecoder decoder = new ProxyDecoder();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setTreatMissingValuesAsErrors(true);
        gestaltConfig.setTreatMissingDiscretionaryValuesAsErrors(false);
        decoder.applyConfig(gestaltConfig);

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(IDBInfoMethodAnnotations.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding proxy on path: db.host.port, with node: " +
                "MapNode{password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}, with class: IDBInfoMethodAnnotations",
            result.getErrors().get(0).description());

        IDBInfoMethodAnnotations results = (IDBInfoMethodAnnotations) result.results();
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
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();


        DBInfoInterfaceDefault results = gestalt.getConfig("db", DBInfoInterfaceDefault.class);

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
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();


        DBInfoInterfaceDefault results = gestalt.getConfig("db", DBInfoInterfaceDefault.class);

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
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterfaceOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding proxy on path: db.uri, with node: " +
                "MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, with class: DBInfoInterfaceOptional");

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
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterfaceOptional\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding proxy on path: db.uri, with node: " +
                "MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, with class: DBInfoInterfaceOptional");
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
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterface2\n" +
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
            .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoInterface2\n" +
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
    public void testInterfaceIntResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        //configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterface dbInfo = gestalt.getConfig("db", DBInfoInterface.class);
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(0, dbInfo.getPort());
            Assertions.assertEquals("mysql.com", dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfaceIntegerResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        //configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

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
            Assertions.assertNull(dbInfo.getPort());
            Assertions.assertEquals("mysql.com", dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testInterfaceOptionalIntegerResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        //configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

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
            Assertions.assertTrue(dbInfo.getPort().isEmpty());
            Assertions.assertEquals("mysql.com", dbInfo.getUri().get());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testReloadInterfaceIntResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        //configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

        var manualReload = new ManualConfigReloadStrategy();
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(manualReload)
                .build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterface dbInfo = gestalt.getConfig("db", DBInfoInterface.class);

            manualReload.reload();
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(0, dbInfo.getPort());
            Assertions.assertEquals("mysql.com", dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }


    @Test
    public void testReloadInterfaceIntegerResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        //configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

        var manualReload = new ManualConfigReloadStrategy();
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(manualReload)
                .build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterface2 dbInfo = gestalt.getConfig("db", DBInfoInterface2.class);

            manualReload.reload();
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(null, dbInfo.getPort());
            Assertions.assertEquals("mysql.com", dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testReloadInterfaceOptionalResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        //configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

        var manualReload = new ManualConfigReloadStrategy();
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(manualReload)
                .build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterfaceOptional dbInfo = gestalt.getConfig("db", DBInfoInterfaceOptional.class);

            manualReload.reload();
            Assertions.assertEquals("test", dbInfo.getPassword().get());
            Assertions.assertTrue(dbInfo.getPort().isEmpty());
            Assertions.assertEquals("mysql.com", dbInfo.getUri().get());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testReloadInterfaceDefaultResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        //configs.put("db.port", "3306");
        configs.put("db.uri", "mysql.com");

        var manualReload = new ManualConfigReloadStrategy();
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(manualReload)
                .build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoInterfaceDefault dbInfo = gestalt.getConfig("db", DBInfoInterfaceDefault.class);

            manualReload.reload();
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(10, dbInfo.getPort());
            Assertions.assertEquals("mysql.com", dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }
}


