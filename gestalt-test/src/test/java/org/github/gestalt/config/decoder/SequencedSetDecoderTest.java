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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class SequencedSetDecoderTest {
    final DoubleDecoder doubleDecoder = new DoubleDecoder();
    final StringDecoder stringDecoder = new StringDecoder();
    final ListDecoder listDecoder = new ListDecoder();

    final SetDecoder setDecoder = new SetDecoder();

    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        lexer = new PathLexer();
        decoderService = new DecoderRegistry(List.of(doubleDecoder, stringDecoder, listDecoder, setDecoder), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }


    @Test
    void canDecode() {
        SetDecoder decoder = new SetDecoder();

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
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<SequencedSet<String>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        Set<String> results = (Set<String>) values.results();

        assertThat(results)
            .contains("John")
            .contains("Steve")
            .contains("Matt");

        SequencedSet<String> results2 = (SequencedSet<String>) values.results();

        Assertions.assertInstanceOf(SequencedSet.class, values.results());
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
        SetDecoder decoder = new SetDecoder();

        GResultOf<Set<?>> values = decoder.decode("", Tags.of(), nodes, new TypeCapture<SequencedSet<Double>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());

        Set<Double> results = (Set<Double>) values.results();
        assertThat(results)
            .contains(0.1111)
            .contains(0.222)
            .contains(0.33);

        SequencedSet<Double> results2 = (SequencedSet<Double>) values.results();
        Assertions.assertInstanceOf(SequencedSet.class, values.results());
        Assertions.assertEquals(0.1111, results2.removeFirst());
        Assertions.assertEquals(0.222, results2.removeFirst());
        Assertions.assertEquals(0.33, results2.removeFirst());
    }
}
