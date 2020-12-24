package org.config.gestalt.decoder;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.test.classes.Colours;
import org.config.gestalt.test.classes.DBInfo;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class EnumDecoderTest {

    @Test
    void name() {
        EnumDecoder decoder = new EnumDecoder();

        Assertions.assertEquals("Enum", decoder.name());
    }

    @Test
    void matches() {
        EnumDecoder decoder = new EnumDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(Colours.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<Colours>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfo.class)));
    }

    @Test
    void leafDecode() throws GestaltException {

        EnumDecoder decoder = new EnumDecoder();

        ValidateOf<Colours> validate = decoder.decode("db.port", new LeafNode("RED"), TypeCapture.of(Colours.class),
            new DecoderRegistry(Collections.singletonList(decoder)));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(Colours.RED, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void leafDecodeNotValidEnum() throws GestaltException {

        EnumDecoder decoder = new EnumDecoder();

        ValidateOf<Colours> validate = decoder.decode("db.port", new LeafNode("pink"), TypeCapture.of(Colours.class),
            new DecoderRegistry(Collections.singletonList(decoder)));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("ENUM org.credmond.gestalt.test.classes.Colours could not be created with value pink for " +
                "Path: db.port",
            validate.getErrors().get(0).description());
    }
}
