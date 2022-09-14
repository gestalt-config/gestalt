package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.test.classes.*;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

class RecordDecoderTest {

    ConfigNodeService configNodeService;
    final SentenceLexer lexer = new PathLexer();
    DecoderRegistry registry;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        registry = new DecoderRegistry(Arrays.asList(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new RecordDecoder()), configNodeService, lexer);
    }

    @Test
    void name() {
        RecordDecoder decoder = new RecordDecoder();
        Assertions.assertEquals("Record", decoder.name());
    }

    @Test
    void priority() {
        RecordDecoder decoder = new RecordDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        RecordDecoder decoder = new RecordDecoder();

        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfo.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfoExtended.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Map<String, Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<DBInfoGeneric<String>>() {
        }));
    }

    @Test
    void decode() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("id", new LeafNode("52"));

        ValidateOf<Object> validate = decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(Person.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Person results = (Person) validate.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }

    @Test
    void decodeDifferentOrder() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("id", new LeafNode("52"));
        configs.put("name", new LeafNode("tim"));

        ValidateOf<Object> validate = decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(Person.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Person results = (Person) validate.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }

    @Test
    void decodePerson2() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("id", new LeafNode("52"));

        ValidateOf<Object> validate = decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(Person2.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Person2 results = (Person2) validate.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }

    @Test
    void decodePersonExtraValues() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("id", new LeafNode("52"));
        configs.put("address", new LeafNode("home"));
        configs.put("phone", new LeafNode("12345"));

        ValidateOf<Object> validate = decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(Person.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        Person results = (Person) validate.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }

    @Test
    void decodePersonMissingValues() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        //configs.put("id", new LeafNode("52"));
        configs.put("address", new LeafNode("home"));
        configs.put("phone", new LeafNode("12345"));

        ValidateOf<Object> validate = decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(Person.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: user.admin.id, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());
    }

    @Test
    void decodePersonWrongValues() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("id", new LeafNode("dog"));
        configs.put("address", new LeafNode("home"));
        configs.put("phone", new LeafNode("12345"));

        ValidateOf<Object> validate = decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(Person.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: user.admin.id, from node: LeafNode{value='dog'} " +
            "attempting to decode Integer", validate.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: user.admin.id, for class: Class, during record decoding",
            validate.getErrors().get(1).description());
    }

    @Test
    void decodePersonWrongNode() {
        RecordDecoder decoder = new RecordDecoder();

        ValidateOf<Object> validate = decoder.decode("user.admin", new LeafNode("12345"), TypeCapture.of(Person.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a leaf on path: user.admin, received node type, received: LeafNode{value='12345'} " +
            "attempting to decode Record", validate.getErrors().get(0).description());
    }

    @Test
    void decodeAnnotations() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("identity", new LeafNode("52"));

        ValidateOf<Object> validate = decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(PersonAnnotations.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        PersonAnnotations results = (PersonAnnotations) validate.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }

    @Test
    void decodeAnnotationsDefault() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));

        ValidateOf<Object> validate = decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(PersonAnnotations.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: user.admin.identity, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        PersonAnnotations results = (PersonAnnotations) validate.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(1234, results.id());
    }

    @Test
    void decodeAnnotationsBadDefault() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));

        ValidateOf<Object> validate =
            decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(PersonBadAnnotations.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: user.admin.identity, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());
        Assertions.assertEquals("Unable to parse a number on Path: user.admin.identity, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer", validate.getErrors().get(1).description());
    }

    @Test
    void decodeAnnotationsLong() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("identity", new MapNode(Map.of("user", new LeafNode("52"))));

        ValidateOf<Object> validate = decoder.decode("user.admin", new MapNode(configs), TypeCapture.of(PersonAnnotationsLong.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        PersonAnnotationsLong results = (PersonAnnotationsLong) validate.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }
}
