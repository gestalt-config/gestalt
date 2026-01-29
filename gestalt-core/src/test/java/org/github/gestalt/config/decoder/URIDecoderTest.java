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

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class URIDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;
    DecoderService decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        decoderService = new DecoderRegistry(Collections.singletonList(new URIDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }


    @Test
    void name() {
        URIDecoder decoder = new URIDecoder();
        Assertions.assertEquals("URI", decoder.name());
    }

    @Test
    void priority() {
        URIDecoder decoder = new URIDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void canDecode() {
        URIDecoder longDecoder = new URIDecoder();

        Assertions.assertTrue(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(URI.class)));
        Assertions.assertTrue(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<URI>() {
        }));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(long.class)));

        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(longDecoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<URI>>() {
        }));
    }

    @Test
    void decode() {
        URIDecoder decoder = new URIDecoder();

        String uri = "http://www.google.com";
        GResultOf<URI> result = decoder.decode("db.port", Tags.of(), new LeafNode(uri),
            TypeCapture.of(URI.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(uri, result.results().toString());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeInvalidNode() {
        URIDecoder decoder = new URIDecoder();

        String uri = "http://www.google.com[]";
        GResultOf<URI> result = decoder.decode("db.port", Tags.of(), new LeafNode(uri),
            TypeCapture.of(URI.class), new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNull(result.results());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to decode a URI on path: db.port, from node: LeafNode{value='http://www.google.com[]'}, " +
                "with reason: Illegal character in hostname at index 21: http://www.google.com[]",
            result.getErrors().get(0).description());
    }

    @Test
    void emptyStringWithConfigEnabled() {
        URIDecoder decoder = new URIDecoder();
        GestaltConfig config = new GestaltConfig();
        config.setTreatEmptyStringAsAbsent(true);

        GResultOf<URI> result = decoder.decode("db.port", Tags.of(), new LeafNode(""),
            TypeCapture.of(URI.class), new DecoderContext(decoderService, null, null, new PathLexer(), config));

        Assertions.assertFalse(result.hasResults());
        // Filter out MISSING_OPTIONAL_VALUE level errors - these are informational, not real errors
        Assertions.assertTrue(result.getErrorsNotLevel(org.github.gestalt.config.entity.ValidationLevel.MISSING_OPTIONAL_VALUE).isEmpty());
        Assertions.assertNull(result.results());
    }
}
