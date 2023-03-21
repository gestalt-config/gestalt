package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeManager;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author Colin Redmond (c) 2023.
 */
class OptionalIntDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry registry;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        registry = new DecoderRegistry(List.of(new OptionalIntDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new DoubleDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void priority() {
        OptionalIntDecoder optDecoder = new OptionalIntDecoder();
        Assertions.assertEquals(Priority.MEDIUM, optDecoder.priority());
    }

    @Test
    void name() {
        OptionalIntDecoder optDecoder = new OptionalIntDecoder();
        Assertions.assertEquals("OptionalInt", optDecoder.name());
    }

    @Test
    void matches() {
        OptionalIntDecoder decoder = new OptionalIntDecoder();

        Assertions.assertTrue(decoder.matches(new TypeCapture<OptionalInt>() {
        }));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(OptionalInt.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Integer>>() {
        }));

    }

    @Test
    void decodeLeafInt() {
        OptionalIntDecoder decoder = new OptionalIntDecoder();

        ValidateOf<OptionalInt> validate = decoder.decode("db.port", new LeafNode("124"), new TypeCapture<OptionalInt>() {
        }, registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertTrue(validate.results().isPresent());
        Assertions.assertEquals(124D, validate.results().getAsInt());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeLeafIntEmpty() {
        OptionalIntDecoder decoder = new OptionalIntDecoder();

        ValidateOf<OptionalInt> validate = decoder.decode("db.port", new LeafNode(null), new TypeCapture<OptionalInt>() {
        }, registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertFalse(validate.results().isPresent());
        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.port, has no value attempting to decode Integer",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeLeafIntNull() {
        OptionalIntDecoder decoder = new OptionalIntDecoder();

        ValidateOf<OptionalInt> validate = decoder.decode("db.port", null,TypeCapture.of(OptionalInt.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertFalse(validate.results().isPresent());
        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.port, received node type: null, attempting to decode Integer",
            validate.getErrors().get(0).description());
    }
}
