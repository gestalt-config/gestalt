package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class ListDecoderTest {
    final DoubleDecoder doubleDecoder = new DoubleDecoder();
    final StringDecoder stringDecoder = new StringDecoder();
    final ListDecoder listDecoder = new ListDecoder();

    ConfigNodeService configNodeService;
    DecoderService decoderService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(List.of(doubleDecoder, stringDecoder, listDecoder), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        ListDecoder decoder = new ListDecoder();
        Assertions.assertEquals("List", decoder.name());
    }

    @Test
    void priority() {
        ListDecoder decoder = new ListDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        ListDecoder decoder = new ListDecoder();
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<String>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<AbstractList<String>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<CopyOnWriteArrayList<String>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<ArrayList<String>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<LinkedList<String>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Stack<String>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Vector<String>>() {
        }));


        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<String>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Long>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(List.class)));


        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Set.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Set<String>>() {
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

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

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

        ConfigNode nodes = new ArrayNode(List.of(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<List<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());

        Assertions.assertEquals(0.1111, values.results().get(0));
        Assertions.assertEquals(0.222, values.results().get(1));
        Assertions.assertEquals(0.33, values.results().get(2));
    }

    @Test
    void arrayDecodeArrayList() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<ArrayList<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertInstanceOf(ArrayList.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));
    }

    @Test
    void arrayDecodeAbstractList() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<AbstractList<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertInstanceOf(AbstractList.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));
    }

    @Test
    void arrayDecodeCopyOnWriteArrayList() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<CopyOnWriteArrayList<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertInstanceOf(CopyOnWriteArrayList.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));
    }

    @Test
    void arrayDecodeLinkedList() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<LinkedList<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertInstanceOf(LinkedList.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));
    }

    @Test
    void arrayDecodeStack() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Stack<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertInstanceOf(Stack.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));
    }

    @Test
    void arrayDecodeVector() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Vector<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertInstanceOf(Vector.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));
    }

    @Test
    void arrayDecodeUnknownListDefaultToArrayList() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertInstanceOf(ArrayList.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));
    }

    @Test
    void arrayDecodeDoublesMissingIndex() {

        ConfigNode[] arrayNode = new ConfigNode[4];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[3] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<List<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

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

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("0.1111, 0.22"), new TypeCapture<List<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.results().size());
        Assertions.assertEquals(0.1111, values.results().get(0));
        Assertions.assertEquals(0.22, values.results().get(1));
    }

    @Test
    void arrayDecodeLeafWithEscapeComma() {
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("a,b,c\\,d"), new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("a", values.results().get(0));
        Assertions.assertEquals("b", values.results().get(1));
        Assertions.assertEquals("c,d", values.results().get(2));
    }

    @Test
    void arrayDecodeNullLeaf() {
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode(null), new TypeCapture<List<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.hosts, has no value attempting to decode List",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeNullNode() {
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), null, new TypeCapture<List<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: null, attempting to decode List",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeEmptyArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(List.of());
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().size());
    }

    @Test
    void arrayDecodeNullArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(null);
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().size());
    }

    @Test
    void arrayDecodeEmptyLeafNodeOk() {
        ConfigNode nodes = new LeafNode("");
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(1, values.results().size());
        Assertions.assertEquals("", values.results().get(0));
    }


    @Test
    void arrayDecodeWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Matt");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<List<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

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
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<List<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

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

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), new MapNode(new HashMap<>()), new TypeCapture<List<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: MAP, attempting to decode List",
            values.getErrors().get(0).description());
    }
}
