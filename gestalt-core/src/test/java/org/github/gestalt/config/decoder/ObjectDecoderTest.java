package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.test.classes.*;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

class ObjectDecoderTest {

    ConfigNodeService configNodeService;
    final SentenceLexer lexer = new PathLexer();
    DecoderRegistry registry;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        registry = new DecoderRegistry(Arrays.asList(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder()), configNodeService, lexer);
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
    void matches() {
        ObjectDecoder decoder = new ObjectDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(DBInfo.class)));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(DBInfoExtended.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Map<String, Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<DBInfoGeneric<String>>() {
        }));
    }

    @Test
    void decode() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs), TypeCapture.of(DBInfo.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfo results = (DBInfo) validate.results();
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs), TypeCapture.of(DBInfoExtended.class),
            registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        DBInfoExtended results = (DBInfoExtended) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
        Assertions.assertEquals("Ted", results.getUser());
        Assertions.assertEquals(10000, results.getTimeout());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.timeout, for class: ObjectToken, " +
                "during navigating to next node",
            validate.getErrors().get(0).description());

    }

    @Test
    void decodeNoDefaultConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInfoNoDefaultConstructor.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("No default Constructor for : org.github.gestalt.config.test.classes.DBInfoNoDefaultConstructor on " +
                "Path: db.host",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodePrivateConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInfoPrivateConstructor.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Constructor for: org.github.gestalt.config.test.classes.DBInfoPrivateConstructor is not public on " +
                "Path: db.host",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeDefaultValues() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.password, for class: ObjectToken, " +
                "during navigating to next node",
            validate.getErrors().get(0).description());

        DBInforNoConstructor results = (DBInforNoConstructor) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("password", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadNodeNotAnInt() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("aaaa"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.port, from node: " +
                "LeafNode{value='aaaa'} attempting to decode Integer",
            validate.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: int, during object decoding",
            validate.getErrors().get(1).description());

        DBInforNoConstructor results = (DBInforNoConstructor) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeNullLeafNodeValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode(null));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.host.port, missing value, LeafNode{value='null'} attempting to decode Integer",
            validate.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: int, during object decoding",
            validate.getErrors().get(1).description());

        DBInforNoConstructor results = (DBInforNoConstructor) validate.results();
        Assertions.assertEquals(100, results.getPort());
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, during navigating to next node",
            validate.getErrors().get(0).description());

        DBInforNoConstructor results = (DBInforNoConstructor) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWrongNodeType() {
        ObjectDecoder decoder = new ObjectDecoder();

        ValidateOf<Object> validate = decoder.decode("db.host", new LeafNode("mysql.com"),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a leaf on path: db.host, received node type, received: LeafNode{value='mysql.com'} " +
                "attempting to decode Object",
            validate.getErrors().get(0).description());
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBPool.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.defaultWait, for class: ObjectToken, " +
                "during navigating to next node",
            validate.getErrors().get(0).description());

        DBPool results = (DBPool) validate.results();
        Assertions.assertEquals(100, results.maxTotal);
        Assertions.assertEquals(10, results.maxPerRoute);
        Assertions.assertEquals(60, results.validateAfterInactivity);
        Assertions.assertEquals(123, results.keepAliveTimeoutMs);
        Assertions.assertEquals(1000, results.idleTimeoutSec);
        Assertions.assertEquals(33.0F, results.defaultWait);
    }
}
