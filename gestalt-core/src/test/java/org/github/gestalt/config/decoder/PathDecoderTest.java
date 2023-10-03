package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.integration.GestaltIntegrationTests;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;
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
    void matches() {
        PathDecoder decoder = new PathDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(Path.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
    }

    @Test
    void decode() {
        PathDecoder decoder = new PathDecoder();

        URL defaultFileURL = GestaltIntegrationTests.class.getClassLoader().getResource("default.properties");
        File defaultFile = new File(defaultFileURL.getFile());
        ValidateOf<Path> validate = decoder.decode("db.user", Tags.of(), new LeafNode(defaultFile.getAbsolutePath()),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(defaultFile.getAbsolutePath(), validate.results().toString());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void invalidLeafNode() {
        PathDecoder stringDecoder = new PathDecoder();

        ValidateOf<Path> validate = stringDecoder.decode("db.user", Tags.of(), new LeafNode(null),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode Path",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() {
        PathDecoder stringDecoder = new PathDecoder();

        ValidateOf<Path> validate = stringDecoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
                TypeCapture.of(String.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode Path",
            validate.getErrors().get(0).description());
    }
}
