package org.config.gestalt.decoder;

import org.config.gestalt.exceptions.ConfigurationException;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.node.*;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class ListDecoderTest {
    DoubleDecoder doubleDecoder = new DoubleDecoder();
    StringDecoder stringDecoder = new StringDecoder();
    ListDecoder listDecoder = new ListDecoder();

    ConfigNodeService configNodeService;
    DecoderService decoderService;
    SentenceLexer lexer;

    ListDecoderTest() throws GestaltException {
    }

    @BeforeEach
    void setup() throws ConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Arrays.asList(doubleDecoder, stringDecoder, listDecoder), configNodeService, lexer);
    }

    @Test
    void name() {
        ListDecoder decoder = new ListDecoder();
        Assertions.assertEquals("List", decoder.name());
    }

    @Test
    void matches() {
        ListDecoder decoder = new ListDecoder();
        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<String>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(List.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<List<String>>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(Set.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Set<String>>() {
        }));
    }

    @Test
    void arrayDecodeStrings() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        ValidateOf<List<String>> values = decoder.decode("db.hosts", nodes, new TypeCapture<List<String>>() {
        }, decoderService);

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));
    }

    @Test
    void arrayDecodeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[2] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        ValidateOf<List<Double>> values = decoder.decode("db.hosts", nodes, new TypeCapture<List<Double>>() {
        }, decoderService);

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());

        Assertions.assertEquals(0.1111, values.results().get(0));
        Assertions.assertEquals(0.222, values.results().get(1));
        Assertions.assertEquals(0.33, values.results().get(2));
    }

    @Test
    void arrayDecodeDoublesMissingIndex() {

        ConfigNode[] arrayNode = new ConfigNode[4];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[3] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        ValidateOf<List<Double>> values = decoder.decode("db.hosts", nodes, new TypeCapture<List<Double>>() {
        }, decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(4, values.results().size());
        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Missing array index: 2", values.getErrors().get(0).description());

        Assertions.assertEquals(0.1111, values.results().get(0));
        Assertions.assertEquals(0.222, values.results().get(1));
        Assertions.assertNull(values.results().get(2));
        Assertions.assertEquals(0.33, values.results().get(3));
    }

    @Test
    void arrayDecodeLeaf() {
        ListDecoder decoder = new ListDecoder();

        ValidateOf<List<Double>> values = decoder.decode("db.hosts", new LeafNode("0.1111, 0.22"), new TypeCapture<List<Double>>() {
        }, decoderService);

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.results().size());
        Assertions.assertEquals(0.1111, values.results().get(0));
        Assertions.assertEquals(0.22, values.results().get(1));
    }

    @Test
    void arrayDecodeNullLeaf() {
        ListDecoder decoder = new ListDecoder();

        ValidateOf<List<Double>> values = decoder.decode("db.hosts", new LeafNode(null), new TypeCapture<List<Double>>() {
        }, decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.hosts, missing value, LeafNode{value='null'} attempting to decode List",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Matt");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        ValidateOf<List<Double>> values = decoder.decode("db.hosts", nodes, new TypeCapture<List<Double>>() {
        }, decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

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
        ListDecoder decoder = new ListDecoder();

        ValidateOf<List<Double>> values = decoder.decode("db.hosts", nodes, new TypeCapture<List<Double>>() {
        }, decoderService);

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
        ListDecoder decoder = new ListDecoder();

        ValidateOf<List<Double>> values = decoder.decode("db.hosts", new MapNode(new HashMap<>()), new TypeCapture<List<Double>>() {
        }, decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array  on path: db.hosts, received node type, received: MapNode{mapNode={}} " +
                "attempting to decode List",
            values.getErrors().get(0).description());
    }
}
