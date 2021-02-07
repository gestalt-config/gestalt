package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reflect.TypeCapture;
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

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
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
    void bigDecimalDecoder() throws GestaltException {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", new LeafNode("124"), TypeCapture.of(Double.class),
            new DecoderRegistry(Collections.singletonList(doubleDecoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(BigInteger.valueOf(124), validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void bigDecimalDecoderType() throws GestaltException {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", new LeafNode("124"), new TypeCapture<Double>() {
        }, new DecoderRegistry(Collections.singletonList(doubleDecoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(BigInteger.valueOf(124), validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void bigDecimalDecoder2() throws GestaltException {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", new LeafNode("124"), TypeCapture.of(Double.class),
            new DecoderRegistry(Collections.singletonList(doubleDecoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(BigInteger.valueOf(124), validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notABigDecimal() throws GestaltException {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", new LeafNode("12s4"), TypeCapture.of(Double.class),
            new DecoderRegistry(Collections.singletonList(doubleDecoder), configNodeService, lexer));
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
    void notABigDecimalFloat() throws GestaltException {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        ValidateOf<BigInteger> validate = doubleDecoder.decode("db.port", new LeafNode("124.2"), TypeCapture.of(Double.class),
            new DecoderRegistry(Collections.singletonList(doubleDecoder), configNodeService, lexer));
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

