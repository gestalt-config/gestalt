package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

class DateDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new DateDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        DateDecoder decoder = new DateDecoder();
        Assertions.assertEquals("Date", decoder.name());
    }

    @Test
    void priority() {
        DateDecoder decoder = new DateDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        DateDecoder decoder = new DateDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Pattern.class)));
    }

    @Test
    void decode() {
        DateDecoder decoder = new DateDecoder();

        Instant instant = Instant.now();

        LocalDateTime ldt = LocalDateTime.parse(instant.toString(), DateTimeFormatter.ISO_DATE_TIME);
        Instant instant2 = ldt.atZone(ZoneId.systemDefault()).toInstant();
        Date newDate = Date.from(instant2);


        GResultOf<Date> result = decoder.decode("db.user", Tags.of(), new LeafNode(instant.toString()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(newDate, result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeFormatNull() {
        DateDecoder decoder = new DateDecoder(null);

        Instant instant = Instant.now();

        LocalDateTime ldt = LocalDateTime.parse(instant.toString(), DateTimeFormatter.ISO_DATE_TIME);
        Instant instant2 = ldt.atZone(ZoneId.systemDefault()).toInstant();
        Date newDate = Date.from(instant2);


        GResultOf<Date> result = decoder.decode("db.user", Tags.of(), new LeafNode(instant.toString()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(newDate, result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeFormatter() throws GestaltException {
        DateDecoder decoder = new DateDecoder("yyyy-MM-dd'T'HH:mm:ss'Z'");

        DecoderRegistry decoderService = new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
            List.of(new StandardPathMapper()));
        String date = "2021-01-10T01:01:06Z";

        LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();

        GResultOf<Date> result = decoder.decode("db.user", Tags.of(), new LeafNode(date),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(Date.from(instant), result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeBadDate() {
        DateDecoder decoder = new DateDecoder();

        String now = "not a date";

        GResultOf<Date> result = decoder.decode("db.user", Tags.of(), new LeafNode(now),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a Date on path: db.user, from node: LeafNode{value='not a date'}, with reason: " +
                "Text 'not a date' could not be parsed at index 0",
            result.getErrors().get(0).description());
    }

    @Test
    void invalidLeafNode() {
        DateDecoder decoder = new DateDecoder();

        GResultOf<Date> result = decoder.decode("db.user", Tags.of(), new LeafNode(null),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode Date",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() {
        DateDecoder decoder = new DateDecoder();

        GResultOf<Date> result = decoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode Date",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() {
        DateDecoder decoder = new DateDecoder();

        GResultOf<Date> result = decoder.decode("db.user", Tags.of(), null,
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode Date",
            result.getErrors().get(0).description());
    }
}
