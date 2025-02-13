package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfoOptional;
import org.github.gestalt.config.test.classes.DBInfoOptional1;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * Test for a generic Optional.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
class OptionalDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new OptionalDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void priority() {
        OptionalDecoder optDecoder = new OptionalDecoder();
        Assertions.assertEquals(Priority.MEDIUM, optDecoder.priority());
    }

    @Test
    void name() {
        OptionalDecoder optDecoder = new OptionalDecoder();
        Assertions.assertEquals("Optional", optDecoder.name());
    }

    @Test
    void canDecode() {
        OptionalDecoder decoder = new OptionalDecoder();

        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Optional<Short>>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Optional<List<Short>>>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Integer>>() {
        }));

    }

    @Test
    void decodeLeafInteger() {
        OptionalDecoder decoder = new OptionalDecoder();

        GResultOf<Optional<?>> result = decoder.decode("db.port", Tags.of(), new LeafNode("124"), new TypeCapture<Optional<Integer>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.results().isPresent());
        Assertions.assertEquals(124, result.results().get());
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    void decodeLeafIntegerEmpty() {
        OptionalDecoder decoder = new OptionalDecoder();

        GResultOf<Optional<?>> result = decoder.decode("db.port", Tags.of(), new LeafNode(null), new TypeCapture<Optional<Integer>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.results().isPresent());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Optional on path: db.port, with node: LeafNode{value='null'}",
            result.getErrors().get(0).description());
    }

    @SuppressWarnings("unchecked")
    @Test
    void decodeObjectOfOptional() {
        OptionalDecoder decoder = new OptionalDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Optional<?>> result = decoder.decode("db", Tags.of(), new MapNode(configs), new TypeCapture<Optional<DBInfoOptional>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.results().isPresent());

        Optional<DBInfoOptional> results = (Optional<DBInfoOptional>) result.results();
        Assertions.assertEquals(100, results.get().getPort().get());
        Assertions.assertEquals("pass", results.get().getPassword().get());
        Assertions.assertEquals("mysql.com", results.get().getUri().get());
    }

    @Test
    void decodeObjectOptionalIntegerEmpty() {
        OptionalDecoder decoder = new OptionalDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Optional<?>> result = decoder.decode("db", Tags.of(), new MapNode(configs), new TypeCapture<Optional<DBInfoOptional>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertTrue(result.results().isPresent());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.port, with node: " +
                "MapNode{password=LeafNode{value='pass'}, uri=LeafNode{value='mysql.com'}}, with class: DBInfoOptional",
            result.getErrors().get(0).description());

        Optional<DBInfoOptional> results = (Optional<DBInfoOptional>) result.results();
        Assertions.assertFalse(results.get().getPort().isPresent());
        Assertions.assertEquals("pass", results.get().getPassword().get());
        Assertions.assertEquals("mysql.com", results.get().getUri().get());
    }

    @Test
    void decodeObjectIntegerEmpty() {
        OptionalDecoder decoder = new OptionalDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Optional<?>> result = decoder.decode("db", Tags.of(), new MapNode(configs), new TypeCapture<Optional<DBInfoOptional1>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertTrue(result.results().isPresent());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.port, for class: DBInfoOptional1, during object decoding",
            result.getErrors().get(0).description());

        Optional<DBInfoOptional1> results = (Optional<DBInfoOptional1>) result.results();
        Assertions.assertNull(results.get().getPort());
        Assertions.assertEquals("pass", results.get().getPassword().get());
        Assertions.assertEquals("mysql.com", results.get().getUri().get());
    }

    @Test
    void decodeLeafIntegerNull() {
        OptionalDecoder decoder = new OptionalDecoder();

        GResultOf<Optional<?>> result = decoder.decode("db.port", Tags.of(), null, new TypeCapture<Optional<Integer>>() {
        }, new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.results().isPresent());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Optional on path: db.port",
            result.getErrors().get(0).description());
    }

    @Test
    void notAnInteger() {
        OptionalDecoder integerDecoder = new OptionalDecoder();

        GResultOf<Optional<?>> result = integerDecoder.decode("db.port", Tags.of(), new LeafNode("12s4"),
            new TypeCapture<Optional<Integer>>() { }, new DecoderContext(decoderService, null, null, new PathLexer()));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals(Optional.empty(), result.results());

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode Integer",
            result.getErrors().get(0).description());
    }
}
