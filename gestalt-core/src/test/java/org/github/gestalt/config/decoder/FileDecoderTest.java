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
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

class FileDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new FileDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
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
    void canDecode() {
        FileDecoder decoder = new FileDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(File.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
    }

    @Test
    void decode() {
        FileDecoder decoder = new FileDecoder();

        URL defaultFileURL = GestaltIntegrationTests.class.getClassLoader().getResource("default.properties");
        File defaultFile = new File(defaultFileURL.getFile());
        GResultOf<File> result = decoder.decode("db.user", Tags.of(), new LeafNode(defaultFile.getAbsolutePath()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(defaultFile.getAbsolutePath(), result.results().getAbsolutePath());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void invalidLeafNode() {
        FileDecoder stringDecoder = new FileDecoder();

        GResultOf<File> result = stringDecoder.decode("db.user", Tags.of(), new LeafNode(null),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode File",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() {
        FileDecoder stringDecoder = new FileDecoder();

        GResultOf<File> result = stringDecoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode File",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeNullNode() {
        FileDecoder stringDecoder = new FileDecoder();

        GResultOf<File> result = stringDecoder.decode("db.user", Tags.of(), null,
            TypeCapture.of(String.class), new DecoderContext(decoderService, null, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode File",
            result.getErrors().get(0).description());
    }
}
