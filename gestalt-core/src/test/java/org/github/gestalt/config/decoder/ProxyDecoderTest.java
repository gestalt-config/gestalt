package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.CamelCasePathMapper;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
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
    DecoderRegistry registry;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        registry = new DecoderRegistry(Arrays.asList(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new BooleanDecoder(), new ProxyDecoder()), configNodeService, lexer,
            Arrays.asList(new StandardPathMapper(), new CamelCasePathMapper()));
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
    void matches() {
        ProxyDecoder decoder = new ProxyDecoder();

        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Set<Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Map<Long, String>>() {
        }));

        Assertions.assertTrue(decoder.matches(TypeCapture.of(DBInfoInterface.class)));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(DBPoolInterface.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<DBPoolInterface>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfo.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfoExtended.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
    }

    @Test
    void decode() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs), TypeCapture.of(DBInfoInterface.class), registry);
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInfoInterface.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInfoInterface.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInfoInterface.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.host.uri, missing value, LeafNode{value='null'} attempting to decode String",
            validate.getErrors().get(0).description());

        DBInfoInterface results = (DBInfoInterface) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());

        try {
            results.getUri();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals("Failed to get proxy config while calling method: getUri in path: db.host.",
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInfoInterface.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, during navigating to next node",
            validate.getErrors().get(0).description());

        DBInfoInterface results = (DBInfoInterface) validate.results();
        Assertions.assertEquals(10, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWrongNodeType() {
        ProxyDecoder decoder = new ProxyDecoder();

        ValidateOf<Object> validate = decoder.decode("db.host", new LeafNode("mysql.com"),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a map node on path: db.host, received a : LEAF",
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBPoolInterface.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
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
            Assertions.assertEquals("Failed to get proxy config while calling method: getDefaultWait in path: db.host.",
                e.getUndeclaredThrowable().getMessage());
        }
    }

    @Test
    void decodeAnnotations() {
        ProxyDecoder decoder = new ProxyDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(IDBInfoAnnotations.class), registry);
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(IDBInfoAnnotations.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
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
            decoder.decode("db.host", new MapNode(configs), TypeCapture.of(IDBInfoBadAnnotations.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: ObjectToken, " +
                "during navigating to next node", validate.getErrors().get(0).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.channel, from node: LeafNode{value='abc'} " +
                "attempting to decode Integer",  validate.getErrors().get(1).description());

        IDBInfoBadAnnotations results = (IDBInfoBadAnnotations) validate.results();
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());

        try {
            results.getPort();
            Assertions.fail("Should throw an exception");
        } catch (UndeclaredThrowableException e) {
            Assertions.assertEquals(GestaltException.class, e.getUndeclaredThrowable().getClass());
            Assertions.assertEquals("Failed to get proxy config while calling method: getPort in path: db.host.",
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(IDBInfoAnnotationsLong.class), registry);
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

        ValidateOf<Object> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(IDBInfoMethodAnnotations.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        IDBInfoMethodAnnotations results = (IDBInfoMethodAnnotations) validate.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }
}

