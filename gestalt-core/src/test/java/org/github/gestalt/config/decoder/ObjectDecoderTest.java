package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.*;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new OptionalDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        ObjectDecoder decoder = new ObjectDecoder();
        Assertions.assertEquals("Object", decoder.name());
    }

    @Test
    void priority() {
        ObjectDecoder decoder = new ObjectDecoder();
        Assertions.assertEquals(Priority.VERY_LOW, decoder.priority());
    }

    @Test
    void canDecode() {
        ObjectDecoder decoder = new ObjectDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfo.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfoExtended.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Boolean.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(boolean.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Void.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Map<String, Long>>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<DBInfoGeneric<String>>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String[].class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(ValidationLevel.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBPoolInterface.class)));

    }

    @Test
    void decode() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfo.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfo results = (DBInfo) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeInherited() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));
        configs.put("user", new LeafNode("Ted"));
        configs.put("timeout", new LeafNode("10"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoExtended.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoExtended results = (DBInfoExtended) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
        Assertions.assertEquals("Ted", results.getUser());
        Assertions.assertEquals(10, results.getTimeout());
    }

    @Test
    void decodeNoDefaultInherited() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));
        configs.put("user", new LeafNode("Ted"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoExtended.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.timeout, for class: DBInfoExtended, " +
            "during object decoding", result.getErrors().get(0).description());

        DBInfoExtended results = (DBInfoExtended) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
        Assertions.assertEquals("Ted", results.getUser());
        Assertions.assertEquals(0, results.getTimeout());
    }

    @Test
    void decodeDefaultInherited() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));
        configs.put("user", new LeafNode("Ted"));
        configs.put("timeout", new LeafNode("10"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoExtendedDefault.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoExtendedDefault results = (DBInfoExtendedDefault) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
        Assertions.assertEquals("Ted", results.getUser());
        Assertions.assertEquals(10, results.getTimeout());
    }

    @Test
    void decodeDefaultInheritedMissingValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));
        configs.put("user", new LeafNode("Ted"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoExtendedDefault.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        DBInfoExtendedDefault results = (DBInfoExtendedDefault) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
        Assertions.assertEquals("Ted", results.getUser());
        Assertions.assertEquals(10000, results.getTimeout());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.timeout, from node: " +
            "MapNode{mapNode={password=LeafNode{value='pass'}, port=LeafNode{value='100'}, uri=LeafNode{value='mysql.com'}, " +
            "user=LeafNode{value='Ted'}}}, with class: DBInfoExtendedDefault",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeNoDefaultConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoNoDefaultConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("No default Constructor for : org.github.gestalt.config.test.classes.DBInfoNoDefaultConstructor on " +
            "Path: db.host", result.getErrors().get(0).description());
    }

    @Test
    void decodePrivateConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoPrivateConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Constructor for: org.github.gestalt.config.test.classes.DBInfoPrivateConstructor is not public on " +
            "Path: db.host", result.getErrors().get(0).description());
    }

    @Test
    void decodeDefaultValues() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.password, from node: " +
            "MapNode{mapNode={port=LeafNode{value='100'}, uri=LeafNode{value='mysql.com'}}}, with class: DBInfoNoConstructor",
            result.getErrors().get(0).description());

        DBInfoNoConstructor results = (DBInfoNoConstructor) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("password", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeSetterTakePriorityTestedByChangeValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoSetterChangeValue.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoSetterChangeValue results = (DBInfoSetterChangeValue) result.results();
        Assertions.assertEquals(200, results.getPort());
        Assertions.assertEquals("****", results.getPassword());
        Assertions.assertEquals("mysql.comabc", results.getUri());
    }

    @Test
    void decodeDefaultGetterModifyValueNotNull() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        //configs.put("port", new LeafNode("100")); missing
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoIntegerPortNonNullGetter.class), new DecoderContext(decoderService, null));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.port, from node: " +
            "MapNode{mapNode={password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}}, " +
            "with class: DBInfoIntegerPortNonNullGetter",
            result.getErrors().get(0).description());


        DBInfoIntegerPortNonNullGetter results = (DBInfoIntegerPortNonNullGetter) result.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeDefaultGetterBooleanModifyValueNotNull() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));
        configs.put("port", new LeafNode("100"));
        //configs.put("enabled", new LeafNode("true")); missing

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoBooleanEnabledNonNullGetter.class), new DecoderContext(decoderService, null));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.enabled, from node: " +
                "MapNode{mapNode={password=LeafNode{value='pass'}, port=LeafNode{value='100'}, uri=LeafNode{value='mysql.com'}}}, " +
                "with class: DBInfoBooleanEnabledNonNullGetter",
            result.getErrors().get(0).description());


        DBInfoBooleanEnabledNonNullGetter results = (DBInfoBooleanEnabledNonNullGetter) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
        Assertions.assertEquals(true, results.isEnabled());
    }

    @Test
    void decodeBadNodeNotAnInt() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("aaaa"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfo.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.port, from node: " +
            "LeafNode{value='aaaa'} attempting to decode Integer", result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals(
            "Unable to find node matching path: db.host.port, for class: DBInfo, during object decoding",
            result.getErrors().get(1).description());

        DBInfo results = (DBInfo) result.results();
        Assertions.assertEquals(0, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeDefaultBadNodeNotAnInt() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("aaaa"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.port, from node: " +
            "LeafNode{value='aaaa'} attempting to decode Integer", result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals(
            "Missing Optional Value while decoding Object on path: db.host.port, from node: " +
                "MapNode{mapNode={password=LeafNode{value='pass'}, port=LeafNode{value='aaaa'}, uri=LeafNode{value='mysql.com'}}}, " +
                "with class: DBInfoNoConstructor",
            result.getErrors().get(1).description());

        DBInfoNoConstructor results = (DBInfoNoConstructor) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadNodeNotAnIntNullResult() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("aaaa"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoIntegerPort.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.port, from node: " +
            "LeafNode{value='aaaa'} attempting to decode Integer", result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: DBInfoIntegerPort, " +
                "during object decoding", result.getErrors().get(1).description());

        DBInfoIntegerPort results = (DBInfoIntegerPort) result.results();
        Assertions.assertNull(results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeIgnoreStaticMethod() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoStatic.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoStatic results = (DBInfoStatic) result.results();
        Assertions.assertEquals(0, DBInfoStatic.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeOptional() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoOptional.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoOptional results = (DBInfoOptional) result.results();
        Assertions.assertEquals(100, results.getPort().get());
        Assertions.assertEquals("pass", results.getPassword().get());
        Assertions.assertEquals("mysql.com", results.getUri().get());
    }

    @Test
    void decodeDefaultOptionalMissingValues() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoOptional.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        DBInfoOptional results = (DBInfoOptional) result.results();
        Assertions.assertTrue(results.getPort().isEmpty());
        Assertions.assertTrue(results.getPassword().isEmpty());
        Assertions.assertTrue(results.getUri().isEmpty());

        Assertions.assertEquals(3, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.port, from node: " +
                "MapNode{mapNode={}}, with class: DBInfoOptional",
            result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.uri, from node: " +
                "MapNode{mapNode={}}, with class: DBInfoOptional",
            result.getErrors().get(1).description());

        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(2).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.password, from node: " +
            "MapNode{mapNode={}}, with class: DBInfoOptional", result.getErrors().get(2).description());
    }

    @Test
    void decodeDefaultOptionalPartialMissingValues() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoOptional.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        DBInfoOptional results = (DBInfoOptional) result.results();
        Assertions.assertEquals(100, results.getPort().get());
        Assertions.assertTrue(results.getPassword().isEmpty());
        Assertions.assertTrue(results.getUri().isEmpty());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.uri, from node: " +
                "MapNode{mapNode={port=LeafNode{value='100'}}}, with class: DBInfoOptional",
            result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.password, from node: " +
            "MapNode{mapNode={port=LeafNode{value='100'}}}, with class: DBInfoOptional",
            result.getErrors().get(1).description());
    }

    @Test
    void decodeNullLeafNodeValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode(null));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfo.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.host.port, has no value attempting to decode Integer",
            result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: DBInfo, during object decoding",
            result.getErrors().get(1).description());

        DBInfo results = (DBInfo) result.results();
        Assertions.assertEquals(0, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeNullLeafNode() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", null);
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfo.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: DBInfo, during object decoding",
            result.getErrors().get(0).description());

        DBInfo results = (DBInfo) result.results();
        Assertions.assertEquals(0, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeDefaultNullMapNode() {
        ObjectDecoder decoder = new ObjectDecoder();

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(null), TypeCapture.of(DBInfoNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(3, result.getErrors().size());

        assertThat(result.getErrors().stream()
            .allMatch(it -> it.level().equals(ValidationLevel.MISSING_OPTIONAL_VALUE))).isTrue();

        assertThat(result.getErrors()).anyMatch(it ->
            ("Missing Optional Value while decoding Object on path: db.host.port, from node: MapNode{mapNode={}}, " +
                "with class: DBInfoNoConstructor")
                .equals(it.description()));

        assertThat(result.getErrors()).anyMatch(it ->
            ("Missing Optional Value while decoding Object on path: db.host.uri, from node: MapNode{mapNode={}}, " +
                "with class: DBInfoNoConstructor")
                .equals(it.description()));

        assertThat(result.getErrors()).anyMatch(it ->
            ("Missing Optional Value while decoding Object on path: db.host.password, from node: MapNode{mapNode={}}, " +
                "with class: DBInfoNoConstructor")
                .equals(it.description()));

        DBInfoNoConstructor results = (DBInfoNoConstructor) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("password", results.getPassword());
        Assertions.assertEquals("test", results.getUri());
    }

    @Test
    void decodeNullMapNodeDBInfoStatic() {
        ObjectDecoder decoder = new ObjectDecoder();

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(null), TypeCapture.of(DBInfoStatic.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.uri, for class: DBInfoStatic, during object decoding",
            result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.password, for class: DBInfoStatic, during object decoding",
            result.getErrors().get(1).description());
    }

    @Test
    void decodeNullNode() {
        ObjectDecoder decoder = new ObjectDecoder();

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            null, TypeCapture.of(DBInfoNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : null",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeWrongNodeType() {
        ObjectDecoder decoder = new ObjectDecoder();

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new LeafNode("mysql.com"), TypeCapture.of(DBInfoNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : LEAF",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeHttpPool() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBPool.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.defaultWait, from node: " +
                "MapNode{mapNode={maxperroute=LeafNode{value='10'}, keepalivetimeoutms=LeafNode{value='123'}, " +
                "idletimeoutsec=LeafNode{value='1000'}, validateafterinactivity=LeafNode{value='60'}, maxtotal=LeafNode{value='100'}}}, " +
                "with class: DBPool",
            result.getErrors().get(0).description());

        DBPool results = (DBPool) result.results();
        Assertions.assertEquals(100, results.maxTotal);
        Assertions.assertEquals(10, results.maxPerRoute);
        Assertions.assertEquals(60, results.validateAfterInactivity);
        Assertions.assertEquals(123, results.keepAliveTimeoutMs);
        Assertions.assertEquals(1000, results.idleTimeoutSec);
        Assertions.assertEquals(33.0F, results.defaultWait);
    }

    @Test
    void decodeWithAnnotationPath() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoAnnotations results = (DBInfoAnnotations) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationLongPath() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new MapNode(Map.of("port", new LeafNode("100"))));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoAnnotationsLong.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoAnnotationsLong results = (DBInfoAnnotationsLong) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.channel, from node: " +
            "MapNode{mapNode={password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}}",
            result.getErrors().get(0).description());

        DBInfoAnnotations results = (DBInfoAnnotations) result.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationOnlyDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result =
            decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoAnnotationsDefault.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.port, from node: " +
            "MapNode{mapNode={password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}}",
            result.getErrors().get(0).description());

        DBInfoAnnotationsDefault results = (DBInfoAnnotationsDefault) result.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationWrongDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoBadAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.channel, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer", result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: DBInfoBadAnnotations, " +
            "during object decoding", result.getErrors().get(1).description());

        DBInfoBadAnnotations results = (DBInfoBadAnnotations) result.results();
        Assertions.assertEquals(0, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationWrongDefaultUseClassDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoBadAnnotationsWithClassDefault.class), new DecoderContext(decoderService, null));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.channel, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer", result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.channel, from node: " +
            "MapNode{mapNode={password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}}, " +
            "with class: DBInfoBadAnnotationsWithClassDefault",
            result.getErrors().get(1).description());

        DBInfoBadAnnotationsWithClassDefault results = (DBInfoBadAnnotationsWithClassDefault) result.results();
        Assertions.assertNotNull(results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithMethodAnnotation() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoMethodAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoMethodAnnotations results = (DBInfoMethodAnnotations) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithMethodAnnotationLongPath() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new MapNode(Map.of("port", new LeafNode("100"))));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoMethodAnnotationsLong.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoMethodAnnotationsLong results = (DBInfoMethodAnnotationsLong) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeDefaultWithMethodAnnotation() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoMethodAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.channel, from node: " +
            "MapNode{mapNode={password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}}",
            result.getErrors().get(0).description());

        DBInfoMethodAnnotations results = (DBInfoMethodAnnotations) result.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithMethodAnnotationOnlyDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoMethodAnnotationsDefault.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.port, from node: " +
            "MapNode{mapNode={password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}}",
            result.getErrors().get(0).description());

        DBInfoMethodAnnotationsDefault results = (DBInfoMethodAnnotationsDefault) result.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithMethodAnnotationWrongDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoBadMethodAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.channel, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer", result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: DBInfoBadMethodAnnotations, " +
            "during object decoding", result.getErrors().get(1).description());

        DBInfoBadMethodAnnotations results = (DBInfoBadMethodAnnotations) result.results();
        Assertions.assertEquals(0, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationPriorityConfig() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("socket", new LeafNode("200"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoBothAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoBothAnnotations results = (DBInfoBothAnnotations) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeDefaultWithAnnotationPriorityConfig() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        //configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(DBInfoBothAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.channel, from node: " +
                "MapNode{mapNode={password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}}",
            result.getErrors().get(0).description());

        DBInfoBothAnnotations results = (DBInfoBothAnnotations) result.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithDefaultWrapper() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(ObjectWithDefaultsWrapper.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(9, result.getErrors().size());
        assertThat(result.getErrors().stream()
            .allMatch(it -> it.level().equals(ValidationLevel.MISSING_OPTIONAL_VALUE))).isTrue();

        ObjectWithDefaultsWrapper results = (ObjectWithDefaultsWrapper) result.results();
        Assertions.assertEquals((byte) 1, results.myByte);
        Assertions.assertEquals((short) 2, results.myShort);
        Assertions.assertEquals(3, results.myInteger);
        Assertions.assertEquals(4L, results.myLong);
        Assertions.assertEquals(5.5f, results.myFloat);
        Assertions.assertEquals(6.6D, results.myDouble);
        Assertions.assertEquals('a', results.myChar);
        Assertions.assertEquals("a", results.myString);
        Assertions.assertEquals(true, results.myBoolean);
    }

    @Test
    void decodeWithDefaultPrimitive() {

        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(ObjectWithDefaultsPrimitive.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(8, result.getErrors().size());
        assertThat(result.getErrors().stream()
            .allMatch(it -> it.level().equals(ValidationLevel.MISSING_OPTIONAL_VALUE))).isTrue();

        ObjectWithDefaultsPrimitive results = (ObjectWithDefaultsPrimitive) result.results();
        Assertions.assertEquals((byte) 1, results.myByte);
        Assertions.assertEquals((short) 2, results.myShort);
        Assertions.assertEquals(3, results.myInteger);
        Assertions.assertEquals(4L, results.myLong);
        Assertions.assertEquals(5.5f, results.myFloat);
        Assertions.assertEquals(6.6D, results.myDouble);
        Assertions.assertEquals('a', results.myChar);
        Assertions.assertEquals(true, results.myBoolean);
    }

    @Test
    void decodeWithOutDefaultPrimitive() {

        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(ObjectWithWithoutDefaultsPrimitive.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(8, result.getErrors().size());
        assertThat(result.getErrors().stream()
            .allMatch(it -> it.level().equals(ValidationLevel.MISSING_VALUE))).isTrue();

        ObjectWithWithoutDefaultsPrimitive results = (ObjectWithWithoutDefaultsPrimitive) result.results();
        Assertions.assertEquals((byte) 0, results.myByte);
        Assertions.assertEquals((short) 0, results.myShort);
        Assertions.assertEquals(0, results.myInteger);
        Assertions.assertEquals(0L, results.myLong);
        Assertions.assertEquals(0f, results.myFloat);
        Assertions.assertEquals(0D, results.myDouble);
        Assertions.assertEquals(Character.MIN_VALUE, results.myChar);
        Assertions.assertEquals(false, results.myBoolean);
    }

    @Test
    void decodeWithOutDefaultWrapper() {

        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(ObjectWithoutDefaultsWrapper.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(9, result.getErrors().size());
        assertThat(result.getErrors().stream()
            .allMatch(it -> it.level().equals(ValidationLevel.MISSING_VALUE))).isTrue();

        ObjectWithoutDefaultsWrapper results = (ObjectWithoutDefaultsWrapper) result.results();
        Assertions.assertNull(results.myByte);
        Assertions.assertNull(results.myShort);
        Assertions.assertNull(results.myInteger);
        Assertions.assertNull(results.myLong);
        Assertions.assertNull(results.myFloat);
        Assertions.assertNull(results.myDouble);
        Assertions.assertNull(results.myChar);
        Assertions.assertNull(results.myString);
        Assertions.assertNull(results.myBoolean);
    }

    @Test
    void decodeWithZeroDefaultPrimitive() {

        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), TypeCapture.of(ObjectWithZeroDefaultsWrapper.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(9, result.getErrors().size());
        assertThat(result.getErrors().stream()
            .allMatch(it -> it.level().equals(ValidationLevel.MISSING_OPTIONAL_VALUE))).isTrue();

        ObjectWithZeroDefaultsWrapper results = (ObjectWithZeroDefaultsWrapper) result.results();
        Assertions.assertEquals((byte) 0, results.myByte);
        Assertions.assertEquals((short) 0, results.myShort);
        Assertions.assertEquals(0, results.myInteger);
        Assertions.assertEquals(0L, results.myLong);
        Assertions.assertEquals(0f, results.myFloat);
        Assertions.assertEquals(0D, results.myDouble);
        Assertions.assertEquals(Character.MIN_VALUE, results.myChar);
        Assertions.assertEquals("", results.myString);
        Assertions.assertEquals(false, results.myBoolean);
    }

}
