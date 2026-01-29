package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Date;
import java.util.List;

class ShortDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new ShortDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        ShortDecoder decoder = new ShortDecoder();
        Assertions.assertEquals("Short", decoder.name());
    }

    @Test
    void priority() {
        ShortDecoder decoder = new ShortDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        ShortDecoder decoder = new ShortDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Short.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Short>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(short.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Integer>>() {
        }));
    }

    @Test
    void decode() {
        ShortDecoder decoder = new ShortDecoder();

        GResultOf<Short> result = decoder.decode("db.port", Tags.of(), new LeafNode("124"),
            TypeCapture.of(Short.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals((short) 124, (short) result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void notAnInteger() {
        ShortDecoder decoder = new ShortDecoder();

        GResultOf<Short> result = decoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
            TypeCapture.of(Short.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode Short",
            result.getErrors().get(0).description());
    }

    @Test
    void notAShortTooLarge() {
        ShortDecoder decoder = new ShortDecoder();

        GResultOf<Short> result = decoder.decode("db.port", Tags.of(),
            new LeafNode("12345678901234567890123456789012345678901234567890123456789"), TypeCapture.of(Short.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a number on path: db.port, from node: " +
                "LeafNode{value='12345678901234567890123456789012345678901234567890123456789'} attempting to decode Short",
            result.getErrors().get(0).description());
    }

    @Test
    void emptyStringWithConfigEnabled() {
        ShortDecoder decoder = new ShortDecoder();
        GestaltConfig config = new GestaltConfig();
        config.setTreatEmptyStringAsAbsent(true);

        GResultOf<Short> result = decoder.decode("db.port", Tags.of(), new LeafNode(""),
            TypeCapture.of(Short.class), new DecoderContext(decoderService, null, null, new PathLexer(), config));

        Assertions.assertFalse(result.hasResults());
        // Filter out MISSING_OPTIONAL_VALUE level errors - these are informational, not real errors
        Assertions.assertTrue(result.getErrorsNotLevel(org.github.gestalt.config.entity.ValidationLevel.MISSING_OPTIONAL_VALUE).isEmpty());
        Assertions.assertNull(result.results());
    }
}
