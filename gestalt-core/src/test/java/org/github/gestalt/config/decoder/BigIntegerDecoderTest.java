package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
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
    void canDecode() {
        BigIntegerDecoder decoder = new BigIntegerDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(BigInteger.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<BigInteger>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Double.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Double>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(double.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Integer>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Double>>() {
        }));
    }

    @Test
    void bigDecimalDecoder() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        GResultOf<BigInteger> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124"),
            TypeCapture.of(Double.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(BigInteger.valueOf(124), result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void bigDecimalDecoderType() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        GResultOf<BigInteger> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124"), new TypeCapture<Double>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(BigInteger.valueOf(124), result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void bigDecimalDecoder2() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        GResultOf<BigInteger> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124"),
            TypeCapture.of(Double.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(BigInteger.valueOf(124), result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void notABigDecimal() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        GResultOf<BigInteger> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
            TypeCapture.of(Double.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode BigInteger",
            result.getErrors().get(0).description());
    }

    @Test
    void notABigDecimalFloat() {
        BigIntegerDecoder doubleDecoder = new BigIntegerDecoder();

        GResultOf<BigInteger> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("124.2"),
            TypeCapture.of(Double.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a number on path: db.port, from node: LeafNode{value='124.2'} " +
                "attempting to decode BigInteger",
            result.getErrors().get(0).description());
    }
}

