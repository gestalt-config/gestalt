package org.config.gestalt.decoder;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.node.ArrayNode;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.node.MapNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.test.classes.DBInfo;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class ArrayDecoderTest {
    DoubleDecoder doubleDecoder = new DoubleDecoder();
    StringDecoder stringDecoder = new StringDecoder();
    ListDecoder listDecoder = new ListDecoder();
    DecoderRegistry decoderService = new DecoderRegistry(Arrays.asList(doubleDecoder, stringDecoder, listDecoder));

    ArrayDecoderTest() throws GestaltException {
    }

    @Test
    void name() {
        ArrayDecoder decoder = new ArrayDecoder();
        Assertions.assertEquals("Array", decoder.name());
    }

    @Test
    void matches() {
        ArrayDecoder decoder = new ArrayDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(int[].class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<int[]>() {
        }));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(Integer[].class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<Integer[]>() {
        }));

        Assertions.assertTrue(decoder.matches(TypeCapture.of(DBInfo[].class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<DBInfo[]>() {
        }));


        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<String>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(List.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<String>>() {
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
        ArrayDecoder decoder = new ArrayDecoder();

        ValidateOf<String[]> values = decoder.decode("db.hosts", nodes, TypeCapture.of(String[].class), decoderService);

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().length);
        Assertions.assertEquals("John", values.results()[0]);
        Assertions.assertEquals("Steve", values.results()[1]);
        Assertions.assertEquals("Matt", values.results()[2]);
    }

    @Test
    void arrayDecodeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[2] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ArrayDecoder decoder = new ArrayDecoder();

        ValidateOf<Double[]> values = decoder.decode("db.hosts", nodes, TypeCapture.of(Double[].class), decoderService);

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().length);

        Assertions.assertEquals(0.1111, values.results()[0]);
        Assertions.assertEquals(0.222, values.results()[1]);
        Assertions.assertEquals(0.33, values.results()[2]);
    }

    @Test
    void arrayDecodeDoublesMissingIndex() {

        ConfigNode[] arrayNode = new ConfigNode[4];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[3] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ArrayDecoder decoder = new ArrayDecoder();

        ValidateOf<Double[]> values = decoder.decode("db.hosts", nodes, TypeCapture.of(Double[].class),
            decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(4, values.results().length);
        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Missing array index: 2", values.getErrors().get(0).description());

        Assertions.assertEquals(0.1111, values.results()[0]);
        Assertions.assertEquals(0.222, values.results()[1]);
        Assertions.assertNull(values.results()[2]);
        Assertions.assertEquals(0.33, values.results()[3]);
    }

    @Test
    void arrayDecodeLeaf() {
        ArrayDecoder decoder = new ArrayDecoder();

        ValidateOf<Double[]> values = decoder.decode("db.hosts", new LeafNode("0.1111, 0.22"), TypeCapture.of(Double[].class),
            decoderService);

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.results().length);
        Assertions.assertEquals(0.1111, values.results()[0]);
        Assertions.assertEquals(0.22, values.results()[1]);
    }

    @Test
    void arrayDecodeNullLeaf() {
        ArrayDecoder decoder = new ArrayDecoder();

        ValidateOf<Double[]> values = decoder.decode("db.hosts", new LeafNode(null), TypeCapture.of(Double[].class),
            decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.hosts, missing value, LeafNode{value='null'} attempting to decode Array",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Matt");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ArrayDecoder decoder = new ArrayDecoder();

        ValidateOf<Double[]> values = decoder.decode("db.hosts", nodes, TypeCapture.of(Double[].class),
            decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

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
        ArrayDecoder decoder = new ArrayDecoder();

        ValidateOf<Double[]> values = decoder.decode("db.hosts", nodes, TypeCapture.of(Double[].class),
            decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[0], from node: LeafNode{value='John'} " +
                "attempting to decode Double",
            values.getErrors().get(0).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[2], from node: " +
                "LeafNode{value='Tom'} attempting to decode Double",
            values.getErrors().get(1).description());

        Assertions.assertEquals(0.22, values.results()[1]);
    }

    @Test
    void arrayDecodeMapNode() {
        ArrayDecoder decoder = new ArrayDecoder();

        ValidateOf<Double[]> values = decoder.decode("db.hosts", new MapNode(new HashMap<>()), TypeCapture.of(Double[].class),
            decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array  on path: db.hosts, received node type, received: MapNode{mapNode={}} " +
                "attempting to decode Array",
            values.getErrors().get(0).description());
    }
}
