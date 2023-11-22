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
import java.util.OptionalDouble;

/**
 * Tests for an OptionalDouble.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
class OptionalDoubleDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new OptionalDoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder(), new ObjectDecoder(), new DoubleDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void priority() {
        OptionalDoubleDecoder optDecoder = new OptionalDoubleDecoder();
        Assertions.assertEquals(Priority.MEDIUM, optDecoder.priority());
    }

    @Test
    void name() {
        OptionalDoubleDecoder optDecoder = new OptionalDoubleDecoder();
        Assertions.assertEquals("OptionalDouble", optDecoder.name());
    }

    @Test
    void matches() {
        OptionalDoubleDecoder decoder = new OptionalDoubleDecoder();

        Assertions.assertTrue(decoder.matches(new TypeCapture<OptionalDouble>() {
        }));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(OptionalDouble.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Integer>>() {
        }));

    }

    @Test
    void decodeLeafDouble() {
        OptionalDoubleDecoder decoder = new OptionalDoubleDecoder();

        ValidateOf<OptionalDouble> validate = decoder.decode("db.port", Tags.of(), new LeafNode("124"), new TypeCapture<OptionalDouble>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertTrue(validate.results().isPresent());
        Assertions.assertEquals(124D, validate.results().getAsDouble());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeLeafDoubleEmpty() {
        OptionalDoubleDecoder decoder = new OptionalDoubleDecoder();

        ValidateOf<OptionalDouble> validate = decoder.decode("db.port", Tags.of(), new LeafNode(null), new TypeCapture<OptionalDouble>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertFalse(validate.results().isPresent());
        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.port, has no value attempting to decode Double",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeLeafDoubleNull() {
        OptionalDoubleDecoder decoder = new OptionalDoubleDecoder();

        ValidateOf<OptionalDouble> validate = decoder.decode("db.port", Tags.of(), null,
                TypeCapture.of(OptionalDouble.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertFalse(validate.results().isPresent());
        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.port, received node type: null, attempting to decode Double",
            validate.getErrors().get(0).description());
    }
}
