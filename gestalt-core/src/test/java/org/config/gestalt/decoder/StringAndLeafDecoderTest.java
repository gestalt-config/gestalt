package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.node.MapNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

class StringAndLeafDecoderTest {

    @Test
    void name() {
        StringDecoder decoder = new StringDecoder();
        Assertions.assertEquals("String", decoder.name());
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
            new DecoderRegistry(Collections.singletonList(stringDecoder)));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals("test", validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void invalidLeafNode() throws GestaltException {
        StringDecoder stringDecoder = new StringDecoder();

        ValidateOf<String> validate = stringDecoder.decode("db.user", new LeafNode(null), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder)));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, missing value, LeafNode{value='null'} attempting to decode String",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        StringDecoder stringDecoder = new StringDecoder();

        ValidateOf<String> validate = stringDecoder.decode("db.user", new MapNode(new HashMap<>()), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder)));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type, received: MapNode{mapNode={}} " +
                "attempting to decode String",
            validate.getErrors().get(0).description());
    }
}
