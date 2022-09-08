package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

class MapDecoderTest {

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
        MapDecoder decoder = new MapDecoder();
        Assertions.assertEquals("Map", decoder.name());
    }

    @Test
    void priority() {
        MapDecoder decoder = new MapDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        MapDecoder decoder = new MapDecoder();

        Assertions.assertTrue(decoder.matches(new TypeCapture<Map<String, Long>>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfo.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Long>>() {
        }));
    }

    @Test
    void decode() {

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("password", new LeafNode("6000"));

        MapDecoder decoder = new MapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", new MapNode(configs), new TypeCapture<Map<String, Integer>>() {
            },
            registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Map<String, Integer> results = (Map<String, Integer>) validate.results();
        Assertions.assertEquals(100, results.get("port"));
        Assertions.assertEquals(300, results.get("uri"));
        Assertions.assertEquals(6000, results.get("password"));
    }

    @Test
    void decodeNullLeafNodeValue() {
        MapDecoder decoder = new MapDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode(null));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", new MapNode(configs),
            new TypeCapture<Map<String, String>>() {
            }, registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.host.port, missing value, LeafNode{value='null'} attempting to decode String",
            validate.getErrors().get(0).description());
        Assertions.assertEquals("Map key was null on path: db.host.port",
            validate.getErrors().get(1).description());

        Map<String, String> results = (Map<String, String>) validate.results();
        Assertions.assertNull(results.get("port"));
        Assertions.assertEquals("mysql.com", results.get("uri"));
        Assertions.assertEquals("pass", results.get("password"));
    }

    @Test
    void decodeNullKeyNodeValue() {
        MapDecoder decoder = new MapDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put(null, new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", new MapNode(configs),
            new TypeCapture<Map<String, String>>() {
            }, registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Map key was null on path: db.host",
            validate.getErrors().get(0).description());

        Map<String, String> results = (Map<String, String>) validate.results();
        Assertions.assertNull(results.get("port"));
        Assertions.assertEquals("mysql.com", results.get("uri"));
        Assertions.assertEquals("pass", results.get("password"));
    }

    @Test
    void decodeNullLeafNode() {
        MapDecoder decoder = new MapDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", null);
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", new MapNode(configs),
            new TypeCapture<Map<String, String>>() {
            }, registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Null Nodes on path: db.host.port",
            validate.getErrors().get(0).description());
        Assertions.assertEquals("Map key was null on path: db.host.port",
            validate.getErrors().get(1).description());

        Map<String, String> results = (Map<String, String>) validate.results();
        Assertions.assertNull(results.get("port"));
        Assertions.assertEquals("mysql.com", results.get("uri"));
        Assertions.assertEquals("pass", results.get("password"));
    }

    @Test
    void decodeWrongNodeType() {
        MapDecoder decoder = new MapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", new LeafNode("mysql.com"),
            new TypeCapture<Map<String, String>>() {
            }, registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a map node on path: db.host, received a : LEAF",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeWrongType() {

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("password", new LeafNode("6000"));

        MapDecoder decoder = new MapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", new MapNode(configs), new TypeCapture<List<String>>() {
            },
            registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a map on path: db.host, received a : MAP, " +
                "received invalid types: [TypeCapture{rawType=class java.lang.String, type=class java.lang.String}]",
            validate.getErrors().get(0).description());
    }

}
