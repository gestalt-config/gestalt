package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;
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
    void matches() {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        Assertions.assertTrue(doubleDecoder.matches(TypeCapture.of(Double.class)));
        Assertions.assertTrue(doubleDecoder.matches(new TypeCapture<Double>() {
        }));
        Assertions.assertTrue(doubleDecoder.matches(TypeCapture.of(double.class)));

        Assertions.assertFalse(doubleDecoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(doubleDecoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(doubleDecoder.matches(new TypeCapture<Integer>() {
        }));
        Assertions.assertFalse(doubleDecoder.matches(new TypeCapture<List<Double>>() {
        }));
    }

    @Test
    void decodeDouble() throws GestaltException {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        ValidateOf<Double> validate = doubleDecoder.decode("db.port", new LeafNode("124.5"), TypeCapture.of(Double.class),
            new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124.5f, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeDoubleType() throws GestaltException {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        ValidateOf<Double> validate = doubleDecoder.decode("db.port", new LeafNode("124.5"), new TypeCapture<Double>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124.5f, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeDouble2() throws GestaltException {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        ValidateOf<Double> validate = doubleDecoder.decode("db.port", new LeafNode("124"), TypeCapture.of(Double.class),
            new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notADouble() throws GestaltException {
        DoubleDecoder doubleDecoder = new DoubleDecoder();

        ValidateOf<Double> validate = doubleDecoder.decode("db.port", new LeafNode("12s4"), TypeCapture.of(Double.class),
            new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode Double",
            validate.getErrors().get(0).description());
    }
}

