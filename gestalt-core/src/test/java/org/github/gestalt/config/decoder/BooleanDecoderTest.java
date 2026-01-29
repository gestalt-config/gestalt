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
import java.util.Date;
import java.util.List;

class BooleanDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new BooleanDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        BooleanDecoder decoder = new BooleanDecoder();
        Assertions.assertEquals("Boolean", decoder.name());
    }

    @Test
    void priority() {
        BooleanDecoder decoder = new BooleanDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        BooleanDecoder decoder = new BooleanDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Boolean.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Boolean>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(boolean.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Integer>>() {
        }));
    }

    @Test
    void decode() {
        BooleanDecoder decoder = new BooleanDecoder();

        GResultOf<Boolean> result = decoder.decode("db.enabled", Tags.of(), new LeafNode("true"),
            TypeCapture.of(Boolean.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.results());
    }

    @Test
    void decodeFalse() {
        BooleanDecoder decoder = new BooleanDecoder();

        GResultOf<Boolean> result = decoder.decode("db.enabled", Tags.of(), new LeafNode("false"),
            TypeCapture.of(Boolean.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertFalse(result.results());
    }

    @Test
    void decodeFalseNull() {
        BooleanDecoder decoder = new BooleanDecoder();

        GResultOf<Boolean> result = decoder.decode("db.enabled", Tags.of(), new LeafNode(null),
            TypeCapture.of(Boolean.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.enabled, has no value attempting to decode Boolean",
            result.getErrors().get(0).description());
    }

    @Test
    void emptyStringWithConfigEnabled() {
        BooleanDecoder decoder = new BooleanDecoder();
        GestaltConfig config = new GestaltConfig();
        config.setTreatEmptyStringAsAbsent(true);

        GResultOf<Boolean> result = decoder.decode("db.enabled", Tags.of(), new LeafNode(""),
            TypeCapture.of(Boolean.class), new DecoderContext(decoderService, null, null, new PathLexer(), config));

        Assertions.assertFalse(result.hasResults());
        // Filter out MISSING_OPTIONAL_VALUE level errors - these are informational, not real errors
        Assertions.assertTrue(result.getErrorsNotLevel(org.github.gestalt.config.entity.ValidationLevel.MISSING_OPTIONAL_VALUE).isEmpty());
        Assertions.assertNull(result.results());
    }
}
