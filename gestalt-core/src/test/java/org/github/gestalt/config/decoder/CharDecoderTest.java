package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;
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
    void matches() {
        CharDecoder decoder = new CharDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(Character.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<Character>() {
        }));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(char.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(Float.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Float>>() {
        }));
    }

    @Test
    void decodeChar() throws GestaltException {
        CharDecoder decoder = new CharDecoder();

        ValidateOf<Character> validate = decoder.decode("db.port", new LeafNode("a"), TypeCapture.of(Character.class),
            new DecoderContext(decoderService, null) );
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals('a', validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notACharTooLong() throws GestaltException {
        CharDecoder decoder = new CharDecoder();

        ValidateOf<Character> validate = decoder.decode("db.port", new LeafNode("aaa"), TypeCapture.of(Character.class),
            new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals('a', validate.results());

        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.WARN, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a char on path: db.port, decoding node: LeafNode{value='aaa'} received the wrong size",
            validate.getErrors().get(0).description());
    }

    @Test
    void notACharTooShort() throws GestaltException {
        CharDecoder decoder = new CharDecoder();

        ValidateOf<Character> validate = decoder.decode("db.port", new LeafNode(""), TypeCapture.of(Character.class),
            new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals("Expected a char on path: db.port, decoding node: LeafNode{value=''} received the wrong size",
            validate.getErrors().get(0).description());
    }
}
