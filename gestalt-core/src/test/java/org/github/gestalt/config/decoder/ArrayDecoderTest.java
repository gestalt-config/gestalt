package org.github.gestalt.config.decoder;

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
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unchecked", "rawtypes"})
class ArrayDecoderTest {
    final DoubleDecoder doubleDecoder = new DoubleDecoder();
    final StringDecoder stringDecoder = new StringDecoder();
    final ListDecoder listDecoder = new ListDecoder();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(List.of(doubleDecoder, stringDecoder, listDecoder), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    void name() {
        ArrayDecoder decoder = new ArrayDecoder();
        Assertions.assertEquals("Array", decoder.name());
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    void priority() {
        ArrayDecoder decoder = new ArrayDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void canDecode() {
        ArrayDecoder decoder = new ArrayDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(int[].class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<int[]>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer[].class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Integer[]>() {
        }));

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfo[].class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<DBInfo[]>() {
        }));


        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<String>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Long>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(List.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<String>>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Set.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Set<String>>() {
        }));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void arrayDecodeStrings() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(List.of(arrayNode));
        ArrayDecoder decoder = new ArrayDecoder();

        GResultOf<Object[]> values = decoder.decode("db.hosts", Tags.of(), nodes,
            TypeCapture.of(String[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        String[] results = (String[]) values.results();
        Assertions.assertEquals(3, results.length);
        Assertions.assertEquals("John", results[0]);
        Assertions.assertEquals("Steve", results[1]);
        Assertions.assertEquals("Matt", results[2]);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void arrayDecodeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[2] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(List.of(arrayNode));
        ArrayDecoder decoder = new ArrayDecoder();

        GResultOf<Object[]> values = decoder.decode("db.hosts", Tags.of(), nodes,
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Double[] results = (Double[]) values.results();
        Assertions.assertEquals(3, results.length);

        Assertions.assertEquals(0.1111, results[0]);
        Assertions.assertEquals(0.222, results[1]);
        Assertions.assertEquals(0.33, results[2]);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void arrayDecodeDoublesMissingIndex() {

        ConfigNode[] arrayNode = new ConfigNode[4];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[3] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ArrayDecoder decoder = new ArrayDecoder();

        GResultOf<Object[]> values = decoder.decode("db.hosts", Tags.of(), nodes,
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Double[] results = (Double[]) values.results();
        Assertions.assertEquals(4, results.length);
        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Missing array index: 2", values.getErrors().get(0).description());

        Assertions.assertEquals(0.1111, results[0]);
        Assertions.assertEquals(0.222, results[1]);
        Assertions.assertNull(results[2]);
        Assertions.assertEquals(0.33, results[3]);
    }

    @Test
    void arrayDecodeLeaf() {
        ArrayDecoder<Double> decoder = new ArrayDecoder<>();

        GResultOf<Double[]> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("0.1111, 0.22"),
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Double[] results = values.results();

        Assertions.assertEquals(2, results.length);
        Assertions.assertEquals(0.1111, results[0]);
        Assertions.assertEquals(0.22, results[1]);
    }

    @Test
    void arrayDecodeNullLeaf() {
        ArrayDecoder<Double> decoder = new ArrayDecoder<>();

        GResultOf<Double[]> values = decoder.decode("db.hosts", Tags.of(), new LeafNode(null),
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.hosts, has no value attempting to decode Array",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeEmptyArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(List.of());
        ArrayDecoder decoder = new ArrayDecoder();

        GResultOf<Object[]> values = decoder.decode("db.hosts", Tags.of(), nodes,
            TypeCapture.of(String[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        String[] results = (String[]) values.results();
        Assertions.assertEquals(0, results.length);
    }


    @Test
    void arrayDecodeNullArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(null);
        ArrayDecoder decoder = new ArrayDecoder();

        GResultOf<Object[]> values = decoder.decode("db.hosts", Tags.of(), nodes, TypeCapture.of(String[].class),
            new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().length);
    }

    @Test
    void arrayDecodeEmptyLeafNodeOk() {
        ConfigNode nodes = new LeafNode("");
        ArrayDecoder decoder = new ArrayDecoder();

        GResultOf<Object[]> values = decoder.decode("db.hosts", Tags.of(), nodes, TypeCapture.of(String[].class),
            new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(1, values.results().length);
        Assertions.assertEquals("", values.results()[0]);
    }

    @Test
    void arrayDecodeWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Matt");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ArrayDecoder<Double> decoder = new ArrayDecoder<>();

        GResultOf<Double[]> values = decoder.decode("db.hosts", Tags.of(), nodes,
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

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
        ArrayDecoder<Double> decoder = new ArrayDecoder<>();

        GResultOf<Double[]> values = decoder.decode("db.hosts", Tags.of(), nodes,
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[0], from node: LeafNode{value='John'} " +
                "attempting to decode Double",
            values.getErrors().get(0).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[2], from node: " +
                "LeafNode{value='Tom'} attempting to decode Double",
            values.getErrors().get(1).description());

        Double[] results = values.results();
        Assertions.assertEquals(0.22, results[1]);
    }

    @Test
    void arrayDecodeMapNode() {
        ArrayDecoder<Double> decoder = new ArrayDecoder<>();

        GResultOf<Double[]> values = decoder.decode("db.hosts", Tags.of(),
            new MapNode(new HashMap<>()), TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: MAP, attempting to decode Array",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeMapNodeNullInside() {
        ArrayDecoder<Double> decoder = new ArrayDecoder<>();

        GResultOf<Double[]> values = decoder.decode("db.hosts", Tags.of(), new MapNode(null),
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: MAP, attempting to decode Array",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeListNodeEmpty() {
        ArrayDecoder<Double> decoder = new ArrayDecoder<>();

        GResultOf<Double[]> values = decoder.decode("db.hosts", Tags.of(), new ArrayNode(List.of()),
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(0, values.results().length);
    }

    @Test
    void arrayDecodeListNodeNullInside() {
        ArrayDecoder<Double> decoder = new ArrayDecoder<>();

        GResultOf<Double[]> values = decoder.decode("db.hosts", Tags.of(), new ArrayNode(null),
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(0, values.results().length);
    }

    @Test
    void arrayDecodeNullNode() {
        ArrayDecoder<Double> decoder = new ArrayDecoder<>();

        GResultOf<Double[]> values = decoder.decode("db.hosts", Tags.of(), null,
            TypeCapture.of(Double[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: null, attempting to decode Array",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeLeafWithEscapeComma() {
        ArrayDecoder<String> decoder = new ArrayDecoder<>();

        GResultOf<String[]> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("a,b,c\\,d"),
            TypeCapture.of(String[].class), new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        String[] results = values.results();

        Assertions.assertEquals(3, results.length);
        Assertions.assertEquals("a", results[0]);
        Assertions.assertEquals("b", results[1]);
        Assertions.assertEquals("c,d", results[2]);
    }
}
