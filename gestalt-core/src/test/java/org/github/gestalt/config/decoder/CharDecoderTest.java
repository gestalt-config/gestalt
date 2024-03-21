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
import java.util.List;

class CharDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new CharDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        CharDecoder decoder = new CharDecoder();
        Assertions.assertEquals("Character", decoder.name());
    }

    @Test
    void priority() {
        CharDecoder decoder = new CharDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        CharDecoder decoder = new CharDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Character.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Character>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(char.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Float.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Float>>() {
        }));
    }

    @Test
    void decodeChar() {
        CharDecoder decoder = new CharDecoder();

        GResultOf<Character> result = decoder.decode("db.port", Tags.of(), new LeafNode("a"),
            TypeCapture.of(Character.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals('a', result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void notACharTooLong() {
        CharDecoder decoder = new CharDecoder();

        GResultOf<Character> result = decoder.decode("db.port", Tags.of(), new LeafNode("aaa"),
            TypeCapture.of(Character.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals('a', result.results());

        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.WARN, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a char on path: db.port, decoding node: LeafNode{value='aaa'} received the wrong size",
            result.getErrors().get(0).description());
    }

    @Test
    void notACharTooShort() {
        CharDecoder decoder = new CharDecoder();

        GResultOf<Character> result = decoder.decode("db.port", Tags.of(), new LeafNode(""),
            TypeCapture.of(Character.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals("Expected a char on path: db.port, decoding node: LeafNode{value=''} received the wrong size",
            result.getErrors().get(0).description());
    }
}
