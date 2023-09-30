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
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

class LocalDateDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new LocalDateDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        LocalDateDecoder decoder = new LocalDateDecoder();
        Assertions.assertEquals("LocalDate", decoder.name());
    }

    @Test
    void priority() {
        LocalDateDecoder decoder = new LocalDateDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        LocalDateDecoder decoder = new LocalDateDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(LocalDate.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(LocalDateTime.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
    }

    @Test
    void decode() throws GestaltException {
        LocalDateDecoder decoder = new LocalDateDecoder();

        String date = "2021-01-10";
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);

        ValidateOf<LocalDate> validate = decoder.decode("db.user", new LeafNode(date), TypeCapture.of(String.class),
            new DecoderContext(decoderService, null) );
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(localDate, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeFormatNull() throws GestaltException {
        LocalDateDecoder decoder = new LocalDateDecoder(null);

        String date = "2021-01-10";
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);

        ValidateOf<LocalDate> validate = decoder.decode("db.user", new LeafNode(date), TypeCapture.of(String.class),
            new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(localDate, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeFormatter() throws GestaltException {
        LocalDateDecoder decoder = new LocalDateDecoder("yyyy-MM-dd");

        String date = "2021-01-10";
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);

        ValidateOf<LocalDate> validate = decoder.decode("db.user", new LeafNode(date), TypeCapture.of(String.class),
            new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(localDate, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeBadDate() throws GestaltException {
        LocalDateDecoder decoder = new LocalDateDecoder();

        String now = "not a date";

        ValidateOf<LocalDate> validate = decoder.decode("db.user", new LeafNode(now), TypeCapture.of(String.class),
            new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a LocalDate on path: db.user, from node: LeafNode{value='not a date'}",
            validate.getErrors().get(0).description());
    }

    @Test
    void invalidLeafNode() throws GestaltException {
        LocalDateDecoder decoder = new LocalDateDecoder();

        ValidateOf<LocalDate> validate = decoder.decode("db.user", new LeafNode(null), TypeCapture.of(String.class),
            new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode LocalDate",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        LocalDateDecoder decoder = new LocalDateDecoder();

        ValidateOf<LocalDate> validate = decoder.decode("db.user", new MapNode(new HashMap<>()), TypeCapture.of(String.class),
            new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode LocalDate",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() throws GestaltException {
        LocalDateDecoder decoder = new LocalDateDecoder();

        ValidateOf<LocalDate> validate = decoder.decode("db.user", null, TypeCapture.of(String.class),
            new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode LocalDate",
            validate.getErrors().get(0).description());
    }
}
