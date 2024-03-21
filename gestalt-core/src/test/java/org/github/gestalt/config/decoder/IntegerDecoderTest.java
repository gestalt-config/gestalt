package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
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

class IntegerDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new IntegerDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        IntegerDecoder decoder = new IntegerDecoder();
        Assertions.assertEquals("Integer", decoder.name());
    }

    @Test
    void priority() {
        IntegerDecoder decoder = new IntegerDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        IntegerDecoder integerDecoder = new IntegerDecoder();

        Assertions.assertTrue(integerDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertTrue(integerDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Integer>() {
        }));
        Assertions.assertTrue(integerDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(int.class)));

        Assertions.assertFalse(integerDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(integerDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(integerDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Integer>>() {
        }));
    }

    @Test
    void decode() {
        IntegerDecoder integerDecoder = new IntegerDecoder();

        GResultOf<Integer> result = integerDecoder.decode("db.port", Tags.of(), new LeafNode("124"),
            TypeCapture.of(Integer.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(124, result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void notAnInteger() {
        IntegerDecoder integerDecoder = new IntegerDecoder();

        GResultOf<Integer> result = integerDecoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
            TypeCapture.of(Integer.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode Integer",
            result.getErrors().get(0).description());
    }

    @Test
    void notAIntegerTooLarge() {
        IntegerDecoder decoder = new IntegerDecoder();

        GResultOf<Integer> result = decoder.decode("db.port", Tags.of(),
            new LeafNode("12345678901234567890123456789012345678901234567890123456789"), TypeCapture.of(Integer.class),
            new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a number on path: db.port, from node: " +
                "LeafNode{value='12345678901234567890123456789012345678901234567890123456789'} attempting to decode Integer",
            result.getErrors().get(0).description());
    }
}
