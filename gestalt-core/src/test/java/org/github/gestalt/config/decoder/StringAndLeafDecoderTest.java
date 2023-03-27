package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;
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

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
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
    void matches() {
        StringDecoder stringDecoder = new StringDecoder();

        Assertions.assertTrue(stringDecoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(stringDecoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(stringDecoder.matches(TypeCapture.of(Date.class)));
    }

    @Test
    void decode() throws GestaltException {
        StringDecoder stringDecoder = new StringDecoder();

        ValidateOf<String> validate = stringDecoder.decode("db.user", new LeafNode("test"), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder), configNodeService, lexer,
                List.of(new StandardPathMapper())));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals("test", validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void invalidLeafNode() throws GestaltException {
        StringDecoder stringDecoder = new StringDecoder();

        ValidateOf<String> validate = stringDecoder.decode("db.user", new LeafNode(null), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder), configNodeService, lexer,
                List.of(new StandardPathMapper())));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode String",
            validate.getErrors().get(0).description());
    }

    @Test
    void nullLeafNode() throws GestaltException {
        StringDecoder stringDecoder = new StringDecoder();

        ValidateOf<String> validate = stringDecoder.decode("db.user", null, TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder), configNodeService, lexer,
                List.of(new StandardPathMapper())));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode String",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        StringDecoder stringDecoder = new StringDecoder();

        ValidateOf<String> validate = stringDecoder.decode("db.user", new MapNode(new HashMap<>()), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder), configNodeService, lexer,
                List.of(new StandardPathMapper())));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode String",
            validate.getErrors().get(0).description());
    }
}
