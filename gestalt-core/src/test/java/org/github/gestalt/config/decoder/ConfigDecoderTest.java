package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.entity.ConfigContainer;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ConfigDecoderTest {
    private final ConfigNodeService configNodeService = Mockito.mock();
    private final SentenceLexer lexer = Mockito.mock();
    private final DecoderRegistry decoderService = new DecoderRegistry(Collections.singletonList(new StringDecoder()), configNodeService,
        lexer, List.of(new StandardPathMapper()));

    private final Gestalt gestalt = Mockito.mock();

    ConfigDecoderTest() throws GestaltConfigurationException {
    }

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        Mockito.clearInvocations(configNodeService, lexer, gestalt);
    }


    @Test
    void name() {
        ConfigDecoder decoder = new ConfigDecoder();
        Assertions.assertEquals("ConfigContainer", decoder.name());
    }

    @Test
    void priority() {
        ConfigDecoder decoder = new ConfigDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        ConfigDecoder decoder = new ConfigDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<ConfigContainer<String>>() {}));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<ConfigContainer<List<String>>>() {}));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""),
            new TypeCapture<ConfigContainer<Map<String, String>>>() {}));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Character.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Character>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(char.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Float.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Float>>() {
        }));
    }

    @Test
    void decode() throws GestaltException {
        ConfigDecoder decoder = new ConfigDecoder();

        GResultOf<ConfigContainer<?>> result = decoder.decode("db.user", Tags.of(), new LeafNode("test"),
            new TypeCapture<ConfigContainer<String>>() {}, new DecoderContext(decoderService, gestalt, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(true, result.results().isPresent());
        Assertions.assertEquals("test", result.results().orElseThrow());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void invalidLeafNode() {
        ConfigDecoder decoder = new ConfigDecoder();

        GResultOf<ConfigContainer<?>> result = decoder.decode("db.user", Tags.of(), new LeafNode(null),
            new TypeCapture<ConfigContainer<String>>() {}, new DecoderContext(decoderService, gestalt, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.user, has no value attempting to decode String",
            result.getErrors().get(0).description());
    }

    @Test
    void nullLeafNode() {
        ConfigDecoder decoder = new ConfigDecoder();

        GResultOf<ConfigContainer<?>> result = decoder.decode("db.user", Tags.of(), null,
            new TypeCapture<ConfigContainer<String>>() {}, new DecoderContext(decoderService, gestalt, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: null, attempting to decode String",
            result.getErrors().get(0).description());
    }

    @Test
    void decodeInvalidNode() {
        ConfigDecoder decoder = new ConfigDecoder();

        GResultOf<ConfigContainer<?>> result = decoder.decode("db.user", Tags.of(), new MapNode(new HashMap<>()),
            new TypeCapture<ConfigContainer<String>>() {}, new DecoderContext(decoderService, gestalt, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Expected a leaf on path: db.user, received node type: map, attempting to decode String",
            result.getErrors().get(0).description());
    }
}
