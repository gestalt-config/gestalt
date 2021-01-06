package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.node.ConfigNodeService;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Date;
import java.util.List;

class LongDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void name() {
        LongDecoder decoder = new LongDecoder();
        Assertions.assertEquals("Long", decoder.name());
    }

    @Test
    void priority() {
        LongDecoder decoder = new LongDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        LongDecoder longDecoder = new LongDecoder();

        Assertions.assertTrue(longDecoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertTrue(longDecoder.matches(new TypeCapture<Long>() {
        }));
        Assertions.assertTrue(longDecoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(longDecoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(longDecoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(longDecoder.matches(new TypeCapture<List<Long>>() {
        }));
    }

    @Test
    void decode() throws GestaltException {
        LongDecoder longDecoder = new LongDecoder();

        ValidateOf<Long> validate = longDecoder.decode("db.port", new LeafNode("124"), TypeCapture.of(Long.class),
            new DecoderRegistry(Collections.singletonList(longDecoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124L, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notALong() throws GestaltException {
        LongDecoder longDecoder = new LongDecoder();

        ValidateOf<Long> validate = longDecoder.decode("db.port", new LeafNode("12s4"), TypeCapture.of(Long.class),
            new DecoderRegistry(Collections.singletonList(longDecoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: " +
                "LeafNode{value='12s4'} attempting to decode Long",
            validate.getErrors().get(0).description());
    }

    @Test
    void notALongTooLarge() throws GestaltException {
        LongDecoder decoder = new LongDecoder();

        ValidateOf<Long> validate = decoder.decode("db.port", new LeafNode("12345678901234567890123456789012345678901234567890123456"),
            TypeCapture.of(Long.class), new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a number on path: db.port, from node: " +
                "LeafNode{value='12345678901234567890123456789012345678901234567890123456'} attempting to decode Long",
            validate.getErrors().get(0).description());
    }
}
