package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
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
import java.util.HashMap;
import java.util.List;

class StringAndLeafDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new StringDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        StringDecoder decoder = new StringDecoder();
        Assertions.assertEquals("String", decoder.name());
    }

    @Test
    void priority() {
        StringDecoder decoder = new StringDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        StringDecoder stringDecoder = new StringDecoder();

        Assertions.assertTrue(stringDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(stringDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(stringDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
    }

    @Test
    void decode() {
        StringDecoder stringDecoder = new StringDecoder();

        GResultOf<String> result = stringDecoder.decode("db.user", Tags.of(), new LeafNode("test"),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("test", result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void invalidLeafNode() {
        StringDecoder stringDecoder = new StringDecoder();

        GResultOf<String> result = stringDecoder.decode("db.user", Tags.of(), new LeafNode(null),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode String",
            result.getErrors().get(0).description());
    }

    @Test
    void nullLeafNode() {
        StringDecoder stringDecoder = new StringDecoder();

        GResultOf<String> result = stringDecoder.decode("db.user", Tags.of(), null,
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode String",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() {
        StringDecoder stringDecoder = new StringDecoder();

        GResultOf<String> result = stringDecoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode String",
            result.getErrors().get(0).description());
    }
}
