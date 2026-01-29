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
import java.util.UUID;

class UUIDDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new UUIDDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        UUIDDecoder decoder = new UUIDDecoder();
        Assertions.assertEquals("UUID", decoder.name());
    }

    @Test
    void priority() {
        UUIDDecoder decoder = new UUIDDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        UUIDDecoder longDecoder = new UUIDDecoder();

        Assertions.assertTrue(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(UUID.class)));
        Assertions.assertTrue(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<UUID>() {
        }));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(long.class)));

        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Long>>() {
        }));
    }

    @Test
    void decode() {
        UUIDDecoder decoder = new UUIDDecoder();


        UUID uuid = UUID.randomUUID();
        GResultOf<UUID> result = decoder.decode("db.port", Tags.of(), new LeafNode(uuid.toString()),
            TypeCapture.of(Long.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(uuid, result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeInvalidNode() {
        UUIDDecoder decoder = new UUIDDecoder();


        GResultOf<UUID> result = decoder.decode("db.port", Tags.of(), new LeafNode("asdfasdfsdf"),
            TypeCapture.of(Long.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a UUID on path: db.port, from node: LeafNode{value='asdfasdfsdf'}, with reason: " +
                "Invalid UUID string: asdfasdfsdf",
            result.getErrors().get(0).description());
    }

    @Test
    void emptyStringWithConfigEnabled() {
        UUIDDecoder decoder = new UUIDDecoder();
        GestaltConfig config = new GestaltConfig();
        config.setTreatEmptyStringAsAbsent(true);

        GResultOf<UUID> result = decoder.decode("db.id", Tags.of(), new LeafNode(""),
            TypeCapture.of(UUID.class), new DecoderContext(decoderService, null, null, new PathLexer(), config));

        Assertions.assertFalse(result.hasResults());
        Assertions.assertEquals(0, result.getErrors().size());
        Assertions.assertNull(result.results());
    }
}
