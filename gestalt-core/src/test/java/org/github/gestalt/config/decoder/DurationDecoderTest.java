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

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class DurationDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void name() {
        DurationDecoder decoder = new DurationDecoder();
        Assertions.assertEquals("Duration", decoder.name());
    }

    @Test
    void priority() {
        DurationDecoder decoder = new DurationDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        DurationDecoder decoder = new DurationDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(Duration.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<Duration>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Long>>() {
        }));
    }

    @Test
    void decode() throws GestaltException {
        DurationDecoder decoder = new DurationDecoder();

        ValidateOf<Duration> validate = decoder.decode("db.port", new LeafNode("124"), TypeCapture.of(Long.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(Duration.ofMillis(124L), validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        DurationDecoder decoder = new DurationDecoder();

        ValidateOf<Duration> validate = decoder.decode("db.port", new LeafNode("12s4"), TypeCapture.of(Long.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode Duration",
            validate.getErrors().get(0).description());
    }

}
