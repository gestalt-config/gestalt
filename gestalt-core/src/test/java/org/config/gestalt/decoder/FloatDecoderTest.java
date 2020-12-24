package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class FloatDecoderTest {

    @Test
    void name() {
        FloatDecoder decoder = new FloatDecoder();
        Assertions.assertEquals("Float", decoder.name());
    }

    @Test
    void matches() {
        FloatDecoder floatDecoder = new FloatDecoder();

        Assertions.assertTrue(floatDecoder.matches(TypeCapture.of(Float.class)));
        Assertions.assertTrue(floatDecoder.matches(new TypeCapture<Float>() {
        }));
        Assertions.assertTrue(floatDecoder.matches(TypeCapture.of(float.class)));

        Assertions.assertFalse(floatDecoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(floatDecoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(floatDecoder.matches(new TypeCapture<List<Float>>() {
        }));
    }

    @Test
    void decodeFloat() throws GestaltException {
        FloatDecoder floatDecoder = new FloatDecoder();

        ValidateOf<Float> validate = floatDecoder.decode("db.timeout", new LeafNode("124.5"), TypeCapture.of(Float.class),
            new DecoderRegistry(Collections.singletonList(floatDecoder)));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124.5f, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeFloat2() throws GestaltException {
        FloatDecoder floatDecoder = new FloatDecoder();

        ValidateOf<Float> validate = floatDecoder.decode("db.timeout", new LeafNode("124"), TypeCapture.of(Float.class),
            new DecoderRegistry(Collections.singletonList(floatDecoder)));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notAFloat() throws GestaltException {
        FloatDecoder floatDecoder = new FloatDecoder();

        ValidateOf<Float> validate = floatDecoder.decode("db.timeout", new LeafNode("12s4"), TypeCapture.of(Float.class),
            new DecoderRegistry(Collections.singletonList(floatDecoder)));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.timeout, from node: LeafNode{value='12s4'} " +
                "attempting to decode Float",
            validate.getErrors().get(0).description());
    }
}
