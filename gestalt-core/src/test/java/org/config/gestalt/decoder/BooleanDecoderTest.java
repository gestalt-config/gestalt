package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

class BooleanDecoderTest {

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

        ValidateOf<Boolean> validate = decoder.decode("db.enabled", new LeafNode("true"), TypeCapture.of(Integer.class),
            new DecoderRegistry(Collections.singletonList(decoder)));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertTrue(validate.results());
    }

    @Test
    void decodeFalse() throws GestaltException {
        BooleanDecoder decoder = new BooleanDecoder();

        ValidateOf<Boolean> validate = decoder.decode("db.enabled", new LeafNode("false"), TypeCapture.of(Integer.class),
            new DecoderRegistry(Collections.singletonList(decoder)));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertFalse(validate.results());
    }

    @Test
    void decodeFalseNull() throws GestaltException {
        BooleanDecoder decoder = new BooleanDecoder();

        ValidateOf<Boolean> validate = decoder.decode("db.enabled", new LeafNode(null), TypeCapture.of(Integer.class),
            new DecoderRegistry(Collections.singletonList(decoder)));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.enabled, missing value, LeafNode{value='null'} attempting to decode Boolean",
            validate.getErrors().get(0).description());
    }
}
