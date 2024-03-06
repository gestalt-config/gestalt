package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class DecoderContextTest {

    @Test
    void equals() {
        Gestalt gestalt = Mockito.mock();
        Gestalt gestalt2 = Mockito.mock();
        DecoderService  decoderService  = Mockito.mock();
        DecoderService  decoderService2  = Mockito.mock();
        DecoderContext decoderContext = new DecoderContext(decoderService, gestalt);
        DecoderContext decoderContext1 = new DecoderContext(decoderService, gestalt);
        DecoderContext decoderContext2 = new DecoderContext(decoderService2, gestalt2);
        DecoderContext decoderContext3 = new DecoderContext(decoderService, gestalt2);
        DecoderContext decoderContext4 = new DecoderContext(decoderService2, gestalt);

        Assertions.assertEquals(decoderContext, decoderContext);
        Assertions.assertEquals(decoderContext, decoderContext1);
        Assertions.assertNotEquals(decoderContext, 0);
        Assertions.assertNotEquals(decoderContext, decoderContext3);
        Assertions.assertNotEquals(decoderContext, decoderContext4);
    }

    @Test
    void hashCodeTest() {
        Gestalt gestalt = Mockito.mock();
        DecoderService decoderService = Mockito.mock();
        DecoderContext decoderContext = new DecoderContext(decoderService, gestalt);
        Assertions.assertTrue(decoderContext.hashCode() != 0);
    }
}

