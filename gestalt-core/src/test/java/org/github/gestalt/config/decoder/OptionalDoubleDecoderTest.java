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
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Tests for an OptionalDouble.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
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
    void canDecode() {
        OptionalDoubleDecoder decoder = new OptionalDoubleDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<OptionalDouble>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(OptionalDouble.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Integer>>() {
        }));

    }

    @Test
    void decodeLeafDouble() {
        OptionalDoubleDecoder decoder = new OptionalDoubleDecoder();

        GResultOf<OptionalDouble> result = decoder.decode("db.port", Tags.of(), new LeafNode("124"), new TypeCapture<OptionalDouble>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.results().isPresent());
        Assertions.assertEquals(124D, result.results().getAsDouble());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeLeafDoubleEmpty() {
        OptionalDoubleDecoder decoder = new OptionalDoubleDecoder();

        GResultOf<OptionalDouble> result = decoder.decode("db.port", Tags.of(), new LeafNode(null), new TypeCapture<OptionalDouble>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.results().isPresent());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding OptionalDouble on path: db.port, with node: " +
                "LeafNode{value='null'}",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeLeafDoubleNull() {
        OptionalDoubleDecoder decoder = new OptionalDoubleDecoder();

        GResultOf<OptionalDouble> result = decoder.decode("db.port", Tags.of(), null,
            TypeCapture.of(OptionalDouble.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.results().isPresent());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding OptionalDouble on path: db.port",
            result.getErrors().get(0).description());
    }

    @Test
    void notADouble() {
        OptionalDoubleDecoder doubleDecoder = new OptionalDoubleDecoder();

        GResultOf<OptionalDouble> result = doubleDecoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
            TypeCapture.of(OptionalDouble.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals(OptionalDouble.empty(), result.results());

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode Double",
            result.getErrors().get(0).description());
    }
}
