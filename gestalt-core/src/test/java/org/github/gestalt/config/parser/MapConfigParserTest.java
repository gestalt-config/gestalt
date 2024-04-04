package org.github.gestalt.config.parser;

import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MapConfigParserTest {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = MapConfigParserTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    public void testBuildConfigTree() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(List.of(new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("20"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.parse(test, false);
        assertTrue(resultsOf.hasResults());
        assertFalse(resultsOf.hasErrors());
        assertNotNull(resultsOf.results());
        ConfigNode results = resultsOf.results();
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

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(List.of(new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("20"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(resultsOf.hasResults());
        assertFalse(resultsOf.hasErrors());
        assertNotNull(resultsOf.results());
        ConfigNode results = resultsOf.results();
        assertEquals(2, results.size());
        assertEquals(Optional.empty(), results.getKey("db").get().getKey("nokey"));
        assertEquals(Optional.empty(), results.getKey("db").get().getKey("hosts").get().getIndex(2));
    }

    @Test
    public void testValidConfigTree() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(List.of(new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("11"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(resultsOf.hasResults());
        assertFalse(resultsOf.hasErrors());
        assertNotNull(resultsOf.results());
        ConfigNode results = resultsOf.results();
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

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ObjectToken("name")), new ConfigValue("host1")),
            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ObjectToken("port")), new ConfigValue("11")),
            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ObjectToken("name")), new ConfigValue("host2")),
            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ObjectToken("port")), new ConfigValue("12")),
            new Pair<>(List.of(
                new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("101"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(resultsOf.hasResults());
        assertFalse(resultsOf.hasErrors());
        assertNotNull(resultsOf.results());
        ConfigNode results = resultsOf.results();
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

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ArrayToken(0),
                new ObjectToken("name")), new ConfigValue("host1")),

            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ArrayToken(1),
                new ObjectToken("port")), new ConfigValue("11")),

            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ArrayToken(0),
                new ObjectToken("name")), new ConfigValue("host2")),

            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ArrayToken(1),
                new ObjectToken("port")), new ConfigValue("12")),

            new Pair<>(List.of(
                new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("101"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(resultsOf.hasResults());
        assertFalse(resultsOf.hasErrors());
        assertNotNull(resultsOf.results());
        ConfigNode results = resultsOf.results();
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

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(Collections.singletonList(new ObjectToken("redis")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ObjectToken("name")), new ConfigValue("host1"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(resultsOf.hasResults());
        assertFalse(resultsOf.hasErrors());
        assertNotNull(resultsOf.results());
        ConfigNode results = resultsOf.results();
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

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(null, 0, false);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Empty or null token provided while building the config node", results.get(0).description());
    }

    @Test
    public void testValidateMultipleTokenTypes() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
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

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1)), new ConfigValue("host2")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNotNull(resultsOf.results());
        assertEquals("test", resultsOf.results().getKey("db").get().getKey("user").get().getValue().get());
        assertFalse(resultsOf.results().getKey("db").get().getKey("hosts").isPresent());
        List<ValidationError> results = resultsOf.getErrors();
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

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts")), new ConfigValue("hostDB1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB2"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Parsing path length errors for path: db.hosts, there could be several causes such as: duplicate paths, " +
            "or a node is both a leaf and an object", results.get(0).description());
    }

    @Test
    public void testValidateDuplicatePathLength() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts")), new ConfigValue("hostDB1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts")), new ConfigValue("hostDB2"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Parsing path length errors for path: db.hosts, there could be several causes such as: duplicate paths, " +
            "or a node is both a leaf and an object", results.get(0).description());
    }

    @Test
    public void testValidateMismatchedPathLengthWarnings() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts")), new ConfigValue("hostDB1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB2"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNotNull(resultsOf.results());
        assertEquals("test", resultsOf.results().getKey("db").get().getKey("user").get().getValue().get());
        assertFalse(resultsOf.results().getKey("db").get().getKey("hosts").isPresent());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Parsing path length errors for path: db.hosts, there could be several causes such as: duplicate paths, " +
            "or a node is both a leaf and an object", results.get(0).description());
    }

    @Test
    public void testValidateMismatchedPathLength2() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(Collections.singletonList(new ObjectToken("db")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts")), new ConfigValue("hostDB1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ObjectToken("name")), new ConfigValue("hostDB2"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Parsing path length errors for path: db, there could be several causes such as: duplicate paths, " +
            "or a node is both a leaf and an object", results.get(0).description());
    }

    @Test
    public void testValidateBadArrayIndex() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(-1)), new ConfigValue("host2"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);

        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Invalid array index: -1 for path: db.hosts", results.get(0).description());
    }

    @Test
    public void testValidateDuplicateArray() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host2"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Duplicate array index: 0 for path: db.hosts", results.get(0).description());
    }

    @Test
    public void testValidateMismatchedArrayPathLength() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("hostDB1")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ObjectToken("name")),
                new ConfigValue("hostDB2"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Array is both a leaf and non leaf with sizes: [4, 3] for path: db.hosts", results.get(0).description());
    }

    @Test
    public void testArrayIndexMissing() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0)), new ConfigValue("host0")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(2)), new ConfigValue("host2")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(4)), new ConfigValue("host4")),
            new Pair<>(List.of(new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("10"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, false);
        assertTrue(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNotNull(resultsOf.results());
        List<ValidationError> errors = resultsOf.getErrors();
        assertEquals(2, errors.size());
        assertEquals(ValidationLevel.MISSING_VALUE, errors.get(0).level());
        assertEquals("Missing array index: 1 for path: db.hosts", errors.get(0).description());
        assertEquals(ValidationLevel.MISSING_VALUE, errors.get(1).level());
        assertEquals("Missing array index: 3 for path: db.hosts", errors.get(1).description());

        ConfigNode results = resultsOf.results();
        assertEquals("host0", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(0).get().getValue().get());
        assertFalse(results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(1).isPresent());
        assertEquals("host2", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(2).get().getValue().get());
        assertFalse(results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(3).isPresent());
        assertEquals("host4", results.getKey("db").flatMap(db -> db.getKey("hosts")).get().getIndex(4).get().getValue().get());
    }

    @Test
    public void testValidConfigTreeArrayOfArraysDuplicateIndex() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ArrayToken(0)), new ConfigValue("host1")),

            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(0), new ArrayToken(0)), new ConfigValue("11")),

            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ArrayToken(0),
                new ObjectToken("name")), new ConfigValue("host1")),

            new Pair<>(List.of(
                new ObjectToken("db"), new ObjectToken("hosts"), new ArrayToken(1), new ArrayToken(1),
                new ObjectToken("port")), new ConfigValue("12")),

            new Pair<>(List.of(
                new ObjectToken("redis"), new ObjectToken("port")), new ConfigValue("10"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Duplicate array index: 0 for path: db.hosts[0]", results.get(0).description());
    }

    @Test
    public void testDuplicateArrayIndex() {
        MapConfigParser mapConfigParser = new MapConfigParser();
        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("admin"), new ArrayToken(0)), new ConfigValue("John")),
            new Pair<>(List.of(new ObjectToken("admin"), new ArrayToken(1)), new ConfigValue("Steve")),
            new Pair<>(List.of(new ObjectToken("admin"), new ArrayToken(1)), new ConfigValue("Gary"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertEquals("Duplicate array index: 1 for path: admin", results.get(0).description());
    }

    @Test
    public void testUnknownObjectType() {
        MapConfigParser mapConfigParser = new MapConfigParser();

        List<Pair<List<Token>, ConfigValue>> test = List.of(
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("connections")), new ConfigValue("10")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("user")), new ConfigValue("test")),
            new Pair<>(List.of(new ObjectToken("db"), new ObjectToken("password")), new ConfigValue("password")),
            new Pair<>(List.of(new ObjectToken("redis"), new OtherToken()), new ConfigValue("10"))
        );

        GResultOf<ConfigNode> resultsOf = mapConfigParser.buildConfigTree(test, 0, true);
        assertFalse(resultsOf.hasResults());
        assertTrue(resultsOf.hasErrors());
        assertNull(resultsOf.results());
        List<ValidationError> results = resultsOf.getErrors();
        assertEquals(1, results.size());
        assertEquals(ValidationLevel.ERROR, results.get(0).level());
        assertThat(results.get(0).description())
            .startsWith("Unknown token type")
            .contains("OtherToken");
    }

    public static class OtherToken extends Token {

    }
}
