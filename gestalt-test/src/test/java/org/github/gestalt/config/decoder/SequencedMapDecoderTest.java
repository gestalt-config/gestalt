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
class SequencedMapDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new ListDecoder(), new MapDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }


    @Test
    void canDecode() {
        MapDecoder decoder = new MapDecoder();
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new MapNode(Map.of()), new TypeCapture<SequencedMap<String, Long>>() {
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

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            new TypeCapture<SequencedMap<String, Integer>>() { }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, Integer> results = (Map<String, Integer>) result.results();
        Assertions.assertEquals(100, results.get("port"));
        Assertions.assertEquals(300, results.get("uri"));
        Assertions.assertEquals(6000, results.get("password"));

        SequencedSet<Map.Entry<String, Integer>> results2 = ((SequencedMap<String, Integer>) result.results()).sequencedEntrySet();
        Assertions.assertInstanceOf(SequencedMap.class, result.results());
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

        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                new TypeCapture<SequencedMap<Integer, Integer>>() { }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<Integer, Integer> results = (Map<Integer, Integer>) result.results();
        Assertions.assertEquals(100, results.get(1));
        Assertions.assertEquals(300, results.get(2));
        Assertions.assertEquals(6000, results.get(3));

        SequencedSet<Map.Entry<String, Integer>> results2 = ((SequencedMap<String, Integer>) result.results()).sequencedEntrySet();
        Assertions.assertEquals(3, results2.size());
        Assertions.assertInstanceOf(SequencedMap.class, result.results());
        Assertions.assertEquals(Map.entry(1, 100), results2.removeFirst());
        Assertions.assertEquals(Map.entry(2, 300), results2.removeFirst());
        Assertions.assertEquals(Map.entry(3, 6000), results2.removeFirst());
    }

    @Test
    void decodeLeaf() {
        MapDecoder decoder = new MapDecoder();

        GResultOf<Map<?, ?>> result = decoder.decode("db.host", Tags.of(), new LeafNode("port=100,uri=300,password=6000"),
            new TypeCapture<SequencedMap<String, Integer>>() {
            }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, Integer> results = (Map<String, Integer>) result.results();
        Assertions.assertEquals(100, results.get("port"));
        Assertions.assertEquals(300, results.get("uri"));
        Assertions.assertEquals(6000, results.get("password"));
    }
}
