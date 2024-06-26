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

import static org.assertj.core.api.Assertions.assertThat;

class SetDecoderTest {
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
    void name() {
        SetDecoder decoder = new SetDecoder();
        Assertions.assertEquals("Set", decoder.name());
    }

    @Test
    void priority() {
        SetDecoder decoder = new SetDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        SetDecoder decoder = new SetDecoder();
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Set<String>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<HashSet<String>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<TreeSet<String>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<LinkedHashSet<String>>() {
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
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeStrings() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<Set<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        Set<String> results = (Set<String>) values.results();

        assertThat(results)
            .contains("John")
            .contains("Steve")
            .contains("Matt");
    }

    @Test
    @SuppressWarnings("unchecked")
    void setDecodeHashSet() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<HashSet<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertInstanceOf(HashSet.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Set<String> results = (Set<String>) values.results();

        assertThat(results)
            .contains("John")
            .contains("Steve")
            .contains("Matt");
    }

    @Test
    @SuppressWarnings("unchecked")
    void setDecodeTreeSet() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<TreeSet<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertInstanceOf(TreeSet.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Set<String> results = (Set<String>) values.results();

        assertThat(results)
            .contains("John")
            .contains("Steve")
            .contains("Matt");
    }

    @Test
    @SuppressWarnings("unchecked")
    void setDecodeLinkedHashSet() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<LinkedHashSet<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertInstanceOf(LinkedHashSet.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Set<String> results = (Set<String>) values.results();

        assertThat(results)
            .contains("John")
            .contains("Steve")
            .contains("Matt");
    }

    @Test
    @SuppressWarnings("unchecked")
    void setDecodeUnknownSetDefaultToHashSet() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        // this is bad mojo, but it is only a test.
        // we need to test the defaulting behavior by passing in an unknown set.
        // since I included all maps we pass in a List, this will not be found and will default to a
        // hashSet. In the real case it will not get called as the canDecode will return false
        GResultOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertInstanceOf(HashSet.class, values.results());
        Assertions.assertEquals(3, values.results().size());
        Set<String> results = (Set<String>) values.results();

        assertThat(results)
            .contains("John")
            .contains("Steve")
            .contains("Matt");
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[2] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());

        Set<Double> results = (Set<Double>) values.results();
        assertThat(results)
            .contains(0.1111)
            .contains(0.222)
            .contains(0.33);
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeDoublesMissingIndex() {

        ConfigNode[] arrayNode = new ConfigNode[4];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[3] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Missing array index: 2 for path: db.hosts", values.getErrors().get(0).description());

        Set<Double> results = (Set<Double>) values.results();
        assertThat(results)
            .contains(0.1111)
            .contains(0.222)
            .contains(0.33);
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeLeaf() {
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("0.1111, 0.22"), new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.results().size());
        Set<Double> results = (Set<Double>) values.results();
        assertThat(results)
            .contains(0.1111)
            .contains(0.22);
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeLeafWithEscapeComma() {
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("a,b,c\\,d"), new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(3, values.results().size());
        Set<String> results = (Set<String>) values.results();
        assertThat(results)
            .contains("a")
            .contains("b")
            .contains("c,d");
    }

    @Test
    void arrayDecodeNullLeaf() {
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode(null), new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.hosts, has no value attempting to decode Set",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeNullNode() {
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), null, new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: null, attempting to decode Set",
            values.getErrors().get(0).description());
    }


    @Test
    void arrayDecodeEmptyArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(List.of());
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().size());
    }

    @Test
    void arrayDecodeNullArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(null);
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().size());
    }

    @Test
    void arrayDecodeEmptyLeafNodeOk() {
        ConfigNode nodes = new LeafNode("");
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(1, values.results().size());
        Assertions.assertTrue(values.results().contains(""));
    }

    @Test
    void arrayDecodeWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Matt");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<Double>>() {
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
    @SuppressWarnings("unchecked")
    void arrayDecodeMixedWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("0.22");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[0], from node: LeafNode{value='John'} " +
                "attempting to decode Double",
            values.getErrors().get(0).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[2], from node: LeafNode{value='Tom'} " +
                "attempting to decode Double",
            values.getErrors().get(1).description());

        Set<Double> results = (Set<Double>) values.results();
        assertThat(results)
            .contains(0.22);
    }

    @Test
    void arrayDecodeMapNode() {
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), new MapNode(new HashMap<>()), new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: MAP, attempting to decode Set",
            values.getErrors().get(0).description());
    }
}
