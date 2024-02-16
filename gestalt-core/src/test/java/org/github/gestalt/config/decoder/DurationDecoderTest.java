package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
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

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class DurationDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new DurationDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        DurationDecoder decoder = new DurationDecoder();
        Assertions.assertEquals("Duration", decoder.name());
    }

    @Test
    void priority() {
        DurationDecoder decoder = new DurationDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        DurationDecoder decoder = new DurationDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Duration.class)));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Duration>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Long>>() {
        }));
    }

    @Test
    void decode() {
        DurationDecoder decoder = new DurationDecoder();

        GResultOf<Duration> result = decoder.decode("db.port", Tags.of(), new LeafNode("124"),
            TypeCapture.of(Long.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(Duration.ofMillis(124L), result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeISO8601() {
        DurationDecoder decoder = new DurationDecoder();

        GResultOf<Duration> result = decoder.decode("db.port", Tags.of(), new LeafNode("PT20S"),
            TypeCapture.of(Long.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(Duration.ofSeconds(20), result.results());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeInvalidNode() {
        DurationDecoder decoder = new DurationDecoder();

        GResultOf<Duration> result = decoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
            TypeCapture.of(Long.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a Duration on path: db.port, from node: LeafNode{value='12s4'}",
            result.getErrors().get(0).description());
    }

}
