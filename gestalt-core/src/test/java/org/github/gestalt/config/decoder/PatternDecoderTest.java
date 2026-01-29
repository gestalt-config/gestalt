package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
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
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

class PatternDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new PatternDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        PatternDecoder decoder = new PatternDecoder();
        Assertions.assertEquals("Pattern", decoder.name());
    }

    @Test
    void priority() {
        PatternDecoder decoder = new PatternDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        PatternDecoder decoder = new PatternDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Pattern.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
    }

    @Test
    void decode() {
        PatternDecoder decoder = new PatternDecoder();

        GResultOf<Pattern> result = decoder.decode("db.user", Tags.of(), new LeafNode("test"),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertTrue(result.results().matcher("test").find());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void invalidLeafNode() {
        PatternDecoder stringDecoder = new PatternDecoder();

        GResultOf<Pattern> result = stringDecoder.decode("db.user", Tags.of(), new LeafNode(null),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode Pattern",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() {
        PatternDecoder stringDecoder = new PatternDecoder();

        GResultOf<Pattern> result = stringDecoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode Pattern",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() {
        PatternDecoder stringDecoder = new PatternDecoder();

        GResultOf<Pattern> result = stringDecoder.decode("db.user", Tags.of(), null,
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode Pattern",
            result.getErrors().get(0).description());
    }

    @Test
    void emptyStringWithConfigEnabled() {
        PatternDecoder decoder = new PatternDecoder();
        GestaltConfig config = new GestaltConfig();
        config.setTreatEmptyStringAsAbsent(true);

        GResultOf<java.util.regex.Pattern> result = decoder.decode("db.pattern", Tags.of(), new LeafNode(""),
            TypeCapture.of(java.util.regex.Pattern.class), new DecoderContext(decoderService, null, null, new PathLexer(), config));

        Assertions.assertFalse(result.hasResults());
        Assertions.assertEquals(0, result.getErrors().size());
        Assertions.assertNull(result.results());
    }
}
