package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.integration.GestaltIntegrationTests;
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

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

class PathDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new PathDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        PathDecoder decoder = new PathDecoder();
        Assertions.assertEquals("Path", decoder.name());
    }

    @Test
    void priority() {
        PathDecoder decoder = new PathDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        PathDecoder decoder = new PathDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Path.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
    }

    @Test
    void decode() {
        PathDecoder decoder = new PathDecoder();

        URL defaultFileURL = GestaltIntegrationTests.class.getClassLoader().getResource("default.properties");
        File defaultFile = new File(defaultFileURL.getFile());
        GResultOf<Path> result = decoder.decode("db.user", Tags.of(), new LeafNode(defaultFile.getAbsolutePath()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(defaultFile.getAbsolutePath(), result.results().toString());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void invalidLeafNode() {
        PathDecoder stringDecoder = new PathDecoder();

        GResultOf<Path> result = stringDecoder.decode("db.user", Tags.of(), new LeafNode(null),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode Path",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() {
        PathDecoder stringDecoder = new PathDecoder();

        GResultOf<Path> result = stringDecoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode Path",
            result.getErrors().get(0).description());
    }

    @Test
    void emptyStringWithConfigEnabled() {
        PathDecoder decoder = new PathDecoder();
        GestaltConfig config = new GestaltConfig();
        config.setTreatEmptyStringAsAbsent(true);

        GResultOf<java.nio.file.Path> result = decoder.decode("db.path", Tags.of(), new LeafNode(""),
            TypeCapture.of(java.nio.file.Path.class), new DecoderContext(decoderService, null, null, new PathLexer(), config));

        Assertions.assertFalse(result.hasResults());
        Assertions.assertEquals(0, result.getErrors().size());
        Assertions.assertNull(result.results());
    }
}
