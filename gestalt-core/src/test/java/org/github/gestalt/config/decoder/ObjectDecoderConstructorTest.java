package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.*;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

class ObjectDecoderConstructorTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = ObjectDecoderConstructorTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new OptionalDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void decode() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoConstructor.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoConstructor results = (DBInfoConstructor) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeSecondaryConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoConstructor.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoConstructor results = (DBInfoConstructor) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("unknown", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeMissingValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        // configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoConstructor.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.uri, " +
                        "for class: DBInfoConstructor, during object decoding",
                result.getErrors().get(0).description());

        DBInfoConstructor results = (DBInfoConstructor) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals(null, results.getUri());
    }

    @Test
    void decodeAnnotation() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("address", new LeafNode("100"));
        configs.put("hostname", new LeafNode("mysql.com"));
        configs.put("secret", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoConstructorAnnotation.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoConstructorAnnotation results = (DBInfoConstructorAnnotation) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationSecondaryConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("address", new LeafNode("100"));
        configs.put("hostname", new LeafNode("mysql.com"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoConstructorAnnotation.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoConstructorAnnotation results = (DBInfoConstructorAnnotation) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("unknown", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationMissingValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("address", new LeafNode("100"));
        // configs.put("hostname", new LeafNode("mysql.com"));
        configs.put("secret", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoConstructorAnnotation.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(3, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, " +
                        "for class: DBInfoConstructorAnnotation, during object decoding",
                result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.uri, " +
                        "for class: DBInfoConstructorAnnotation, during object decoding",
                result.getErrors().get(1).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(2).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.password, " +
                        "for class: DBInfoConstructorAnnotation, during object decoding",
                result.getErrors().get(2).description());

        DBInfoConstructorAnnotation results = (DBInfoConstructorAnnotation) result.results();
        Assertions.assertEquals(0, results.getPort());
        Assertions.assertEquals(null, results.getPassword());
        Assertions.assertEquals(null, results.getUri());
    }

    @Test
    void decodeAnnotationDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("address", new LeafNode("100"));
        configs.put("hostname", new LeafNode("mysql.com"));
        configs.put("secret", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoConstructorAnnotationDefault.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoConstructorAnnotationDefault results = (DBInfoConstructorAnnotationDefault) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationDefaultSecondaryConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("address", new LeafNode("100"));
        configs.put("hostname", new LeafNode("mysql.com"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoConstructorAnnotationDefault.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoConstructorAnnotationDefault results = (DBInfoConstructorAnnotationDefault) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("default", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeAnnotationDefaultSecondaryConstructorFallback() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("address", new LeafNode("100"));
        configs.put("secret", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoConstructorAnnotationDefault.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        DBInfoConstructorAnnotationDefault results = (DBInfoConstructorAnnotationDefault) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("unknown", results.getUri());
    }

    @Test
    void decodeAnnotationDefaultMissingValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        //configs.put("address", new LeafNode("100"));
        configs.put("hostname", new LeafNode("mysql.com"));
        configs.put("secret", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoConstructorAnnotationDefault.class),
                new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(3, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, " +
                        "for class: DBInfoConstructorAnnotationDefault, during object decoding",
                result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.uri, " +
                        "for class: DBInfoConstructorAnnotationDefault, during object decoding",
                result.getErrors().get(1).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(2).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.password, " +
                        "for class: DBInfoConstructorAnnotationDefault, during object decoding",
                result.getErrors().get(2).description());

        DBInfoConstructorAnnotationDefault results = (DBInfoConstructorAnnotationDefault) result.results();
        Assertions.assertEquals(0, results.getPort());
        Assertions.assertEquals(null, results.getPassword());
        Assertions.assertEquals(null, results.getUri());
    }
}
