package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

class InstantDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new InstantDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
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
    void canDecode() {
        InstantDecoder decoder = new InstantDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Instant.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
    }

    @Test
    void decode() {
        InstantDecoder decoder = new InstantDecoder();

        String now = Instant.now().toString();

        GResultOf<Instant> result = decoder.decode("db.user", Tags.of(), new LeafNode(now),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(now, result.results().toString());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeBadDate() {
        InstantDecoder decoder = new InstantDecoder();

        String now = "not a date";

        GResultOf<Instant> result = decoder.decode("db.user", Tags.of(), new LeafNode(now),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a Instant on path: db.user, from node: LeafNode{value='not a date'}, with reason: " +
                "Text 'not a date' could not be parsed at index 0",
            result.getErrors().get(0).description());
    }

    @Test
    void invalidLeafNode() {
        InstantDecoder decoder = new InstantDecoder();

        GResultOf<Instant> result = decoder.decode("db.user", Tags.of(), new LeafNode(null),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode Instant",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() {
        InstantDecoder decoder = new InstantDecoder();

        GResultOf<Instant> result = decoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode Instant",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() {
        InstantDecoder decoder = new InstantDecoder();

        GResultOf<Instant> result = decoder.decode("db.user", Tags.of(), null,
            TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode Instant",
            result.getErrors().get(0).description());
    }
}
