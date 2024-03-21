package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

class DoubleDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new DoubleDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        DoubleDecoder decoder = new DoubleDecoder();
        Assertions.assertEquals("Double", decoder.name());
    }

    @Test
    void priority() {
        DoubleDecoder decoder = new DoubleDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        Assertions.assertTrue(doubleDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Double.class)));
        Assertions.assertTrue(doubleDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Double>() {
        }));
        Assertions.assertTrue(doubleDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(double.class)));

        Assertions.assertFalse(doubleDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(doubleDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(doubleDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Integer>() {
        }));
        Assertions.assertFalse(doubleDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Double>>() {
        }));
    }

    @Test
    void decodeDouble() {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        GResultOf<Double> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124.5"),
            TypeCapture.of(Double.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(124.5f, result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeDoubleType() {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        GResultOf<Double> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124.5"), new TypeCapture<Double>() {
        }, new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(124.5f, result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeDouble2() {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        GResultOf<Double> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124"),
            TypeCapture.of(Double.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(124, result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void notADouble() {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        GResultOf<Double> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
            TypeCapture.of(Double.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode Double",
            result.getErrors().get(0).description());
    }
}

