package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

class SequencedMapDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new ListDecoder(), new SequencedMapDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        SequencedMapDecoder decoder = new SequencedMapDecoder();
        Assertions.assertEquals("SequencedMap", decoder.name());
    }

    @Test
    void priority() {
        SequencedMapDecoder decoder = new SequencedMapDecoder();
        Assertions.assertEquals(Priority.HIGH, decoder.priority());
    }

    @Test
    void canDecode() {
        SequencedMapDecoder decoder = new SequencedMapDecoder();

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Map<String, Long>>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<HashMap<String, Long>>() {
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

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<SequencedMap<String, Long>>() {
        }));

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<LinkedHashMap<String, Long>>() {
        }));

    }

    @Test
    void decode() {

        Map<String, ConfigNode> configs = new LinkedHashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("password", new LeafNode("6000"));

        SequencedMapDecoder decoder = new SequencedMapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<Map<String, Integer>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Map<String, Integer> results = (Map<String, Integer>) validate.results();
        Assertions.assertEquals(100, results.get("port"));
        Assertions.assertEquals(300, results.get("uri"));
        Assertions.assertEquals(6000, results.get("password"));

        SequencedSet<Map.Entry<String, Integer>> results2 = ((SequencedMap<String, Integer>) validate.results()).sequencedEntrySet();
        Assertions.assertEquals(3, results2.size());
        Assertions.assertEquals(Map.entry("port", 100), results2.removeFirst());
        Assertions.assertEquals(Map.entry("uri", 300), results2.removeFirst());
        Assertions.assertEquals(Map.entry("password", 6000), results2.removeFirst());
    }


    @Test
    void decodeInt() {

        Map<String, ConfigNode> configs = new LinkedHashMap<>();
        configs.put("1", new LeafNode("100"));
        configs.put("2", new LeafNode("300"));
        configs.put("3", new LeafNode("6000"));

        SequencedMapDecoder decoder = new SequencedMapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                new TypeCapture<Map<Integer, Integer>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Map<Integer, Integer> results = (Map<Integer, Integer>) validate.results();
        Assertions.assertEquals(100, results.get(1));
        Assertions.assertEquals(300, results.get(2));
        Assertions.assertEquals(6000, results.get(3));

        SequencedSet<Map.Entry<String, Integer>> results2 = ((SequencedMap<String, Integer>) validate.results()).sequencedEntrySet();
        Assertions.assertEquals(3, results2.size());
        Assertions.assertEquals(Map.entry(1, 100), results2.removeFirst());
        Assertions.assertEquals(Map.entry(2, 300), results2.removeFirst());
        Assertions.assertEquals(Map.entry(3, 6000), results2.removeFirst());
    }


    @Test
    void decodeClass() {
        Map<String, ConfigNode> user1 = new LinkedHashMap<>();
        user1.put("name", new LeafNode("steve"));
        user1.put("age", new LeafNode("52"));
        Map<String, ConfigNode> user2 = new LinkedHashMap<>();
        user2.put("name", new LeafNode("john"));
        user2.put("age", new LeafNode("23"));

        Map<String, ConfigNode> configs = new LinkedHashMap<>();
        configs.put("user1", new MapNode(user1));
        configs.put("user2", new MapNode(user2));

        SequencedMapDecoder decoder = new SequencedMapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                new TypeCapture<Map<String, User>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Map<String, User> results = (Map<String, User>) validate.results();
        Assertions.assertEquals("steve", results.get("user1").name);
        Assertions.assertEquals(52, results.get("user1").age);
        Assertions.assertEquals("john", results.get("user2").name);
        Assertions.assertEquals(23, results.get("user2").age);

        SequencedSet<Map.Entry<String, User>> results2 = ((SequencedMap<String, User>) validate.results()).sequencedEntrySet();
        Assertions.assertEquals(2, results2.size());
        Assertions.assertEquals(Map.entry("user1", new User("steve", 52)), results2.removeFirst());
        Assertions.assertEquals(Map.entry("user2", new User("john", 23)), results2.removeFirst());
    }

    @Test
    void decodeNestedMap() {
        Map<String, ConfigNode> retrySetting = new LinkedHashMap<>();
        retrySetting.put("times", new LeafNode("2"));
        retrySetting.put("delay", new LeafNode("7"));

        Map<String, ConfigNode> settings = new LinkedHashMap<>();
        settings.put("timeout", new LeafNode("123"));
        settings.put("retry", new MapNode(retrySetting));

        Map<String, ConfigNode> configs = new LinkedHashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("settings", new MapNode(settings));

        SequencedMapDecoder decoder = new SequencedMapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                new TypeCapture<Map<String, Integer>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Map<String, Integer> results = (Map<String, Integer>) validate.results();
        Assertions.assertEquals(100, results.get("port"));
        Assertions.assertEquals(300, results.get("uri"));
        Assertions.assertEquals(123, results.get("settings.timeout"));
        Assertions.assertEquals(2, results.get("settings.retry.times"));
        Assertions.assertEquals(7, results.get("settings.retry.delay"));

        SequencedSet<Map.Entry<String, Integer>> results2 = ((SequencedMap<String, Integer>) validate.results()).sequencedEntrySet();
        Assertions.assertEquals(5, results2.size());
        Assertions.assertEquals(Map.entry("port", 100 ), results2.removeFirst());
        Assertions.assertEquals(Map.entry("uri", 300 ), results2.removeFirst());
        Assertions.assertEquals(Map.entry("settings.timeout", 123), results2.removeFirst());
        Assertions.assertEquals(Map.entry("settings.retry.times", 2), results2.removeFirst());
        Assertions.assertEquals(Map.entry("settings.retry.delay", 7), results2.removeFirst());
    }

    @Test
    void decodeNestedMapWithArray() {
        List<ConfigNode> retrySetting = new ArrayList<>();
        retrySetting.add(new LeafNode("2"));
        retrySetting.add(new LeafNode("7"));

        Map<String, ConfigNode> settings = new LinkedHashMap<>();
        settings.put("timeout", new LeafNode("123"));
        settings.put("retryList", new ArrayNode(retrySetting));

        Map<String, ConfigNode> configs = new LinkedHashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("settings", new MapNode(settings));

        SequencedMapDecoder decoder = new SequencedMapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                new TypeCapture<Map<String, Integer>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Map<String, Integer> results = (Map<String, Integer>) validate.results();
        Assertions.assertEquals(100, results.get("port"));
        Assertions.assertEquals(300, results.get("uri"));
        Assertions.assertEquals(123, results.get("settings.timeout"));
        Assertions.assertEquals(2, results.get("settings.retryList[0]"));
        Assertions.assertEquals(7, results.get("settings.retryList[1]"));

        SequencedSet<Map.Entry<String, Integer>> results2 = ((SequencedMap<String, Integer>) validate.results()).sequencedEntrySet();
        Assertions.assertEquals(5, results2.size());
        Assertions.assertEquals(Map.entry("port", 100), results2.removeFirst());
        Assertions.assertEquals(Map.entry("uri", 300), results2.removeFirst());
        Assertions.assertEquals(Map.entry( "settings.timeout", 123), results2.removeFirst());
        Assertions.assertEquals(Map.entry("settings.retryList[0]", 2), results2.removeFirst());
        Assertions.assertEquals(Map.entry("settings.retryList[1]", 7), results2.removeFirst());
    }

    @Test
    void decodeNullLeafNodeValue() {
        SequencedMapDecoder decoder = new SequencedMapDecoder();

        Map<String, ConfigNode> configs = new LinkedHashMap<>();
        configs.put("port", new LeafNode(null));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), new TypeCapture<Map<String, String>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.host.port, has no value attempting to decode String",
            validate.getErrors().get(0).description());
        Assertions.assertEquals("Map key was null on path: db.host.port",
            validate.getErrors().get(1).description());

        Map<String, String> results = (Map<String, String>) validate.results();
        Assertions.assertNull(results.get("port"));
        Assertions.assertEquals("mysql.com", results.get("uri"));
        Assertions.assertEquals("pass", results.get("password"));

        SequencedSet<Map.Entry<String, String>> results2 = ((SequencedMap<String, String>) validate.results()).sequencedEntrySet();
        Assertions.assertEquals(3, results2.size());
        var first = results2.removeFirst();
        Assertions.assertEquals("port", first.getKey());
        Assertions.assertEquals(null, first.getValue());
        Assertions.assertEquals(Map.entry("uri", "mysql.com"), results2.removeFirst());
        Assertions.assertEquals(Map.entry("password", "pass"), results2.removeFirst());
    }

    @Test
    void decodeNullKeyNodeValue() {
        SequencedMapDecoder decoder = new SequencedMapDecoder();

        Map<String, ConfigNode> configs = new LinkedHashMap<>();
        configs.put(null, new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), new TypeCapture<Map<String, String>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Map key was null on path: db.host",
            validate.getErrors().get(0).description());

        Map<String, String> results = (Map<String, String>) validate.results();
        Assertions.assertNull(results.get("port"));
        Assertions.assertEquals("mysql.com", results.get("uri"));
        Assertions.assertEquals("pass", results.get("password"));

        SequencedSet<Map.Entry<String, Integer>> results2 = ((SequencedMap<String, Integer>) validate.results()).sequencedEntrySet();
        Assertions.assertEquals(2, results2.size());
        Assertions.assertEquals(Map.entry("uri", "mysql.com"), results2.removeFirst());
        Assertions.assertEquals(Map.entry("password", "pass"), results2.removeFirst());
    }

    @Test
    void decodeNullLeafNode() {
        SequencedMapDecoder decoder = new SequencedMapDecoder();

        Map<String, ConfigNode> configs = new LinkedHashMap<>();
        configs.put("port", null);
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), new TypeCapture<Map<String, String>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Expected a leaf on path: db.host.port, received node type: null, attempting to decode String",
            validate.getErrors().get(0).description());
        Assertions.assertEquals("Map key was null on path: db.host.port",
            validate.getErrors().get(1).description());

        Map<String, String> results = (Map<String, String>) validate.results();
        Assertions.assertNull(results.get("port"));
        Assertions.assertEquals("mysql.com", results.get("uri"));
        Assertions.assertEquals("pass", results.get("password"));

        SequencedSet<Map.Entry<String, Integer>> results2 = ((SequencedMap<String, Integer>) validate.results()).sequencedEntrySet();
        Assertions.assertEquals(3, results2.size());
        var first = results2.removeFirst();
        Assertions.assertEquals("port", first.getKey());
        Assertions.assertEquals(null, first.getValue());
        Assertions.assertEquals(Map.entry("uri", "mysql.com"), results2.removeFirst());
        Assertions.assertEquals(Map.entry("password", "pass"), results2.removeFirst());
    }

    @Test
    void decodeWrongNodeType() {
        SequencedMapDecoder decoder = new SequencedMapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(),
                new LeafNode("mysql.com"), new TypeCapture<Map<String, String>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : LEAF",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeWrongType() {

        Map<String, ConfigNode> configs = new LinkedHashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("300"));
        configs.put("password", new LeafNode("6000"));

        SequencedMapDecoder decoder = new SequencedMapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                new TypeCapture<List<String>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a map on path: db.host, received node type : map, " +
                "received invalid types: [TypeCapture{rawType=class java.lang.String, type=class java.lang.String}]",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeMapNodeNullInside() {
        SequencedMapDecoder decoder = new SequencedMapDecoder();

        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(), new MapNode(null),
                new TypeCapture<List<String>>() { }, new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a map on path: db.host, received node type : map, " +
                "received invalid types: [TypeCapture{rawType=class java.lang.String, type=class java.lang.String}]",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() {
        SequencedMapDecoder decoder = new SequencedMapDecoder();
        ValidateOf<Map<?, ?>> validate = decoder.decode("db.host", Tags.of(), null, new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : null",
            validate.getErrors().get(0).description());
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(name, user.name) && Objects.equals(age, user.age);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

}
