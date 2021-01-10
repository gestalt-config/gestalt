package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.node.ConfigNodeService;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.node.MapNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

class InstantDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void name() {
        InstantDecoder decoder = new InstantDecoder();
        Assertions.assertEquals("Instant", decoder.name());
    }

    @Test
    void priority() {
        InstantDecoder decoder = new InstantDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        InstantDecoder decoder = new InstantDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(Instant.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
    }

    @Test
    void decode() throws GestaltException {
        InstantDecoder decoder = new InstantDecoder();

        String now = Instant.now().toString();

        ValidateOf<Instant> validate = decoder.decode("db.user", new LeafNode(now), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(now, validate.results().toString());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeBadDate() throws GestaltException {
        InstantDecoder decoder = new InstantDecoder();

        String now = "not a date";

        ValidateOf<Instant> validate = decoder.decode("db.user", new LeafNode(now), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a Instant on path: db.user, from node: LeafNode{value='not a date'}",
            validate.getErrors().get(0).description());
    }

    @Test
    void invalidLeafNode() throws GestaltException {
        InstantDecoder decoder = new InstantDecoder();

        ValidateOf<Instant> validate = decoder.decode("db.user", new LeafNode(null), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, missing value, LeafNode{value='null'} attempting to decode Instant",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        InstantDecoder decoder = new InstantDecoder();

        ValidateOf<Instant> validate = decoder.decode("db.user", new MapNode(new HashMap<>()), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type, received: MapNode{mapNode={}} " +
                "attempting to decode Instant",
            validate.getErrors().get(0).description());
    }
}
