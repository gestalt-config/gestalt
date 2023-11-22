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
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.OptionalLong;

/**
 * Tests for an optionalLong.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
class OptionalLongDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new OptionalLongDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder(), new ObjectDecoder(), new DoubleDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void priority() {
        OptionalLongDecoder optDecoder = new OptionalLongDecoder();
        Assertions.assertEquals(Priority.MEDIUM, optDecoder.priority());
    }

    @Test
    void name() {
        OptionalLongDecoder optDecoder = new OptionalLongDecoder();
        Assertions.assertEquals("OptionalLong", optDecoder.name());
    }

    @Test
    void matches() {
        OptionalLongDecoder decoder = new OptionalLongDecoder();

        Assertions.assertTrue(decoder.matches(new TypeCapture<OptionalLong>() {
        }));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(OptionalLong.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Integer>>() {
        }));

    }

    @Test
    void decodeLeafLong() {
        OptionalLongDecoder decoder = new OptionalLongDecoder();

        ValidateOf<OptionalLong> validate = decoder.decode("db.port", Tags.of(), new LeafNode("124"), new TypeCapture<OptionalLong>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertTrue(validate.results().isPresent());
        Assertions.assertEquals(124L, validate.results().getAsLong());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeLeafLongEmpty() {
        OptionalLongDecoder decoder = new OptionalLongDecoder();

        ValidateOf<OptionalLong> validate = decoder.decode("db.port", Tags.of(), new LeafNode(null), new TypeCapture<OptionalLong>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertFalse(validate.results().isPresent());
        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.port, has no value attempting to decode Long",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeLeafLongNull() {
        OptionalLongDecoder decoder = new OptionalLongDecoder();

        ValidateOf<OptionalLong> validate = decoder.decode("db.port", Tags.of(), null,
                TypeCapture.of(OptionalLong.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertFalse(validate.results().isPresent());
        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.port, received node type: null, attempting to decode Long",
            validate.getErrors().get(0).description());
    }
}
