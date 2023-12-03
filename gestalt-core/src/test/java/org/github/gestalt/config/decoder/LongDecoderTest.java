package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Date;
import java.util.List;

class LongDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new LongDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        LongDecoder decoder = new LongDecoder();
        Assertions.assertEquals("Long", decoder.name());
    }

    @Test
    void priority() {
        LongDecoder decoder = new LongDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        LongDecoder longDecoder = new LongDecoder();

        Assertions.assertTrue(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Long.class)));
        Assertions.assertTrue(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Long>() {
        }));
        Assertions.assertTrue(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(long.class)));

        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Long>>() {
        }));
    }

    @Test
    void decode() {
        LongDecoder longDecoder = new LongDecoder();

        ValidateOf<Long> validate = longDecoder.decode("db.port", Tags.of(), new LeafNode("124"),
                TypeCapture.of(Long.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124L, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notALong() {
        LongDecoder longDecoder = new LongDecoder();

        ValidateOf<Long> validate = longDecoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
                TypeCapture.of(Long.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: " +
                "LeafNode{value='12s4'} attempting to decode Long",
            validate.getErrors().get(0).description());
    }

    @Test
    void notALongTooLarge() {
        LongDecoder decoder = new LongDecoder();

        ValidateOf<Long> validate = decoder.decode("db.port", Tags.of(),
                new LeafNode("12345678901234567890123456789012345678901234567890123456"), TypeCapture.of(Long.class),
            new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a number on path: db.port, from node: " +
                "LeafNode{value='12345678901234567890123456789012345678901234567890123456'} attempting to decode Long",
            validate.getErrors().get(0).description());
    }
}
