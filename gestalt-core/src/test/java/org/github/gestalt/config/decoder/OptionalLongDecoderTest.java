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
import java.util.OptionalLong;

/**
 * Tests for an optionalLong.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
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
    void canDecode() {
        OptionalLongDecoder decoder = new OptionalLongDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<OptionalLong>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(OptionalLong.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Integer>>() {
        }));

    }

    @Test
    void decodeLeafLong() {
        OptionalLongDecoder decoder = new OptionalLongDecoder();

        GResultOf<OptionalLong> result = decoder.decode("db.port", Tags.of(), new LeafNode("124"), new TypeCapture<OptionalLong>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.results().isPresent());
        Assertions.assertEquals(124L, result.results().getAsLong());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeLeafLongEmpty() {
        OptionalLongDecoder decoder = new OptionalLongDecoder();

        GResultOf<OptionalLong> result = decoder.decode("db.port", Tags.of(), new LeafNode(null), new TypeCapture<OptionalLong>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.results().isPresent());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding OptionalLong on path: db.port",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeLeafLongNull() {
        OptionalLongDecoder decoder = new OptionalLongDecoder();

        GResultOf<OptionalLong> result = decoder.decode("db.port", Tags.of(), null,
            TypeCapture.of(OptionalLong.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.results().isPresent());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding OptionalLong on path: db.port",
            result.getErrors().get(0).description());
    }

    @Test
    void notALong() {
        OptionalLongDecoder longDecoder = new OptionalLongDecoder();

        GResultOf<OptionalLong> result = longDecoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
            TypeCapture.of(OptionalLong.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals(OptionalLong.empty(), result.results());

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: " +
                "LeafNode{value='12s4'} attempting to decode Long",
            result.getErrors().get(0).description());
    }
}
