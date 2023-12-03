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
import org.github.gestalt.config.utils.ValidateOf;
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
    void decode() throws GestaltException {
        DateDecoder decoder = new DateDecoder();

        Instant instant = Instant.now();

        LocalDateTime ldt = LocalDateTime.parse(instant.toString(), DateTimeFormatter.ISO_DATE_TIME);
        Instant instant2 = ldt.atZone(ZoneId.systemDefault()).toInstant();
        Date newDate = Date.from(instant2);


        ValidateOf<Date> validate = decoder.decode("db.user", Tags.of(), new LeafNode(instant.toString()),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(newDate, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeFormatNull() throws GestaltException {
        DateDecoder decoder = new DateDecoder(null);

        Instant instant = Instant.now();

        LocalDateTime ldt = LocalDateTime.parse(instant.toString(), DateTimeFormatter.ISO_DATE_TIME);
        Instant instant2 = ldt.atZone(ZoneId.systemDefault()).toInstant();
        Date newDate = Date.from(instant2);


        ValidateOf<Date> validate = decoder.decode("db.user", Tags.of(), new LeafNode(instant.toString()),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(newDate, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeFormatter() throws GestaltException {
        DateDecoder decoder = new DateDecoder("yyyy-MM-dd'T'HH:mm:ss'Z'");

        DecoderRegistry decoderService = new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
            List.of(new StandardPathMapper()));
        String date = "2021-01-10T01:01:06Z";

        LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();

        ValidateOf<Date> validate = decoder.decode("db.user", Tags.of(), new LeafNode(date),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(Date.from(instant), validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeBadDate() throws GestaltException {
        DateDecoder decoder = new DateDecoder();

        String now = "not a date";

        ValidateOf<Date> validate = decoder.decode("db.user", Tags.of(), new LeafNode(now),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a Date on path: db.user, from node: LeafNode{value='not a date'}",
            validate.getErrors().get(0).description());
    }

    @Test
    void invalidLeafNode() throws GestaltException {
        DateDecoder decoder = new DateDecoder();

        ValidateOf<Date> validate = decoder.decode("db.user", Tags.of(), new LeafNode(null),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode Date",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        DateDecoder decoder = new DateDecoder();

        ValidateOf<Date> validate = decoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode Date",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() throws GestaltException {
        DateDecoder decoder = new DateDecoder();

        ValidateOf<Date> validate = decoder.decode("db.user", Tags.of(), null,
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode Date",
            validate.getErrors().get(0).description());
    }
}
