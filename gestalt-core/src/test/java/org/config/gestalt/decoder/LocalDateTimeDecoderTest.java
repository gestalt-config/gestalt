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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

class LocalDateTimeDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
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
    void matches() {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(LocalDateTime.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
    }

    @Test
    void decode() throws GestaltException {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        String now = Instant.now().toString();

        ValidateOf<LocalDateTime> validate = decoder.decode("db.user", new LeafNode(now), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(now, validate.results().toString() + "Z");
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeFormatNull() throws GestaltException {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder(null);

        String now = Instant.now().toString();

        ValidateOf<LocalDateTime> validate = decoder.decode("db.user", new LeafNode(now), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(now, validate.results().toString() + "Z");
        Assertions.assertEquals(0, validate.getErrors().size());
    }


    @Test
    void decodeFormatter() throws GestaltException {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder("yyyy-MM-dd'T'HH:mm:ss'Z'");

        String date = "2021-01-10T01:01:06Z";
        LocalDateTime localDate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

        ValidateOf<LocalDateTime> validate = decoder.decode("db.user", new LeafNode(date), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(localDate, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeBadDate() throws GestaltException {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        String now = "not a date";

        ValidateOf<LocalDateTime> validate = decoder.decode("db.user", new LeafNode(now), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a LocalDateTime on path: db.user, from node: LeafNode{value='not a date'}",
            validate.getErrors().get(0).description());
    }

    @Test
    void invalidLeafNode() throws GestaltException {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        ValidateOf<LocalDateTime> validate = decoder.decode("db.user", new LeafNode(null), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, missing value, LeafNode{value='null'} attempting to decode LocalDateTime",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        LocalDateTimeDecoder decoder = new LocalDateTimeDecoder();

        ValidateOf<LocalDateTime> validate = decoder.decode("db.user", new MapNode(new HashMap<>()), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type, received: MapNode{mapNode={}} " +
                "attempting to decode LocalDateTime",
            validate.getErrors().get(0).description());
    }
}
