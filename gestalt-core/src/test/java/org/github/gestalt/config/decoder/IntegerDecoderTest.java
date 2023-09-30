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
import java.util.Date;
import java.util.List;

class IntegerDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new IntegerDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        IntegerDecoder decoder = new IntegerDecoder();
        Assertions.assertEquals("Integer", decoder.name());
    }

    @Test
    void priority() {
        IntegerDecoder decoder = new IntegerDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        IntegerDecoder integerDecoder = new IntegerDecoder();

        Assertions.assertTrue(integerDecoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertTrue(integerDecoder.matches(new TypeCapture<Integer>() {
        }));
        Assertions.assertTrue(integerDecoder.matches(TypeCapture.of(int.class)));

        Assertions.assertFalse(integerDecoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(integerDecoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(integerDecoder.matches(new TypeCapture<List<Integer>>() {
        }));
    }

    @Test
    void decode() throws GestaltException {
        IntegerDecoder integerDecoder = new IntegerDecoder();

        ValidateOf<Integer> validate = integerDecoder.decode("db.port", new LeafNode("124"), TypeCapture.of(Integer.class),
            new DecoderContext(decoderService, null) );
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notAnInteger() throws GestaltException {
        IntegerDecoder integerDecoder = new IntegerDecoder();

        ValidateOf<Integer> validate = integerDecoder.decode("db.port", new LeafNode("12s4"), TypeCapture.of(Integer.class),
            new DecoderContext(decoderService, null) );
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode Integer",
            validate.getErrors().get(0).description());
    }

    @Test
    void notAIntegerTooLarge() throws GestaltException {
        IntegerDecoder decoder = new IntegerDecoder();

        ValidateOf<Integer> validate = decoder.decode("db.port",
            new LeafNode("12345678901234567890123456789012345678901234567890123456789"),
            TypeCapture.of(Integer.class), new DecoderContext(decoderService, null) );
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a number on path: db.port, from node: " +
                "LeafNode{value='12345678901234567890123456789012345678901234567890123456789'} attempting to decode Integer",
            validate.getErrors().get(0).description());
    }
}
