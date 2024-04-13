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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RecordDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry registry;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        registry = new DecoderRegistry(List.of(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new RecordDecoder(), new OptionalDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
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
    void canDecode() {
        RecordDecoder decoder = new RecordDecoder();

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfo.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(DBInfoExtended.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<Map<String, Long>>() {
        }));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<DBInfoGeneric<String>>() {
        }));
    }

    @Test
    void decode() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("id", new LeafNode("52"));

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(), new MapNode(configs), TypeCapture.of(Person.class),
            new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Person results = (Person) result.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }

    @Test
    void decodeDifferentOrder() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("id", new LeafNode("52"));
        configs.put("name", new LeafNode("tim"));

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(), new MapNode(configs), TypeCapture.of(Person.class),
            new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Person results = (Person) result.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }

    @Test
    void decodePerson2() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("id", new LeafNode("52"));

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(), new MapNode(configs), TypeCapture.of(Person2.class),
            new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Person2 results = (Person2) result.results();
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

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(), new MapNode(configs), TypeCapture.of(Person.class),
            new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Person results = (Person) result.results();
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

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(), new MapNode(configs), TypeCapture.of(Person.class),
            new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: user.admin.id, for class: Person, during record decoding",
            result.getErrors().get(0).description());

        Person results = (Person) result.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertNull(results.id());

    }

    @Test
    void decodePersonWrongValues() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("id", new LeafNode("dog"));
        configs.put("address", new LeafNode("home"));
        configs.put("phone", new LeafNode("12345"));

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(), new MapNode(configs), TypeCapture.of(Person.class),
            new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: user.admin.id, from node: LeafNode{value='dog'} " +
            "attempting to decode Integer", result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: user.admin.id, for class: Person, during record decoding",
            result.getErrors().get(1).description());

        Person results = (Person) result.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(null, results.id());
    }

    @Test
    void decodePersonWrongNode() {
        RecordDecoder decoder = new RecordDecoder();

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(), new LeafNode("12345"),
            TypeCapture.of(Person.class), new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().getFirst().level());
        Assertions.assertEquals("Expected a leaf on path: user.admin, received node type: leaf, attempting to decode Record",
            result.getErrors().getFirst().description());
    }

    @Test
    void decodeAnnotations() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("identity", new LeafNode("52"));

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(),
                new MapNode(configs), TypeCapture.of(PersonAnnotations.class), new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        PersonAnnotations results = (PersonAnnotations) result.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }

    @Test
    void decodeAnnotationsDefault() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(),
                new MapNode(configs), TypeCapture.of(PersonAnnotations.class), new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().getFirst().level());
        Assertions.assertEquals("Missing Optional Value while decoding Record on path: user.admin.identity, with node: " +
            "MapNode{name=LeafNode{value='tim'}}, with class: PersonAnnotations", result.getErrors().getFirst().description());

        PersonAnnotations results = (PersonAnnotations) result.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(1234, results.id());
    }

    @Test
    void decodeAnnotationsBadDefault() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));

        GResultOf<Object> result =
            decoder.decode("user.admin", Tags.of(), new MapNode(configs), TypeCapture.of(PersonBadAnnotations.class),
                new DecoderContext(registry, null, null, new PathLexer()));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: user.admin.identity, from node: " +
            "LeafNode{value='abc'} attempting to decode Integer", result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: user.admin.identity, for class: PersonBadAnnotations, " +
            "during record decoding", result.getErrors().get(1).description());

        PersonBadAnnotations results = (PersonBadAnnotations) result.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertNull(results.id());
    }

    @Test
    void decodeAnnotationsLong() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("identity", new MapNode(Map.of("user", new LeafNode("52"))));

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(),
                new MapNode(configs), TypeCapture.of(PersonAnnotationsLong.class), new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        PersonAnnotationsLong results = (PersonAnnotationsLong) result.results();
        Assertions.assertEquals("tim", results.name());
        Assertions.assertEquals(52, results.id());
    }

    @Test
    void decodeOptional() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));
        configs.put("id", new LeafNode("52"));

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(), new MapNode(configs),
            TypeCapture.of(PersonOptional.class), new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        PersonOptional results = (PersonOptional) result.results();
        Assertions.assertEquals("tim", results.name().get());
        Assertions.assertEquals(52, results.id().get());
    }

    @Test
    void decodeOptionalEmptyValue() {
        RecordDecoder decoder = new RecordDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("name", new LeafNode("tim"));

        GResultOf<Object> result = decoder.decode("user.admin", Tags.of(), new MapNode(configs),
            TypeCapture.of(PersonOptional.class), new DecoderContext(registry, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Record on path: user.admin.id, with node: " +
            "MapNode{name=LeafNode{value='tim'}}, with class: PersonOptional", result.getErrors().get(0).description());

        PersonOptional results = (PersonOptional) result.results();
        Assertions.assertEquals("tim", results.name().get());
        Assertions.assertFalse(results.id().isPresent());

    }
}
