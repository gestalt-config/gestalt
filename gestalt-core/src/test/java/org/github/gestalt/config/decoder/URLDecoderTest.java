package org.github.gestalt.config.decoder;

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

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;


class URLDecoderTest {

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
        URLDecoder decoder = new URLDecoder();
        Assertions.assertEquals("URL", decoder.name());
    }

    @Test
    void priority() {
        URLDecoder decoder = new URLDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        URLDecoder longDecoder = new URLDecoder();

        Assertions.assertTrue(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(URL.class)));
        Assertions.assertTrue(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<URL>() {
        }));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(long.class)));

        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<URL>>() {
        }));
    }

    @Test
    void decode() {
        URLDecoder decoder = new URLDecoder();

        String url = "http://www.google.com";
        GResultOf<URL> result = decoder.decode("db.port", Tags.of(), new LeafNode(url),
            TypeCapture.of(URI.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(url, result.results().toString());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeInvalidNode() {
        URLDecoder decoder = new URLDecoder();

        String uri = "8080:www.google.com";
        GResultOf<URL> result = decoder.decode("db.port", Tags.of(), new LeafNode(uri),
            TypeCapture.of(URI.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a URL on path: db.port, from node: LeafNode{value='8080:www.google.com'}, " +
                "with reason: no protocol: 8080:www.google.com",
            result.getErrors().get(0).description());
    }
}
