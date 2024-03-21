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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

class LocalDateTimeDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new LocalDateTimeDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();
        Assertions.assertEquals("LocalDateTime", decoder.name());
    }

    @Test
    void priority() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(LocalDateTime.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
    }

    @Test
    void decode() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        String now = Instant.now().toString();

        GResultOf<LocalDateTime> result = decoder.decode("db.user", Tags.of(), new LeafNode(now),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(now, result.results().toString() + "Z");
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeFormatNull() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder(null);

        String now = Instant.now().toString();

        GResultOf<LocalDateTime> result = decoder.decode("db.user", Tags.of(), new LeafNode(now),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(now, result.results().toString() + "Z");
        Assertions.assertEquals(0, result.getErrors().size());
    }


    @Test
    void decodeFormatter() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder("yyyy-MM-dd'T'HH:mm:ss'Z'");

        String date = "2021-01-10T01:01:06Z";
        LocalDateTime localDate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

        GResultOf<LocalDateTime> result = decoder.decode("db.user", Tags.of(), new LeafNode(date),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(localDate, result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeBadDate() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        String now = "not a date";

        GResultOf<LocalDateTime> result = decoder.decode("db.user", Tags.of(), new LeafNode(now),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a LocalDateTime on path: db.user, from node: LeafNode{value='not a date'}, with " +
                "reason: Text 'not a date' could not be parsed at index 0",
            result.getErrors().get(0).description());
    }

    @Test
    void invalidLeafNode() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        GResultOf<LocalDateTime> result = decoder.decode("db.user", Tags.of(), new LeafNode(null),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode LocalDateTime",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        GResultOf<LocalDateTime> result = decoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode LocalDateTime",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        GResultOf<LocalDateTime> result = decoder.decode("db.user", Tags.of(), null,
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode LocalDateTime",
            result.getErrors().get(0).description());
    }
}
