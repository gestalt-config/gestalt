package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

class SequencedCollectionDecoderTest {
    final DoubleDecoder doubleDecoder = new DoubleDecoder();
    final StringDecoder stringDecoder = new StringDecoder();
    final SequencedCollectionDecoder SequencedCollectionDecoder = new SequencedCollectionDecoder();

    final ListDecoder listDecoder = new ListDecoder();

    ConfigNodeService configNodeService;
    DecoderService decoderService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(List.of(doubleDecoder, stringDecoder, SequencedCollectionDecoder, listDecoder), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {

        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();
        Assertions.assertEquals("SequencedList", decoder.name());
    }

    @Test
    void priority() {
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();
        Assertions.assertEquals(Priority.HIGH, decoder.priority());
    }

    @Test
    void canDecode() {
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<String>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Long>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(List.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<String>>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<ArrayList<String>>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Set.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Set<String>>() {
        }));

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<SequencedCollection<Long>>() {
        }));

    }

    @Test
    void arrayDecodeStrings() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<SequencedCollection<String>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));

        SequencedCollection<?> sq = values.results();
        Assertions.assertEquals(3, sq.size());
        Assertions.assertEquals("John", sq.removeFirst());
        Assertions.assertEquals("Steve", sq.removeFirst());
        Assertions.assertEquals("Matt", sq.removeFirst());
    }

    @Test
    void arrayDecodeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[2] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(List.of(arrayNode));
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<SequencedCollection<Double>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());

        Assertions.assertEquals(0.1111, values.results().get(0));
        Assertions.assertEquals(0.222, values.results().get(1));
        Assertions.assertEquals(0.33, values.results().get(2));

        SequencedCollection<?> sq = values.results();
        Assertions.assertEquals(3, sq.size());
        Assertions.assertEquals(0.1111, sq.removeFirst());
        Assertions.assertEquals(0.222, sq.removeFirst());
        Assertions.assertEquals(0.33, sq.removeFirst());
    }

    @Test
    void arrayDecodeDoublesMissingIndex() {

        ConfigNode[] arrayNode = new ConfigNode[4];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[3] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<SequencedCollection<Double>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(4, values.results().size());
        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Missing array index: 2", values.getErrors().get(0).description());

        Assertions.assertEquals(0.1111, values.results().get(0));
        Assertions.assertEquals(0.222, values.results().get(1));
        Assertions.assertNull(values.results().get(2));
        Assertions.assertEquals(0.33, values.results().get(3));

        SequencedCollection<?> sq = values.results();
        Assertions.assertEquals(0.1111, sq.removeFirst());
        Assertions.assertEquals(0.222, sq.removeFirst());
        Assertions.assertNull(sq.removeFirst());
        Assertions.assertEquals(0.33, sq.removeFirst());
    }

    @Test
    void arrayDecodeLeaf() {
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("0.1111, 0.22"),
            new TypeCapture<SequencedCollection<Double>>() { }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.results().size());
        Assertions.assertEquals(0.1111, values.results().get(0));
        Assertions.assertEquals(0.22, values.results().get(1));

        SequencedCollection<?> sq = values.results();
        Assertions.assertEquals(2, sq.size());
        Assertions.assertEquals(0.1111, sq.removeFirst());
        Assertions.assertEquals(0.22, sq.removeFirst());
    }

    @Test
    void arrayDecodeLeafWithEscapeComma() {
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("a,b,c\\,d"),
            new TypeCapture<SequencedCollection<String>>() { }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("a", values.results().get(0));
        Assertions.assertEquals("b", values.results().get(1));
        Assertions.assertEquals("c,d", values.results().get(2));

        SequencedCollection<?> sq = values.results();
        Assertions.assertEquals(3, sq.size());
        Assertions.assertEquals("a", sq.removeFirst());
        Assertions.assertEquals("b", sq.removeFirst());
        Assertions.assertEquals("c,d", sq.removeFirst());
    }

    @Test
    void arrayDecodeNullLeaf() {
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode(null),
            new TypeCapture<SequencedCollection<Double>>() {  }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.hosts, has no value attempting to decode SequencedList",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeNullNode() {
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), null,
            new TypeCapture<SequencedCollection<Double>>() { }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: null, attempting to decode SequencedList",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeEmptyArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(List.of());
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes,
            new TypeCapture<SequencedCollection<String>>() { }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().size());
    }

    @Test
    void arrayDecodeNullArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(null);
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes,
            new TypeCapture<SequencedCollection<String>>() { }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().size());
    }

    @Test
    void arrayDecodeEmptyLeafNodeOk() {
        ConfigNode nodes = new LeafNode("");
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes,
            new TypeCapture<SequencedCollection<String>>() { }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(1, values.results().size());
        Assertions.assertEquals("", values.results().get(0));

        SequencedCollection<?> sq = values.results();
        Assertions.assertEquals(1, sq.size());
        Assertions.assertEquals("", sq.removeFirst());
    }


    @Test
    void arrayDecodeWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Matt");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes,
            new TypeCapture<SequencedCollection<Double>>() { }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().size());

        Assertions.assertEquals(3, values.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[0], from node: LeafNode{value='John'} " +
                "attempting to decode Double",
            values.getErrors().get(0).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[1], from node: LeafNode{value='Matt'} " +
                "attempting to decode Double",
            values.getErrors().get(1).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[2], from node: LeafNode{value='Tom'} " +
                "attempting to decode Double",
            values.getErrors().get(2).description());
    }

    @Test
    void arrayDecodeMixedWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("0.22");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes,
            new TypeCapture<SequencedCollection<Double>>() { }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[0], from node: LeafNode{value='John'} " +
                "attempting to decode Double",
            values.getErrors().get(0).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[2], from node: " +
                "LeafNode{value='Tom'} attempting to decode Double",
            values.getErrors().get(1).description());

        Assertions.assertEquals(0.22, values.results().get(0));
    }

    @Test
    void arrayDecodeMapNode() {
        SequencedCollectionDecoder decoder = new SequencedCollectionDecoder();

        ValidateOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), new MapNode(new HashMap<>()),
            new TypeCapture<SequencedCollection<Double>>() { }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: MAP, attempting to decode SequencedList",
            values.getErrors().get(0).description());
    }
}
