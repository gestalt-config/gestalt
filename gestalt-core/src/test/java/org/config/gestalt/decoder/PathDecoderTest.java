package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.integration.GestaltIntegrationTests;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.node.ConfigNodeService;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.node.MapNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
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

class PathDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
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
    void decode() throws GestaltException {
        PathDecoder decoder = new PathDecoder();

        URL defaultFileURL = GestaltIntegrationTests.class.getClassLoader().getResource("default.properties");
        File defaultFile = new File(defaultFileURL.getFile());
        ValidateOf<Path> validate = decoder.decode("db.user", new LeafNode(defaultFile.getAbsolutePath()), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(defaultFile.getAbsolutePath(), validate.results().toString());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void invalidLeafNode() throws GestaltException {
        PathDecoder stringDecoder = new PathDecoder();

        ValidateOf<Path> validate = stringDecoder.decode("db.user", new LeafNode(null), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, missing value, LeafNode{value='null'} attempting to decode Path",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        PathDecoder stringDecoder = new PathDecoder();

        ValidateOf<Path> validate = stringDecoder.decode("db.user", new MapNode(new HashMap<>()), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type, received: MapNode{mapNode={}} " +
                "attempting to decode Path",
            validate.getErrors().get(0).description());
    }
}
