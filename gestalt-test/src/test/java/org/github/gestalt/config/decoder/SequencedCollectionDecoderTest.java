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

class SequencedCollectionDecoderTest {
    final DoubleDecoder doubleDecoder = new DoubleDecoder();
    final StringDecoder stringDecoder = new StringDecoder();
    final ListDecoder listDecoder = new ListDecoder();


    ConfigNodeService configNodeService;
    DecoderService decoderService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        lexer = new PathLexer();
        decoderService = new DecoderRegistry(List.of(doubleDecoder, stringDecoder, listDecoder), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void canDecode() {
        ListDecoder decoder = new ListDecoder();

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
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<SequencedCollection<String>>() {
        }, new DecoderContext(decoderService, null, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals("John", values.results().get(0));
        Assertions.assertEquals("Steve", values.results().get(1));
        Assertions.assertEquals("Matt", values.results().get(2));

        SequencedCollection<?> sq = values.results();
        Assertions.assertInstanceOf(SequencedCollection.class, values.results());
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
        ListDecoder decoder = new ListDecoder();

        GResultOf<List<?>> values = decoder.decode("db.hosts", Tags.of(), nodes, new TypeCapture<SequencedCollection<Double>>() {
        }, new DecoderContext(decoderService, null, null));

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());

        Assertions.assertEquals(0.1111, values.results().get(0));
        Assertions.assertEquals(0.222, values.results().get(1));
        Assertions.assertEquals(0.33, values.results().get(2));

        SequencedCollection<?> sq = values.results();
        Assertions.assertInstanceOf(SequencedCollection.class, values.results());
        Assertions.assertEquals(3, sq.size());
        Assertions.assertEquals(0.1111, sq.removeFirst());
        Assertions.assertEquals(0.222, sq.removeFirst());
        Assertions.assertEquals(0.33, sq.removeFirst());
    }
}
