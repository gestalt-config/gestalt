package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.CamelCasePathMapper;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class ShortDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void name() {
        ShortDecoder decoder = new ShortDecoder();
        Assertions.assertEquals("Short", decoder.name());
    }

    @Test
    void priority() {
        ShortDecoder decoder = new ShortDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        ShortDecoder decoder = new ShortDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(Short.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<Short>() {
        }));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(short.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Integer>>() {
        }));
    }

    @Test
    void decode() throws GestaltException {
        ShortDecoder decoder = new ShortDecoder();

        ValidateOf<Short> validate = decoder.decode("db.port", new LeafNode("124"), TypeCapture.of(Short.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
                Arrays.asList(new StandardPathMapper(), new CamelCasePathMapper())));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals((short) 124, (short) validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notAnInteger() throws GestaltException {
        ShortDecoder decoder = new ShortDecoder();

        ValidateOf<Short> validate = decoder.decode("db.port", new LeafNode("12s4"), TypeCapture.of(Short.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
                Arrays.asList(new StandardPathMapper(), new CamelCasePathMapper())));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode Short",
            validate.getErrors().get(0).description());
    }

    @Test
    void notAShortTooLarge() throws GestaltException {
        ShortDecoder decoder = new ShortDecoder();

        ValidateOf<Short> validate = decoder.decode("db.port", new LeafNode("12345678901234567890123456789012345678901234567890123456789"),
            TypeCapture.of(Short.class), new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
                Arrays.asList(new StandardPathMapper(), new CamelCasePathMapper())));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a number on path: db.port, from node: " +
                "LeafNode{value='12345678901234567890123456789012345678901234567890123456789'} attempting to decode Short",
            validate.getErrors().get(0).description());
    }
}
