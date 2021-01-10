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
import java.util.UUID;

class UUIDDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void name() {
        UUIDDecoder decoder = new UUIDDecoder();
        Assertions.assertEquals("UUID", decoder.name());
    }

    @Test
    void priority() {
        UUIDDecoder decoder = new UUIDDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        UUIDDecoder longDecoder = new UUIDDecoder();

        Assertions.assertTrue(longDecoder.matches(TypeCapture.of(UUID.class)));
        Assertions.assertTrue(longDecoder.matches(new TypeCapture<UUID>() {
        }));
        Assertions.assertFalse(longDecoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(longDecoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(longDecoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(longDecoder.matches(new TypeCapture<List<Long>>() {
        }));
    }

    @Test
    void decode() throws GestaltException {
        UUIDDecoder decoder = new UUIDDecoder();


        UUID uuid = UUID.randomUUID();
        ValidateOf<UUID> validate = decoder.decode("db.port", new LeafNode(uuid.toString()), TypeCapture.of(Long.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(uuid, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        UUIDDecoder decoder = new UUIDDecoder();


        ValidateOf<UUID> validate = decoder.decode("db.port", new LeafNode("asdfasdfsdf"), TypeCapture.of(Long.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a UUID on path: db.port, from node: LeafNode{value='asdfasdfsdf'}",
            validate.getErrors().get(0).description());
    }
}
