package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DecoderContextTest {

    @Test
    void equals() {
        var lexer1 = new PathLexer();
        Gestalt gestalt = Mockito.mock();
        Gestalt gestalt2 = Mockito.mock();
        DecoderService  decoderService  = Mockito.mock();
        DecoderService  decoderService2  = Mockito.mock();
        SecretConcealer secretConcealer = Mockito.mock();
        SecretConcealer secretConcealer2 = Mockito.mock();
        DecoderContext decoderContext = new DecoderContext(decoderService, gestalt, secretConcealer, lexer1);
        DecoderContext decoderContext1 = new DecoderContext(decoderService, gestalt, secretConcealer, lexer1);
        DecoderContext decoderContext3 = new DecoderContext(decoderService, gestalt2, secretConcealer2, lexer1);
        DecoderContext decoderContext4 = new DecoderContext(decoderService2, gestalt, secretConcealer2, lexer1);
        DecoderContext decoderContext5 = new DecoderContext(decoderService2, gestalt, secretConcealer2, new PathLexer("_"));

        Assertions.assertEquals(decoderContext, decoderContext);
        Assertions.assertEquals(decoderContext, decoderContext1);
        Assertions.assertNotEquals(decoderContext, 0);
        Assertions.assertNotEquals(decoderContext, decoderContext3);
        Assertions.assertNotEquals(decoderContext, decoderContext4);
        Assertions.assertNotEquals(decoderContext4, decoderContext5);
    }

    @Test
    void hashCodeTest() {
        Gestalt gestalt = Mockito.mock();
        DecoderService decoderService = Mockito.mock();
        DecoderContext decoderContext = new DecoderContext(decoderService, gestalt, null, new PathLexer());
        Assertions.assertTrue(decoderContext.hashCode() != 0);
    }
}

