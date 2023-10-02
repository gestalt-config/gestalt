package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

class BigIntegerDecoderTest {

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
        BigIntegerDecoder decoder = new BigIntegerDecoder();
        Assertions.assertEquals("BigInteger", decoder.name());
    }

    @Test
    void priority() {
        BigIntegerDecoder decoder = new BigIntegerDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        BigIntegerDecoder decoder = new BigIntegerDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(BigInteger.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<BigInteger>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(Double.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Double>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(double.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Integer>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Double>>() {
        }));
    }

    @Test
    void bigDecimalDecoder() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124"),
                TypeCapture.of(Double.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(BigInteger.valueOf(124), validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void bigDecimalDecoderType() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124"), new TypeCapture<Double>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(BigInteger.valueOf(124), validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void bigDecimalDecoder2() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124"),
                TypeCapture.of(Double.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(BigInteger.valueOf(124), validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notABigDecimal() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
                TypeCapture.of(Double.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode BigInteger",
            validate.getErrors().get(0).description());
    }

    @Test
    void notABigDecimalFloat() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124.2"),
                TypeCapture.of(Double.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a number on path: db.port, from node: LeafNode{value='124.2'} " +
                "attempting to decode BigInteger",
            validate.getErrors().get(0).description());
    }
}

