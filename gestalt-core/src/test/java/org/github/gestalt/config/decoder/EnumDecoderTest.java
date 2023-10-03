package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.Colours;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

class EnumDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new EnumDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        EnumDecoder decoder = new EnumDecoder();

        Assertions.assertEquals("Enum", decoder.name());
    }

    @Test
    void priority() {
        EnumDecoder decoder = new EnumDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
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
    void leafDecode() {

        EnumDecoder decoder = new EnumDecoder();

        ValidateOf<Colours> validate = decoder.decode("db.port", Tags.of(), new LeafNode("RED"),
                TypeCapture.of(Colours.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(Colours.RED, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void leafDecodeNotValidEnum() {

        EnumDecoder decoder = new EnumDecoder();

        ValidateOf<Colours> validate = decoder.decode("db.port", Tags.of(), new LeafNode("pink"),
                TypeCapture.of(Colours.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("ENUM org.github.gestalt.config.test.classes.Colours " +
                "could not be created with value pink for Path: db.port",
            validate.getErrors().get(0).description());
    }

    @Test
    void leafDecodeNotAnEnum() {

        EnumDecoder decoder = new EnumDecoder();

        ValidateOf<Colours> validate = decoder.decode("db.port", Tags.of(), new LeafNode("pink"),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Exception on Path: db.port, decoding enum: java.lang.String " +
                "could not be created with value pink exception was: java.lang.String.name()",
            validate.getErrors().get(0).description());
    }
}
