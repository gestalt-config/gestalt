package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
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
import java.util.Date;
import java.util.List;

class BooleanDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void name() {
        BooleanDecoder decoder = new BooleanDecoder();
        Assertions.assertEquals("Boolean", decoder.name());
    }

    @Test
    void priority() {
        BooleanDecoder decoder = new BooleanDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        BooleanDecoder decoder = new BooleanDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(Boolean.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<Boolean>() {
        }));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(boolean.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Integer>>() {
        }));
    }

    @Test
    void decode() throws GestaltException {
        BooleanDecoder decoder = new BooleanDecoder();

        ValidateOf<Boolean> validate = decoder.decode("db.enabled", new LeafNode("true"), TypeCapture.of(Boolean.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
                List.of(new StandardPathMapper())));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertTrue(validate.results());
    }

    @Test
    void decodeFalse() throws GestaltException {
        BooleanDecoder decoder = new BooleanDecoder();

        ValidateOf<Boolean> validate = decoder.decode("db.enabled", new LeafNode("false"), TypeCapture.of(Boolean.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
                List.of(new StandardPathMapper())));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertFalse(validate.results());
    }

    @Test
    void decodeFalseNull() throws GestaltException {
        BooleanDecoder decoder = new BooleanDecoder();

        ValidateOf<Boolean> validate = decoder.decode("db.enabled", new LeafNode(null), TypeCapture.of(Boolean.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer,
                List.of(new StandardPathMapper())));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.enabled, has no value attempting to decode Boolean",
            validate.getErrors().get(0).description());
    }
}
