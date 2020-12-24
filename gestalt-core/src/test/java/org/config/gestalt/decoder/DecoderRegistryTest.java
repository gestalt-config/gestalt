package org.config.gestalt.decoder;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.reflect.TypeCapture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class DecoderRegistryTest {

    @Test
    void getDecoderForClass() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()));

        List<Decoder> decoders = decoderRegistry.getDecoderForClass(TypeCapture.of(String.class));

        Assertions.assertEquals(1, decoders.size());
        Assertions.assertTrue(decoders.get(0).matches(TypeCapture.of(String.class)));
    }

    @Test
    void getDecoderForClassNull() {
        try {
            new DecoderRegistry(null);
        } catch (GestaltException e) {
            Assertions.assertEquals("Decoder list was null", e.getMessage());
        }
    }

    @Test
    void getDecoderForClassDuplicate() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder(), new LongDecoder()));

        List<Decoder> decoders = decoderRegistry.getDecoderForClass(TypeCapture.of(Long.class));

        Assertions.assertEquals(2, decoders.size());
        Assertions.assertTrue(decoders.get(0).matches(TypeCapture.of(Long.class)));
        Assertions.assertTrue(decoders.get(1).matches(TypeCapture.of(Long.class)));
    }
}
