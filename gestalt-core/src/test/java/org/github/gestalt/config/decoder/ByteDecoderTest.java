package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

class ByteDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);

        decoderService = new DecoderRegistry(Collections.singletonList(new ByteDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        ByteDecoder decoder = new ByteDecoder();
        Assertions.assertEquals("Byte", decoder.name());
    }

    @Test
    void priority() {
        ByteDecoder decoder = new ByteDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        ByteDecoder decoder = new ByteDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Byte.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Byte>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(byte.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Character.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Float.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Float>>() {
        }));
    }

    @Test
    void decodeNumericByte() {
        ByteDecoder decoder = new ByteDecoder();

        for (String value : List.of("5", "12", "-5", "127", "-128")) {
            GResultOf<Byte> result = decoder.decode("db.port", Tags.of(), new LeafNode(value),
                TypeCapture.of(Byte.class), new DecoderContext(decoderService, null, null, new PathLexer()));
            Assertions.assertTrue(result.hasResults(), value);
            Assertions.assertFalse(result.hasErrors(), value);
            Assertions.assertEquals(Byte.parseByte(value), result.results(), value);
        }
    }

    @Test
    void byteOutsideRange() {
        ByteDecoder decoder = new ByteDecoder();

        for (String value : List.of("128", "-129")) {
            GResultOf<Byte> result = decoder.decode("db.port", Tags.of(), new LeafNode(value),
                TypeCapture.of(Byte.class), new DecoderContext(decoderService, null, null, new PathLexer()));
            Assertions.assertFalse(result.hasResults(), value);
            Assertions.assertTrue(result.hasErrors(), value);
            Assertions.assertNull(result.results(), value);
            Assertions.assertNotNull(result.getErrors(), value);
            Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level(), value);
            Assertions.assertEquals("Unable to decode a number on path: db.port, from node: " +
                    "LeafNode{value='" + value + "'} attempting to decode Byte",
                result.getErrors().get(0).description(), value);
        }
    }

    @Test
    void nonNumericByte() {
        ByteDecoder decoder = new ByteDecoder();

        GResultOf<Byte> result = decoder.decode("db.port", Tags.of(), new LeafNode("a"),
            TypeCapture.of(Byte.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='a'} " +
                "attempting to decode Byte",
            result.getErrors().get(0).description());
    }

    @Test
    void emptyStringWithConfigEnabled() {
        ByteDecoder decoder = new ByteDecoder();
        GestaltConfig config = new GestaltConfig();
        config.setTreatEmptyStringAsAbsent(true);

        GResultOf<Byte> result = decoder.decode("db.port", Tags.of(), new LeafNode(""),
            TypeCapture.of(Byte.class), new DecoderContext(decoderService, null, null, new PathLexer(), config));

        Assertions.assertFalse(result.hasResults());
        Assertions.assertEquals(0, result.getErrors().size());
        Assertions.assertNull(result.results());
    }
}
