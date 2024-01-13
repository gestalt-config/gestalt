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
import java.util.Optional;


/**
 * Test for a generic Optional.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
class OptionalDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new OptionalDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void priority() {
        OptionalDecoder optDecoder = new OptionalDecoder();
        Assertions.assertEquals(Priority.MEDIUM, optDecoder.priority());
    }

    @Test
    void name() {
        OptionalDecoder optDecoder = new OptionalDecoder();
        Assertions.assertEquals("Optional", optDecoder.name());
    }

    @Test
    void canDecode() {
        OptionalDecoder decoder = new OptionalDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Optional<Short>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Optional<List<Short>>>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Integer>>() {
        }));

    }

    @Test
    void decodeLeafInteger() {
        OptionalDecoder decoder = new OptionalDecoder();

        GResultOf<Optional<?>> result = decoder.decode("db.port", Tags.of(), new LeafNode("124"), new TypeCapture<Optional<Integer>>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.results().isPresent());
        Assertions.assertEquals(124, result.results().get());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeLeafIntegerEmpty() {
        OptionalDecoder decoder = new OptionalDecoder();

        GResultOf<Optional<?>> result = decoder.decode("db.port", Tags.of(), new LeafNode(null), new TypeCapture<Optional<Integer>>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.results().isPresent());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.port, has no value attempting to decode Integer",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeLeafIntegerNull() {
        OptionalDecoder decoder = new OptionalDecoder();

        GResultOf<Optional<?>> result = decoder.decode("db.port", Tags.of(), null, new TypeCapture<Optional<Integer>>() {
        }, new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.results().isPresent());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.port, received node type: null, attempting to decode Integer",
            result.getErrors().get(0).description());
    }
}
