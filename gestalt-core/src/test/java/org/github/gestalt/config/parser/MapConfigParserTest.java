package org.github.gestalt.config.parser;

import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapConfigParserTest {

    @Test
    public void testBuildConfigTree() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(Arrays.asList(new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("20"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.parse(test, false);
        assertTrue(validateOfResults.hasResults());
        assertFalse(validateOfResults.hasErrors());
        assertNotNull(validateOfResults.results());
        ConfigNode results = validateOfResults.results();
        assertEquals(2, results.size());
        assertEquals("10", results.getKey("db").flatMap(db -> db.getKey("connections")).get().getValue().get());
        assertEquals("test", results.getKey("db").get().getKey("user").get().getValue().get());
        assertEquals("password", results.getKey("db").get().getKey("password").get().getValue().get());
        assertEquals("host1", results.getKey("db").get().getKey("hosts").get().getIndex(0).get().getValue().get());
        assertEquals("host2", results.getKey("db").get().getKey("hosts").get().getIndex(1).get().getValue().get());
        assertEquals("20", results.getKey("redis").get().getKey("port").get().getValue().get());
    }

    @Test
    public void testBuildConfigTreeMissingResults() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(Arrays.asList(new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("20"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(validateOfResults.hasResults());
        assertFalse(validateOfResults.hasErrors());
        assertNotNull(validateOfResults.results());
        ConfigNode results = validateOfResults.results();
        assertEquals(2, results.size());
        assertEquals(Optional.empty(), results.getKey("db").get().getKey("nokey"));
        assertEquals(Optional.empty(), results.getKey("db").get().getKey("hosts").get().getIndex(2));
    }

    @Test
    public void testValidConfigTree() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(Arrays.asList(new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("11"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(validateOfResults.hasResults());
        assertFalse(validateOfResults.hasErrors());
        assertNotNull(validateOfResults.results());
        ConfigNode results = validateOfResults.results();
        assertEquals(2, results.size());

        assertEquals("10", results.getKey("db").flatMap(db -> db.getKey("connections")).flatMap(ConfigNode::getValue).get());
        assertEquals("test", results.getKey("db").flatMap(db -> db.getKey("user")).flatMap(ConfigNode::getValue).get());
        assertEquals("password", results.getKey("db").flatMap(db -> db.getKey("password")).flatMap(ConfigNode::getValue).get());

        assertEquals("host1", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(0).get().getValue().get());
        assertEquals("host2", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(1).get().getValue().get());

        assertEquals("11", results.getKey("redis").flatMap(db -> db.getKey("port")).flatMap(ConfigNode::getValue).get());
    }

    @Test
    public void testValidConfigTreeArrayOfSingleObjects() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ObjectToken("name")), new ConfigValue("host1")),
            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ObjectToken("port")), new ConfigValue("11")),
            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ObjectToken("name")), new ConfigValue("host2")),
            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ObjectToken("port")), new ConfigValue("12")),
            new Pair<>(Arrays.asList(
                new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("101"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(validateOfResults.hasResults());
        assertFalse(validateOfResults.hasErrors());
        assertNotNull(validateOfResults.results());
        ConfigNode results = validateOfResults.results();
        assertEquals(2, results.size());

        assertEquals("10", results.getKey("db").flatMap(db -> db.getKey("connections")).flatMap(ConfigNode::getValue).get());
        assertEquals("test", results.getKey("db").flatMap(db -> db.getKey("user")).flatMap(ConfigNode::getValue).get());
        assertEquals("password", results.getKey("db").flatMap(db -> db.getKey("password")).flatMap(ConfigNode::getValue).get());

        assertEquals("host1", results.getKey("db").flatMap(db -> db.getKey("hosts")).get()
            .getIndex(0).get().getKey("name").get().getValue().get());
        assertEquals("11", results.getKey("db").flatMap(db -> db.getKey("hosts")).get()
            .getIndex(0).get().getKey("port").get().getValue().get());

        assertEquals("host2", results.getKey("db").flatMap(db -> db.getKey("hosts")).get()
            .getIndex(1).get().getKey("name").get().getValue().get());
        assertEquals("12", results.getKey("db").flatMap(db -> db.getKey("hosts")).get()
            .getIndex(1).get().getKey("port").get().getValue().get());


        assertEquals("101", results.getKey("redis").flatMap(db -> db.getKey("port")).flatMap(ConfigNode::getValue).get());
    }

    @Test
    public void testValidConfigTreeArrayOfArraysObjects() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ArrayToken(0),
                new ObjectToken("name")), new ConfigValue("host1")),

            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ArrayToken(1),
                new ObjectToken("port")), new ConfigValue("11")),

            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ArrayToken(0),
                new ObjectToken("name")), new ConfigValue("host2")),

            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ArrayToken(1),
                new ObjectToken("port")), new ConfigValue("12")),

            new Pair<>(Arrays.asList(
                new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("101"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(validateOfResults.hasResults());
        assertFalse(validateOfResults.hasErrors());
        assertNotNull(validateOfResults.results());
        ConfigNode results = validateOfResults.results();
        assertEquals(2, results.size());
        assertEquals("10", results.getKey("db").flatMap(db -> db.getKey("connections")).flatMap(ConfigNode::getValue).get());
        assertEquals("test", results.getKey("db").flatMap(db -> db.getKey("user")).flatMap(ConfigNode::getValue).get());
        assertEquals("password", results.getKey("db").flatMap(db -> db.getKey("password")).flatMap(ConfigNode::getValue).get());

        assertEquals("host1", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(0).get()
            .getIndex(0).get().getKey("name").get().getValue().get());
        assertEquals("11", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(0).get()
            .getIndex(1).get().getKey("port").get().getValue().get());

        assertEquals("host2", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(1).get()
            .getIndex(0).get().getKey("name").get().getValue().get());
        assertEquals("12", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(1).get()
            .getIndex(1).get().getKey("port").get().getValue().get());

        assertEquals("101", results.getKey("redis").flatMap(db -> db.getKey("port")).flatMap(ConfigNode::getValue).get());
    }

    @Test
    public void testValidConfigTreeArrayOfObjects() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Collections.singletonList(new ObjectToken("redis")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ObjectToken("name")), new ConfigValue("host1"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(validateOfResults.hasResults());
        assertFalse(validateOfResults.hasErrors());
        assertNotNull(validateOfResults.results());
        ConfigNode results = validateOfResults.results();
        assertEquals(2, results.size());
        assertEquals("10", results.getKey("db").flatMap(db -> db.getKey("connections")).flatMap(ConfigNode::getValue).get());
        assertEquals("test", results.getKey("db").flatMap(db -> db.getKey("user")).flatMap(ConfigNode::getValue).get());
        assertEquals("password", results.getKey("db").flatMap(db -> db.getKey("password")).flatMap(ConfigNode::getValue).get());
        assertEquals("host1", results.getKey("db").flatMap(db -> db.getKey("hosts"))
            .flatMap(configNode -> configNode.getIndex(0)).get().getKey("name").get().getValue().get());
    }

    @Test
    public void testValidateNullTokens() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(null, 0, false);
        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Empty or null token provided while building the config node", results.get(0).description());
    }

    @Test
    public void testValidateMultipleTokenTypes() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertThat(results.get(0).description())
            .startsWith("Found multiple token types")
            .contains("ArrayToken")
            .contains("ObjectToken");
    }

    @Test
    public void testValidateMultipleTokenTypesWarnings() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNotNull(validateOfResults.results());
        assertEquals("test", validateOfResults.results().getKey("db").get().getKey("user").get().getValue().get());
        assertFalse(validateOfResults.results().getKey("db").get().getKey("hosts").isPresent());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertThat(results.get(0).description())
            .startsWith("Found multiple token types")
            .contains("ArrayToken")
            .contains("ObjectToken");
    }

    @Test
    public void testValidateMismatchedPathLength() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts")), new ConfigValue("hostDB1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB2"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Mismatched path lengths received for path: db.hosts, " +
            "this could be because a node is both a leaf and an object", results.get(0).description());
    }

    @Test
    public void testValidateMismatchedPathLengthWarnings() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts")), new ConfigValue("hostDB1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB2"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNotNull(validateOfResults.results());
        assertEquals("test", validateOfResults.results().getKey("db").get().getKey("user").get().getValue().get());
        assertFalse(validateOfResults.results().getKey("db").get().getKey("hosts").isPresent());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Mismatched path lengths received for path: db.hosts, " +
            "this could be because a node is both a leaf and an object", results.get(0).description());
    }

    @Test
    public void testValidateMismatchedPathLength2() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Collections.singletonList(new ObjectToken("db")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts")), new ConfigValue("hostDB1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB2"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Mismatched path lengths received for path: db, this could be because " +
            "a node is both a leaf and an object", results.get(0).description());
    }

    @Test
    public void testValidateBadArrayIndex() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(-1)), new ConfigValue("host2"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, true);

        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Invalid array index: -1 for path: db.hosts", results.get(0).description());
    }

    @Test
    public void testValidateDuplicateArray() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host2"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Duplicate array index: 0 for path: db.hosts", results.get(0).description());
    }

    @Test
    public void testValidateMismatchedArrayPathLength() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("hostDB1")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ObjectToken("name")),
                new ConfigValue("hostDB2"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Array is both a leaf and non leaf with sizes: [4, 3] for path: db.hosts", results.get(0).description());
    }

    @Test
    public void testArrayIndexMissing() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host0")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(2)), new ConfigValue("host2")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(4)), new ConfigValue("host4")),
            new Pair<>(Arrays.asList(new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("10"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNotNull(validateOfResults.results());
        List<ValidationError> errors = validateOfResults.getErrors();
        assertEquals(2, errors.size());
        assertEquals(ValidationLevel.WARN, errors.get(0).level());
        assertEquals("Missing array index: 1 for path: db.hosts", errors.get(0).description());
        assertEquals(ValidationLevel.WARN, errors.get(1).level());
        assertEquals("Missing array index: 3 for path: db.hosts", errors.get(1).description());

        ConfigNode results = validateOfResults.results();
        assertEquals("host0", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(0).get().getValue().get());
        assertFalse(results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(1).isPresent());
        assertEquals("host2", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(2).get().getValue().get());
        assertFalse(results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(3).isPresent());
        assertEquals("host4", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(4).get().getValue().get());
    }

    @Test
    public void testValidConfigTreeArrayOfArraysDuplicateIndex() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ArrayToken(0)), new ConfigValue("host1")),

            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ArrayToken(0)), new ConfigValue("11")),

            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ArrayToken(0),
                new ObjectToken("name")), new ConfigValue("host1")),

            new Pair<>(Arrays.asList(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ArrayToken(1),
                new ObjectToken("port")), new ConfigValue("12")),

            new Pair<>(Arrays.asList(
                new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("10"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Duplicate array index: 0 for path: db.hosts[0]", results.get(0).description());
    }

    @Test
    public void testDuplicateArrayIndex() {
        MapConfigParser mapConfigParser = new MapConfigParser();
        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("admin"), new ArrayToken(0)), new ConfigValue("John")),
            new Pair<>(Arrays.asList(new ObjectToken("admin"), new ArrayToken(1)), new ConfigValue("Steve")),
            new Pair<>(Arrays.asList(new ObjectToken("admin"), new ArrayToken(1)), new ConfigValue("Gary"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Duplicate array index: 1 for path: admin", results.get(0).description());
    }

    @Test
    public void testUnknownObjectType() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = Arrays.asList(
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(Arrays.asList(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(Arrays.asList(new ObjectToken("redis"), new OtherToken()), new ConfigValue("10"))
        );

        ValidateOf<ConfigNode> validateOfResults = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(validateOfResults.hasResults());
        assertTrue(validateOfResults.hasErrors());
        assertNull(validateOfResults.results());
        List<ValidationError> results = validateOfResults.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertThat(results.get(0).description())
            .startsWith("Unknown token type")
            .contains("OtherToken");
    }

    public static class OtherToken extends Token {

    }
}
