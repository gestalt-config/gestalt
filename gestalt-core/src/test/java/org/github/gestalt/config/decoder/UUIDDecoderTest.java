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

import java.util.*;

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
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
                Arrays.asList(new StandardPathMapper(), new CamelCasePathMapper())));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(uuid, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        UUIDDecoder decoder = new UUIDDecoder();


        ValidateOf<UUID> validate = decoder.decode("db.port", new LeafNode("asdfasdfsdf"), TypeCapture.of(Long.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
                Arrays.asList(new StandardPathMapper(), new CamelCasePathMapper())));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a UUID on path: db.port, from node: LeafNode{value='asdfasdfsdf'}",
            validate.getErrors().get(0).description());
    }
}
