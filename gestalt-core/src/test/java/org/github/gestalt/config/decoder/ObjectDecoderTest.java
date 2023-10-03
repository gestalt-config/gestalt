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
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ObjectDecoderTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new OptionalDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void name() {
        ObjectDecoder decoder = new ObjectDecoder();
        Assertions.assertEquals("Object", decoder.name());
    }

    @Test
    void priority() {
        ObjectDecoder decoder = new ObjectDecoder();
        Assertions.assertEquals(Priority.VERY_LOW, decoder.priority());
    }

    @Test
    void matches() {
        ObjectDecoder decoder = new ObjectDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(DBInfo.class)));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(DBInfoExtended.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Map<String, Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<DBInfoGeneric<String>>() {
        }));
    }

    @Test
    void decode() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfo.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfo results = (DBInfo) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeInherited() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));
        configs.put("user", new LeafNode("Ted"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoExtended.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        DBInfoExtended results = (DBInfoExtended) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
        Assertions.assertEquals("Ted", results.getUser());
        Assertions.assertEquals(10000, results.getTimeout());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.timeout, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

    }

    @Test
    void decodeNoDefaultConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoNoDefaultConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("No default Constructor for : org.github.gestalt.config.test.classes.DBInfoNoDefaultConstructor on " +
            "Path: db.host", validate.getErrors().get(0).description());
    }

    @Test
    void decodePrivateConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoPrivateConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Constructor for: org.github.gestalt.config.test.classes.DBInfoPrivateConstructor is not public on " +
            "Path: db.host", validate.getErrors().get(0).description());
    }

    @Test
    void decodeDefaultValues() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInforNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.password, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        DBInforNoConstructor results = (DBInforNoConstructor) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("password", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeSetterModifyValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoSetterChangeValue.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfoSetterChangeValue results = (DBInfoSetterChangeValue) validate.results();
        Assertions.assertEquals(200, results.getPort());
        Assertions.assertEquals("****", results.getPassword());
        Assertions.assertEquals("mysql.comabc", results.getUri());
    }

    @Test
    void decodeGetterModifyValueNotNull() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoIntegerPortNonNullGetter.class), new DecoderContext(decoderService, null));

        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());


        DBInfoIntegerPortNonNullGetter results = (DBInfoIntegerPortNonNullGetter) validate.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadNodeNotAnInt() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("aaaa"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInforNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.port, from node: " +
            "LeafNode{value='aaaa'} attempting to decode Integer", validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: int, during object decoding",
            validate.getErrors().get(1).description());

        DBInforNoConstructor results = (DBInforNoConstructor) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeBadNodeNotAnIntNullResult() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("aaaa"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoIntegerPort.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.port, from node: " +
            "LeafNode{value='aaaa'} attempting to decode Integer", validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(1).level());
        Assertions.assertEquals("Decoding object : DBInfoIntegerPort on path: db.host.port, field port results in null value",
            validate.getErrors().get(1).description());

        DBInfoIntegerPort results = (DBInfoIntegerPort) validate.results();
        Assertions.assertNull(results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeStaticMethod() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoStatic.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfoStatic results = (DBInfoStatic) validate.results();
        Assertions.assertEquals(0, DBInfoStatic.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeOptional() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoOptional.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfoOptional results = (DBInfoOptional) validate.results();
        Assertions.assertEquals(100, results.getPort().get());
        Assertions.assertEquals("pass", results.getPassword().get());
        Assertions.assertEquals("mysql.com", results.getUri().get());
    }

    @Test
    void decodeOptionalMissingValues() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoOptional.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        DBInfoOptional results = (DBInfoOptional) validate.results();
        Assertions.assertFalse(results.getPort().isPresent());
        Assertions.assertFalse(results.getPassword().isPresent());
        Assertions.assertFalse(results.getUri().isPresent());

        Assertions.assertEquals(3, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, during navigating to next node",
            validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.uri, for class: ObjectToken, during navigating to next node",
            validate.getErrors().get(1).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(2).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.password, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(2).description());
    }

    @Test
    void decodeOptionalPartialMissingValues() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoOptional.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        DBInfoOptional results = (DBInfoOptional) validate.results();
        Assertions.assertEquals(100, results.getPort().get());
        Assertions.assertFalse(results.getPassword().isPresent());
        Assertions.assertFalse(results.getUri().isPresent());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.uri, for class: ObjectToken, during navigating to next node",
            validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.password, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(1).description());
    }

    @Test
    void decodeNullLeafNodeValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode(null));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInforNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Leaf on path: db.host.port, has no value attempting to decode Integer",
            validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(1).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: int, during object decoding",
            validate.getErrors().get(1).description());

        DBInforNoConstructor results = (DBInforNoConstructor) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeNullLeafNode() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", null);
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInforNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, " +
                "during navigating to next node",
            validate.getErrors().get(0).description());

        DBInforNoConstructor results = (DBInforNoConstructor) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeNullMapNodeWithDefaults() {
        ObjectDecoder decoder = new ObjectDecoder();

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(null), TypeCapture.of(DBInforNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(3, validate.getErrors().size());

        org.assertj.core.api.Assertions.assertThat(validate.getErrors().stream()
            .allMatch(it -> it.level().equals(ValidationLevel.MISSING_VALUE))).isTrue();

        org.assertj.core.api.Assertions.assertThat(validate.getErrors()).anyMatch(it ->
            "Unable to find node matching path: db.host.uri, for class: ObjectToken, during navigating to next node"
                .equals(it.description()));

        org.assertj.core.api.Assertions.assertThat(validate.getErrors()).anyMatch(it ->
            "Unable to find node matching path: db.host.port, for class: ObjectToken, during navigating to next node"
                .equals(it.description()));

        org.assertj.core.api.Assertions.assertThat(validate.getErrors()).anyMatch(it ->
            "Unable to find node matching path: db.host.password, for class: ObjectToken, during navigating to next node"
                .equals(it.description()));

        DBInforNoConstructor results = (DBInforNoConstructor) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("password", results.getPassword());
        Assertions.assertEquals("test", results.getUri());
    }

    @Test
    void decodeNullMapNodeDBInfoStatic() {
        ObjectDecoder decoder = new ObjectDecoder();

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(null), TypeCapture.of(DBInfoStatic.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(4, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.uri, for class: ObjectToken, " +
                "during navigating to next node",
            validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(1).level());
        Assertions.assertEquals("Decoding object : DBInfoStatic on path: db.host.uri, field uri results in null value",
            validate.getErrors().get(1).description());

        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(2).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.password, for class: ObjectToken, " +
                "during navigating to next node",
            validate.getErrors().get(2).description());

        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(3).level());
        Assertions.assertEquals("Decoding object : DBInfoStatic on path: db.host.password, field password results in null value",
            validate.getErrors().get(3).description());
    }

    @Test
    void decodeNullNode() {
        ObjectDecoder decoder = new ObjectDecoder();

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                null, TypeCapture.of(DBInforNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : null",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeWrongNodeType() {
        ObjectDecoder decoder = new ObjectDecoder();

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new LeafNode("mysql.com"), TypeCapture.of(DBInforNoConstructor.class), new DecoderContext(decoderService, null));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a map node on path: db.host, received node type : LEAF",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeHttpPool() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("maxtotal", new LeafNode("100"));
        configs.put("maxperroute", new LeafNode("10"));
        configs.put("validateafterinactivity", new LeafNode("60"));
        configs.put("keepalivetimeoutms", new LeafNode("123"));
        configs.put("idletimeoutsec", new LeafNode("1000"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBPool.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.defaultWait, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        DBPool results = (DBPool) validate.results();
        Assertions.assertEquals(100, results.maxTotal);
        Assertions.assertEquals(10, results.maxPerRoute);
        Assertions.assertEquals(60, results.validateAfterInactivity);
        Assertions.assertEquals(123, results.keepAliveTimeoutMs);
        Assertions.assertEquals(1000, results.idleTimeoutSec);
        Assertions.assertEquals(33.0F, results.defaultWait);
    }

    @Test
    void decodeWithAnnotationPath() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfoAnnotations results = (DBInfoAnnotations) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationLongPath() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new MapNode(Map.of("port", new LeafNode("100"))));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoAnnotationsLong.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfoAnnotationsLong results = (DBInfoAnnotationsLong) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        DBInfoAnnotations results = (DBInfoAnnotations) validate.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationOnlyDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate =
            decoder.decode("db.host", Tags.of(), new MapNode(configs),
                    TypeCapture.of(DBInfoAnnotationsDefault.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        DBInfoAnnotationsDefault results = (DBInfoAnnotationsDefault) validate.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationWrongDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(), new MapNode(configs),
                TypeCapture.of(DBInfoBadAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(1).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.channel, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer", validate.getErrors().get(1).description());

        DBInfoBadAnnotations results = (DBInfoBadAnnotations) validate.results();
        Assertions.assertEquals(0, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationWrongDefaultUseClassDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoBadAnnotationsWithClassDefault.class), new DecoderContext(decoderService, null));

        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(1).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.channel, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer", validate.getErrors().get(1).description());

        DBInfoBadAnnotationsWithClassDefault results = (DBInfoBadAnnotationsWithClassDefault) validate.results();
        Assertions.assertNotNull(results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithMethodAnnotation() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoMethodAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfoMethodAnnotations results = (DBInfoMethodAnnotations) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithMethodAnnotationLongPath() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new MapNode(Map.of("port", new LeafNode("100"))));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoMethodAnnotationsLong.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfoMethodAnnotationsLong results = (DBInfoMethodAnnotationsLong) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithMethodAnnotationDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoMethodAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        DBInfoMethodAnnotations results = (DBInfoMethodAnnotations) validate.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithMethodAnnotationOnlyDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoMethodAnnotationsDefault.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        DBInfoMethodAnnotationsDefault results = (DBInfoMethodAnnotationsDefault) validate.results();
        Assertions.assertEquals(1234, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithMethodAnnotationWrongDefault() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoBadMethodAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: db.host.channel, for class: ObjectToken, " +
            "during navigating to next node", validate.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(1).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.channel, from node: LeafNode{value='abc'} " +
            "attempting to decode Integer", validate.getErrors().get(1).description());

        DBInfoBadMethodAnnotations results = (DBInfoBadMethodAnnotations) validate.results();
        Assertions.assertEquals(0, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }

    @Test
    void decodeWithAnnotationPriority() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("channel", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<Object> validate = decoder.decode("db.host", Tags.of(),
                new MapNode(configs), TypeCapture.of(DBInfoBothAnnotations.class), new DecoderContext(decoderService, null));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());

        DBInfoBothAnnotations results = (DBInfoBothAnnotations) validate.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertEquals("mysql.com", results.getUri());
    }
}
