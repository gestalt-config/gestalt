package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.integration.GestaltIntegrationTests;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

class FileDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void name() {
        FileDecoder decoder = new FileDecoder();
        Assertions.assertEquals("File", decoder.name());
    }

    @Test
    void priority() {
        FileDecoder decoder = new FileDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        FileDecoder decoder = new FileDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(File.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
    }

    @Test
    void decode() throws GestaltException {
        FileDecoder decoder = new FileDecoder();

        URL defaultFileURL = GestaltIntegrationTests.class.getClassLoader().getResource("default.properties");
        File defaultFile = new File(defaultFileURL.getFile());
        ValidateOf<File> validate = decoder.decode("db.user", new LeafNode(defaultFile.getAbsolutePath()), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Assertions.assertEquals(defaultFile.getAbsolutePath(), validate.results().getAbsolutePath());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void invalidLeafNode() throws GestaltException {
        FileDecoder stringDecoder = new FileDecoder();

        ValidateOf<File> validate = stringDecoder.decode("db.user", new LeafNode(null), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, missing value, LeafNode{value='null'} attempting to decode File",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() throws GestaltException {
        FileDecoder stringDecoder = new FileDecoder();

        ValidateOf<File> validate = stringDecoder.decode("db.user", new MapNode(new HashMap<>()), TypeCapture.of(String.class),
            new DecoderRegistry(Collections.singletonList(stringDecoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type, received: MapNode{mapNode={}} " +
                "attempting to decode File",
            validate.getErrors().get(0).description());
    }
}
