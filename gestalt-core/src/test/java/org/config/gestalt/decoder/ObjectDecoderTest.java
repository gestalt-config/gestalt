package org.config.gestalt.decoder;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.node.MapNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.test.classes.*;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class ObjectDecoderTest {

    DecoderRegistry registry = new DecoderRegistry(Arrays.asList(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
        new ObjectDecoder()));

    ObjectDecoderTest() throws GestaltException {
    }


    @Test
    void name() {
        ObjectDecoder decoder = new ObjectDecoder();
        Assertions.assertEquals("Object", decoder.name());
    }

    @Test
    void matches() {
        ObjectDecoder decoder = new ObjectDecoder();

        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfo.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Long>>() {
        }));
    }

    @Test
    void decode() throws GestaltException {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<DBInfo> validate = decoder.decode("db.host", new MapNode(configs), TypeCapture.of(DBInfo.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(100, validate.results().getPort());
        Assertions.assertEquals("pass", validate.results().getPassword());
        Assertions.assertEquals("mysql.com", validate.results().getUri());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeInherited() throws GestaltException {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));
        configs.put("user", new LeafNode("Ted"));

        ValidateOf<DBInfoExtended> validate = decoder.decode("db.host", new MapNode(configs), TypeCapture.of(DBInfoExtended.class),
            registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertEquals(100, validate.results().getPort());
        Assertions.assertEquals("pass", validate.results().getPassword());
        Assertions.assertEquals("mysql.com", validate.results().getUri());
        Assertions.assertEquals("Ted", validate.results().getUser());
        Assertions.assertEquals(10000, validate.results().getTimeout());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.timeout, for class: int",
            validate.getErrors().get(0).description());

    }

    @Test
    void decodeNoDefaultConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<DBInfoNoDefaultConstructor> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInfoNoDefaultConstructor.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("No default Constructor for : org.credmond.gestalt.test.classes.DBInfoNoDefaultConstructor on " +
                "Path: db.host",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodePrivateConstructor() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<DBInfoPrivateConstructor> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInfoPrivateConstructor.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Constructor for: org.credmond.gestalt.test.classes.DBInfoPrivateConstructor is not public on " +
                "Path: db.host",
            validate.getErrors().get(0).description());
    }

    @Test
    void decodeDefaultValues() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        configs.put("uri", new LeafNode("mysql.com"));

        ValidateOf<DBInforNoConstructor> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.password, for class: String",
            validate.getErrors().get(0).description());

        Assertions.assertEquals(100, validate.results().getPort());
        Assertions.assertEquals("password", validate.results().getPassword());
        Assertions.assertEquals("mysql.com", validate.results().getUri());
    }

    @Test
    void decodeBadNodeNotAnInt() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("aaaa"));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<DBInforNoConstructor> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: db.host.port, from node: " +
                "LeafNode{value='aaaa'} attempting to decode Integer",
            validate.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: int",
            validate.getErrors().get(1).description());

        Assertions.assertEquals(100, validate.results().getPort());
        Assertions.assertEquals("pass", validate.results().getPassword());
        Assertions.assertEquals("mysql.com", validate.results().getUri());
    }

    @Test
    void decodeNullLeafNodeValue() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode(null));
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<DBInforNoConstructor> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(2, validate.getErrors().size());
        Assertions.assertEquals("Leaf on path: db.host.port, missing value, LeafNode{value='null'} attempting to decode Integer",
            validate.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: int",
            validate.getErrors().get(1).description());

        Assertions.assertEquals(100, validate.results().getPort());
        Assertions.assertEquals("pass", validate.results().getPassword());
        Assertions.assertEquals("mysql.com", validate.results().getUri());
    }

    @Test
    void decodeNullLeafNode() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", null);
        configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        ValidateOf<DBInforNoConstructor> validate = decoder.decode("db.host", new MapNode(configs),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.host.port, for class: int",
            validate.getErrors().get(0).description());

        Assertions.assertEquals(100, validate.results().getPort());
        Assertions.assertEquals("pass", validate.results().getPassword());
        Assertions.assertEquals("mysql.com", validate.results().getUri());
    }

    @Test
    void decodeWrongNodeType() {
        ObjectDecoder decoder = new ObjectDecoder();

        ValidateOf<DBInforNoConstructor> validate = decoder.decode("db.host", new LeafNode("mysql.com"),
            TypeCapture.of(DBInforNoConstructor.class), registry);
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());

        Assertions.assertEquals(1, validate.getErrors().size());
        Assertions.assertEquals("Expected a leaf on path: db.host, received node type, received: LeafNode{value='mysql.com'} " +
                "attempting to decode Object",
            validate.getErrors().get(0).description());
    }
}
