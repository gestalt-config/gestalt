package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class SequencedSetDecoderTest {
    final DoubleDecoder doubleDecoder = new DoubleDecoder();
    final StringDecoder stringDecoder = new StringDecoder();
    final ListDecoder listDecoder = new ListDecoder();

    final SetDecoder setDecoder = new SetDecoder();

    final SequencedSetDecoder sequencedSetDecoder = new SequencedSetDecoder();

    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        lexer = new PathLexer();
        decoderService = new DecoderRegistry(List.of(doubleDecoder, stringDecoder, sequencedSetDecoder, listDecoder, setDecoder), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        SequencedSetDecoder decoder = new SequencedSetDecoder();
        Assertions.assertEquals("SequencedSet", decoder.name());
    }

    @Test
    void priority() {
        SequencedSetDecoder decoder = new SequencedSetDecoder();
        Assertions.assertEquals(Priority.HIGH, decoder.priority());
    }

    @Test
    void canDecode() {
        SequencedSetDecoder decoder = new SequencedSetDecoder();
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

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<SequencedCollection<String>>() {
        }));

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<SequencedSet<String>>() {
        }));

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<LinkedHashSet<String>>() {
        }));
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeStrings() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<Set<String>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        Set<String> results = (Set<String>) values.results();

        assertThat(results)
            .contains("John")
            .contains("Steve")
            .contains("Matt");

        SequencedSet<String> results2 = (SequencedSet<String>) values.results();


        Assertions.assertEquals("John", results2.removeFirst());
        Assertions.assertEquals("Steve", results2.removeFirst());
        Assertions.assertEquals("Matt", results2.removeFirst());
        Assertions.assertTrue(results2.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[2] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());

        Set<Double> results = (Set<Double>) values.results();
        assertThat(results)
            .contains(0.1111)
            .contains(0.222)
            .contains(0.33);

        SequencedSet<Double> results2 = (SequencedSet<Double>) values.results();
        Assertions.assertEquals(0.1111, results2.removeFirst());
        Assertions.assertEquals(0.222, results2.removeFirst());
        Assertions.assertEquals(0.33, results2.removeFirst());
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeDoublesMissingIndex() {

        ConfigNode[] arrayNode = new ConfigNode[4];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[3] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Missing array index: 2 for path: db.hosts", values.getErrors().getFirst().description());

        Set<Double> results = (Set<Double>) values.results();
        assertThat(results)
            .contains(0.1111)
            .contains(0.222)
            .contains(0.33);

        SequencedSet<Double> results2 = (SequencedSet<Double>) values.results();
        Assertions.assertEquals(0.1111, results2.removeFirst());
        Assertions.assertEquals(0.222, results2.removeFirst());
        Assertions.assertEquals(0.33, results2.removeFirst());
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeLeaf() {
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("0.1111, 0.22"), new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.results().size());
        Set<Double> results = (Set<Double>) values.results();
        assertThat(results)
            .contains(0.1111)
            .contains(0.22);

        SequencedSet<Double> results2 = (SequencedSet<Double>) values.results();
        Assertions.assertEquals(0.1111, results2.removeFirst());
        Assertions.assertEquals(0.22, results2.removeFirst());
    }

    @Test
    @SuppressWarnings("unchecked")
    void arrayDecodeLeafWithEscapeComma() {
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode("a,b,c\\,d"), new TypeCapture<List<String>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(3, values.results().size());
        Set<String> results = (Set<String>) values.results();
        assertThat(results)
            .contains("a")
            .contains("b")
            .contains("c,d");

        SequencedSet<String> results2 = (SequencedSet<String>) values.results();
        Assertions.assertEquals("a", results2.removeFirst());
        Assertions.assertEquals("b", results2.removeFirst());
        Assertions.assertEquals("c,d", results2.removeFirst());
    }

    @Test
    void arrayDecodeNullLeaf() {
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), new LeafNode(null), new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.hosts, has no value attempting to decode SequencedSet",
            values.getErrors().getFirst().description());
    }

    @Test
    void arrayDecodeNullNode() {
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), null, new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: null, attempting to decode SequencedSet",
            values.getErrors().getFirst().description());
    }


    @Test
    void arrayDecodeEmptyArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(List.of());
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<String>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().size());
    }

    @Test
    void arrayDecodeNullArrayNodeOk() {
        ConfigNode nodes = new ArrayNode(null);
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<String>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(0, values.results().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void arrayDecodeEmptyLeafNodeOk() {
        ConfigNode nodes = new LeafNode("");
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<String>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(1, values.results().size());
        Assertions.assertTrue(values.results().contains(""));

        Assertions.assertEquals(1, values.results().size());
        Assertions.assertTrue(values.results().contains(""));

        SequencedSet<String> results2 = (SequencedSet<String>) values.results();
        Assertions.assertEquals("", results2.removeFirst());
    }

    @Test
    void arrayDecodeWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Matt");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null));

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
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null));

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

        SequencedSet<Double> results2 = (SequencedSet<Double>) values.results();
        Assertions.assertEquals(0.22, results2.removeFirst());
    }

    @Test
    void arrayDecodeMapNode() {
        SequencedSetDecoder decoder = new SequencedSetDecoder();

        ValidateOf<Set<?>> values = decoder.decode("db.hosts", Tags.of(), new MapNode(new HashMap<>()), new TypeCapture<Set<Double>>() {
        }, new DecoderContext(decoderService, null));

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array on path: db.hosts, received node type: MAP, attempting to decode SequencedSet",
            values.getErrors().getFirst().description());
    }
}
