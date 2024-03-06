package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

@SuppressWarnings("unchecked")
class MapDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
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
    void canDecode() {
        MapDecoder decoder = new MapDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Map<String, Long>>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Map>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfo.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Long>>() {
        }));
    }

    @Test
    void decode() {

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("password", new LeafNode("6000"));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<Map<String, Integer>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, Integer> results = (Map<String, Integer>) result.results();
        Assertions.assertEquals(100, results.get("port"));
        Assertions.assertEquals(300, results.get("uri"));
        Assertions.assertEquals(6000, results.get("password"));
    }


    @Test
    void decodeInt() {

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("1", new LeafNode("100"));
        configs.put("2", new LeafNode("300"));
        configs.put("3", new LeafNode("6000"));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<Map<Integer, Integer>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<Integer, Integer> results = (Map<Integer, Integer>) result.results();
        Assertions.assertEquals(100, results.get(1));
        Assertions.assertEquals(300, results.get(2));
        Assertions.assertEquals(6000, results.get(3));
    }

    @Test
    void decodeClass() {
        Map<String, ConfigNode> user1 = new HashMap<>();
        user1.put("name", new LeafNode("steve"));
        user1.put("age", new LeafNode("52"));
        Map<String, ConfigNode> user2 = new HashMap<>();
        user2.put("name", new LeafNode("john"));
        user2.put("age", new LeafNode("23"));

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("user1", new MapNode(user1));
        configs.put("user2", new MapNode(user2));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<Map<String, User>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, User> results = (Map<String, User>) result.results();
        Assertions.assertEquals("steve", results.get("user1").name);
        Assertions.assertEquals(52, results.get("user1").age);
        Assertions.assertEquals("john", results.get("user2").name);
        Assertions.assertEquals(23, results.get("user2").age);
    }

    @Test
    void decodeNestedMap() {
        Map<String, ConfigNode> retrySetting = new HashMap<>();
        retrySetting.put("times", new LeafNode("2"));
        retrySetting.put("delay", new LeafNode("7"));

        Map<String, ConfigNode> settings = new HashMap<>();
        settings.put("timeout", new LeafNode("123"));
        settings.put("retry", new MapNode(retrySetting));

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("settings", new MapNode(settings));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<Map<String, Integer>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, Integer> results = (Map<String, Integer>) result.results();
        Assertions.assertEquals(100, results.get("port"));
        Assertions.assertEquals(300, results.get("uri"));
        Assertions.assertEquals(123, results.get("settings.timeout"));
        Assertions.assertEquals(2, results.get("settings.retry.times"));
        Assertions.assertEquals(7, results.get("settings.retry.delay"));
    }

    @Test
    void decodeNestedMapWithArray() {
        List<ConfigNode> retrySetting = new ArrayList<>();
        retrySetting.add(new LeafNode("2"));
        retrySetting.add(new LeafNode("7"));

        Map<String, ConfigNode> settings = new HashMap<>();
        settings.put("timeout", new LeafNode("123"));
        settings.put("retryList", new ArrayNode(retrySetting));

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("settings", new MapNode(settings));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<Map<String, Integer>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, Integer> results = (Map<String, Integer>) result.results();
        Assertions.assertEquals(100, results.get("port"));
        Assertions.assertEquals(300, results.get("uri"));
        Assertions.assertEquals(123, results.get("settings.timeout"));
        Assertions.assertEquals(2, results.get("settings.retryList[0]"));
        Assertions.assertEquals(7, results.get("settings.retryList[1]"));
    }

    @Test
    void decodeNullLeafNodeValue() {
        MapDecoder decoder = new MapDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode(null));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), new TypeCapture<Map<String, String>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.host.port, has no value attempting to decode String",
            result.getErrors().get(0).description());
        Assertions.assertEquals("Map key was null on path: db.host.port",
            result.getErrors().get(1).description());

        Map<String, String> results = (Map<String, String>) result.results();
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

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), new TypeCapture<Map<String, String>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("Map key was null on path: db.host",
            result.getErrors().get(0).description());

        Map<String, String> results = (Map<String, String>) result.results();
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

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(),
            new MapNode(configs), new TypeCapture<Map<String, String>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals("Expected a leaf on path: db.host.port, received node type: null, attempting to decode String",
            result.getErrors().get(0).description());
        Assertions.assertEquals("Map key was null on path: db.host.port",
            result.getErrors().get(1).description());

        Map<String, String> results = (Map<String, String>) result.results();
        Assertions.assertNull(results.get("port"));
        Assertions.assertEquals("mysql.com", results.get("uri"));
        Assertions.assertEquals("pass", results.get("password"));
    }

    @Test
    void decodeWrongNodeType() {
        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(),
            new LeafNode("mysql.com"), new TypeCapture<Map<String, String>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : LEAF",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeWrongType() {

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("password", new LeafNode("6000"));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<List<String>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("Expected a map on path: db.host, received node type : map, " +
                "received invalid types: [TypeCapture{rawType=class java.lang.String, type=class java.lang.String}]",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeWrongTypeInt() {

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("password", new LeafNode("6000"));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<Integer>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : MAP",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeMapNodeNullInside() {
        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(null),
            new TypeCapture<List<String>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("Expected a map on path: db.host, received node type : map, " +
                "received invalid types: [TypeCapture{rawType=class java.lang.String, type=class java.lang.String}]",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() {
        MapDecoder decoder = new MapDecoder();
        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), null, new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : null",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeWrongKeyType() {

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("a", new LeafNode("100"));
        configs.put("2", new LeafNode("300"));
        configs.put("3", new LeafNode("6000"));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<Map<Integer, Integer>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.a, from node: LeafNode{value='a'} " +
                "attempting to decode Integer",
            result.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(1).level());
        Assertions.assertEquals("Map key was null on path: db.host.a",
            result.getErrors().get(1).description());

        Map<Integer, Integer> results = (Map<Integer, Integer>) result.results();
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals(null, results.get(1));
        Assertions.assertEquals(300, results.get(2));
        Assertions.assertEquals(6000, results.get(3));
    }

    @Test
    void decodeWrongValueType() {

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("1", new LeafNode("a"));
        configs.put("2", new LeafNode("300"));
        configs.put("3", new LeafNode("6000"));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<Map<Integer, Integer>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.1, from node: LeafNode{value='a'} " +
                "attempting to decode Integer",
            result.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(1).level());
        Assertions.assertEquals("Map key was null on path: db.host.1",
            result.getErrors().get(1).description());

        Map<Integer, Integer> results = (Map<Integer, Integer>) result.results();
        Assertions.assertEquals(3, results.size());
        Assertions.assertEquals(null, results.get(1));
        Assertions.assertEquals(300, results.get(2));
        Assertions.assertEquals(6000, results.get(3));
    }

    @Test
    void decodePathNull() {

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("1", new LeafNode("100"));
        configs.put("2", new LeafNode("300"));
        configs.put("3", new LeafNode("6000"));

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode(null, Tags.of(), new MapNode(configs),
            new TypeCapture<Map<Integer, Integer>>() {
            }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<Integer, Integer> results = (Map<Integer, Integer>) result.results();
        Assertions.assertEquals(3, results.size());
        Assertions.assertEquals(100, results.get(1));
        Assertions.assertEquals(300, results.get(2));
        Assertions.assertEquals(6000, results.get(3));
    }

    static class User {

        private String name;
        private Integer age;

        public User() {

        }

        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

}
