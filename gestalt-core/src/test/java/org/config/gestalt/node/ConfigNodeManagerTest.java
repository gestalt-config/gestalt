package org.config.gestalt.node;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.token.ArrayToken;
import org.config.gestalt.token.ObjectToken;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigNodeManagerTest {

    @Test
    public void testAddNode() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        Assertions.assertEquals("test", results.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results.getKey("db").get().getKey("port").get().getValue().get());

        Assertions.assertEquals("John", results.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Steve", results.getKey("admin").get().getIndex(1).get().getValue().get());
    }

    @Test
    public void testMergeNodes() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[1] = new LeafNode("Matt");
        arrayNode2[2] = new LeafNode("Paul");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("New Name"));
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertFalse(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("New Name", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("John", results2.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());
    }

    @Test
    public void testMergeArrayOfNodes() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        Map<String, ConfigNode> redisNode1 = new HashMap<>();
        redisNode1.put("uri", new LeafNode("redis1"));
        redisNode1.put("port", new LeafNode("1111"));

        Map<String, ConfigNode> redisNode2 = new HashMap<>();
        redisNode2.put("uri", new LeafNode("redis2"));
        redisNode2.put("port", new LeafNode("2222"));

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new MapNode(redisNode1);
        arrayNode[1] = new MapNode(redisNode2);

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        Map<String, ConfigNode> redisNode1a = new HashMap<>();
        redisNode1a.put("uri", new LeafNode("redis1a"));
        redisNode1a.put("port", new LeafNode("aaaa"));

        Map<String, ConfigNode> redisNode3 = new HashMap<>();
        redisNode3.put("uri", new LeafNode("redis3"));
        redisNode3.put("port", new LeafNode("3333"));

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[0] = new MapNode(redisNode1a);
        arrayNode2[2] = new MapNode(redisNode3);

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("New Name"));
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertFalse(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("New Name", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("redis1a", results2.getKey("admin").get().getIndex(0).get().getKey("uri").get().getValue().get());
        Assertions.assertEquals("aaaa", results2.getKey("admin").get().getIndex(0).get().getKey("port").get().getValue().get());
        Assertions.assertEquals("redis2", results2.getKey("admin").get().getIndex(1).get().getKey("uri").get().getValue().get());
        Assertions.assertEquals("2222", results2.getKey("admin").get().getIndex(1).get().getKey("port").get().getValue().get());
        Assertions.assertEquals("redis3", results2.getKey("admin").get().getIndex(2).get().getKey("uri").get().getValue().get());
        Assertions.assertEquals("3333", results2.getKey("admin").get().getIndex(2).get().getKey("port").get().getValue().get());
    }

    @Test
    public void testMergeArrayOfNodesMissingIndex() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        Map<String, ConfigNode> redisNode1 = new HashMap<>();
        redisNode1.put("uri", new LeafNode("redis1"));
        redisNode1.put("port", new LeafNode("1111"));

        Map<String, ConfigNode> redisNode2 = new HashMap<>();
        redisNode2.put("uri", new LeafNode("redis2"));
        redisNode2.put("port", new LeafNode("2222"));

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new MapNode(redisNode1);
        arrayNode[1] = new MapNode(redisNode2);

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Missing array index: 2 for path: admin", validateOfResults.getErrors().get(0).description());


        Map<String, ConfigNode> redisNode1a = new HashMap<>();
        redisNode1a.put("uri", new LeafNode("redis1a"));
        redisNode1a.put("port", new LeafNode("aaaa"));

        Map<String, ConfigNode> redisNode3 = new HashMap<>();
        redisNode3.put("uri", new LeafNode("redis3"));
        redisNode3.put("port", new LeafNode("3333"));

        ConfigNode[] arrayNode2 = new ConfigNode[4];
        arrayNode2[0] = new MapNode(redisNode1a);
        arrayNode2[3] = new MapNode(redisNode3);

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("New Name"));
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("New Name", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("redis1a", results2.getKey("admin").get().getIndex(0).get().getKey("uri").get().getValue().get());
        Assertions.assertEquals("aaaa", results2.getKey("admin").get().getIndex(0).get().getKey("port").get().getValue().get());
        Assertions.assertEquals("redis2", results2.getKey("admin").get().getIndex(1).get().getKey("uri").get().getValue().get());
        Assertions.assertEquals("2222", results2.getKey("admin").get().getIndex(1).get().getKey("port").get().getValue().get());
        Assertions.assertFalse(results2.getKey("admin").get().getIndex(2).isPresent());
        Assertions.assertEquals("redis3", results2.getKey("admin").get().getIndex(3).get().getKey("uri").get().getValue().get());
        Assertions.assertEquals("3333", results2.getKey("admin").get().getIndex(3).get().getKey("port").get().getValue().get());

        Assertions.assertEquals(1, validateOfResults2.getErrors().size());
        Assertions.assertEquals("Missing array index: 2 for path: admin", validateOfResults2.getErrors().get(0).description());
    }

    @Test
    public void testMergeArrayBadNodes() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        Map<String, ConfigNode> redisNode1 = new HashMap<>();
        redisNode1.put("uri", new LeafNode("redis1"));
        redisNode1.put("port", new LeafNode(null));

        Map<String, ConfigNode> redisNode2 = new HashMap<>();
        redisNode2.put("uri", new LeafNode("redis2"));
        redisNode2.put("port", new LeafNode("2222"));

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new MapNode(redisNode1);
        arrayNode[1] = new MapNode(redisNode2);

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        Assertions.assertEquals(2, validateOfResults.getErrors().size());

        assertThat(validateOfResults.getErrors())
            .anyMatch(it -> it.description().equals("Leaf nodes are empty for path: admin[0].port"))
            .anyMatch(it -> it.description().equals("Missing array index: 2 for path: admin"));

        Map<String, ConfigNode> redisNode1a = new HashMap<>();
        redisNode1a.put("uri", new LeafNode("redis1a"));
        redisNode1a.put("port", new LeafNode(null));

        Map<String, ConfigNode> redisNode3 = new HashMap<>();
        redisNode3.put("uri", new LeafNode("redis3"));
        redisNode3.put("port", new LeafNode("3333"));

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[0] = new MapNode(redisNode1a);
        arrayNode2[2] = new MapNode(redisNode3);

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("New Name"));
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("New Name", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("redis1a", results2.getKey("admin").get().getIndex(0).get().getKey("uri").get().getValue().get());
        Assertions.assertFalse(results2.getKey("admin").get().getIndex(0).get().getKey("port").get().getValue().isPresent());
        Assertions.assertEquals("redis2", results2.getKey("admin").get().getIndex(1).get().getKey("uri").get().getValue().get());
        Assertions.assertEquals("2222", results2.getKey("admin").get().getIndex(1).get().getKey("port").get().getValue().get());

        Assertions.assertEquals(2, validateOfResults2.getErrors().size());

        assertThat(validateOfResults2.getErrors())
            .anyMatch(it -> it.description().equals("Leaf nodes are empty for path: admin[0].port"))
            .anyMatch(it -> it.description().equals("Unable to find node matching path: admin[0], for class: MapNode"));
    }

    @Test
    public void testAddNullNode() {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        try {
            configNodeManager.addNode(null);
        } catch (Exception e) {
            Assertions.assertEquals("No node provided", e.getMessage());
        }
    }

    @Test
    public void testMerge3Nodes() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[1] = new LeafNode("Scott");
        arrayNode2[2] = new LeafNode("Paul");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("New Name"));
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertFalse(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());


        ConfigNode[] arrayNode3 = new ConfigNode[1];
        arrayNode3[0] = new LeafNode("Matt");

        Map<String, ConfigNode> dbNode3 = new HashMap<>();
        dbNode3.put("name", new LeafNode("New Name"));
        dbNode3.put("timeout", new LeafNode("5000"));

        Map<String, ConfigNode> rootNode3 = new HashMap<>();
        rootNode3.put("db", new MapNode(dbNode3));
        rootNode3.put("admin", new ArrayNode(Arrays.asList(arrayNode3)));
        ConfigNode root3 = new MapNode(rootNode3);

        ValidateOf<ConfigNode> validateOfResults3 = configNodeManager.addNode(root3);
        Assertions.assertFalse(validateOfResults3.hasErrors());
        Assertions.assertTrue(validateOfResults3.hasResults());
        Assertions.assertNotNull(validateOfResults3.results());

        ConfigNode results3 = validateOfResults3.results();
        Assertions.assertEquals(2, results3.size());

        Assertions.assertEquals("New Name", results3.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results3.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results3.getKey("db").get().getKey("password").get().getValue().get());
        Assertions.assertEquals("5000", results3.getKey("db").get().getKey("timeout").get().getValue().get());

        Assertions.assertEquals("Matt", results3.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Scott", results3.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results3.getKey("admin").get().getIndex(2).get().getValue().get());
    }

    @Test
    public void testMergeMismatchedNodes() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("New Name"));
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new LeafNode("test"));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());

        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("New Name", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("leaf", results2.getKey("admin").get().getNodeType().getType());

        Assertions.assertEquals("Unable to merge different nodes, of type: ArrayNode and type: LeafNode",
            validateOfResults2.getErrors().get(0).description());
    }


    @Test
    public void testMergeEmptyLeaf() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[1] = new LeafNode("Matt");
        arrayNode2[2] = new LeafNode("Paul");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode(null));
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertFalse(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("test", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("John", results2.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());

        Assertions.assertEquals(0, validateOfResults2.getErrors().size());
    }

    @Test
    public void testMerge2EmptyLeaves() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode(null));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Leaf nodes are empty for path: db.name", validateOfResults.getErrors().get(0).description());

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[1] = new LeafNode("Matt");
        arrayNode2[2] = new LeafNode("Paul");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode(null));
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertFalse(results2.getKey("db").get().getKey("name").get().getValue().isPresent());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("John", results2.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());

        Assertions.assertEquals(2, validateOfResults2.getErrors().size());
        assertThat(validateOfResults2.getErrors())
            .anyMatch(it -> it.description().equals("Unable to find node matching path: db, for class: MapNode"))
            .anyMatch(it -> it.description().equals("Leaf nodes are empty for path: db.name"));
    }

    @Test
    public void testMergeEmptyLeafNode() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[1] = new LeafNode("Matt");
        arrayNode2[2] = new LeafNode("Paul");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", null);
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("test", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("John", results2.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());

        Assertions.assertEquals(1, validateOfResults2.getErrors().size());
        Assertions.assertEquals("Empty node value provided for path: db.name",
            validateOfResults2.getErrors().get(0).description());
    }

    @Test
    public void testMerge2EmptyLeavesNodes() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", null);
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Empty node value provided for path: db.name", validateOfResults.getErrors().get(0).description());

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[1] = new LeafNode("Matt");
        arrayNode2[2] = new LeafNode("Paul");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", null);
        dbNode2.put("password", new LeafNode("123abc"));
        dbNode2.put("autoCommit", null);

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertFalse(results2.getKey("db").get().getKey("name").isPresent());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("John", results2.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());

        Assertions.assertEquals(2, validateOfResults2.getErrors().size());
        assertThat(validateOfResults2.getErrors())
            .anyMatch(it -> it.description().equals("Empty node value provided for path: db.name"))
            .anyMatch(it -> it.description().equals("Empty node value provided for path: db.autoCommit"));
    }

    @Test
    public void testMergeEmptyKey() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[1] = new LeafNode("Matt");
        arrayNode2[2] = new LeafNode("Paul");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("port", new LeafNode("1234"));
        dbNode2.put(null, new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("test", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("1234", results2.getKey("db").get().getKey("port").get().getValue().get());

        Assertions.assertEquals("John", results2.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());

        Assertions.assertEquals(1, validateOfResults2.getErrors().size());
        Assertions.assertEquals("Empty node name provided for path: db",
            validateOfResults2.getErrors().get(0).description());
    }

    @Test
    public void testMerge2EmptyKey() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));
        dbNode.put(null, new LeafNode("123abc"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Empty node name provided for path: db", validateOfResults.getErrors().get(0).description());

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[1] = new LeafNode("Matt");
        arrayNode2[2] = new LeafNode("Paul");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("port", new LeafNode("1234"));
        dbNode2.put(null, new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("test", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("1234", results2.getKey("db").get().getKey("port").get().getValue().get());

        Assertions.assertEquals("John", results2.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());

        Assertions.assertEquals(1, validateOfResults2.getErrors().size());
        Assertions.assertEquals("Empty node name provided for path: db", validateOfResults2.getErrors().get(0).description());
    }

    @Test
    public void testMergeEmptyArray() throws GestaltException {

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode(null);
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Leaf nodes are empty for path: admin[0]", validateOfResults.getErrors().get(0).description());

        ConfigNode[] arrayNode2 = new ConfigNode[3];
        arrayNode2[1] = new LeafNode("Matt");
        arrayNode2[2] = new LeafNode(null);

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("New Name"));
        dbNode2.put("password", new LeafNode("123abc"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(root2);
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Leaf nodes are empty for path: admin[0]", validateOfResults.getErrors().get(0).description());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("New Name", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertFalse(results2.getKey("admin").get().getIndex(0).get().getValue().isPresent());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertFalse(results2.getKey("admin").get().getIndex(2).get().getValue().isPresent());
    }


    @Test
    public void testNavigateToNode() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("name"));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.name", tokens);

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        ConfigNode configNode = navigateValidate.results();
        Assertions.assertEquals("test", configNode.getValue().get());

        tokens = Arrays.asList(new ObjectToken("admin"), new ArrayToken(0));
        navigateValidate = configNodeManager.navigateToNode("admin[0]", tokens);

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        configNode = navigateValidate.results();
        Assertions.assertEquals("John", configNode.getValue().get());
    }

    @Test
    public void testNavigateToInvalidArrayIndex() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("admin"), new ArrayToken(2));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("admin[0]", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find array node for path: admin[0], at token: ArrayToken",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToInvalidMapNode() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("password"));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.password", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find object node for path: db.password, at token: ObjectToken",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToMismatchedNodeMap() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("admin"), new ObjectToken("user"));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("admin.user", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Mismatched Nodes on path: admin.user, expected: MapNode received: ArrayNode",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToMismatchedNodeArray() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ArrayToken(0));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db[0]", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Mismatched Nodes on path: db[0], expected: ArrayNode received: MapNode",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToInvalidToken() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new TestToken());
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.test", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("unsupported token: TestToken for path: db.test",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullToken() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), null);
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.test", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Null Token on path: db.test",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullTokenValue() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken(null));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.test", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find object node for path: db.test, at token: ObjectToken",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullLeafToken() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode(null));
        dbNode.put("port", null);

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("name"));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.name", tokens);

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        Assertions.assertFalse(navigateValidate.results().getValue().isPresent());


        tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("port"));
        navigateValidate = configNodeManager.navigateToNode("db.port", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find object node for path: db.port, at token: ObjectToken",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullArrayToken() throws GestaltException {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode(null));
        dbNode.put("port", null);

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);


        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(root1);
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("admin"), new ArrayToken(2));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("array[2]", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find array node for path: array[2], at token: ArrayToken",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullRoot() {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new TestToken());
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.test", tokens);

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Null Nodes on path: db.test", navigateValidate.getErrors().get(0).description());
    }

    public static class TestToken extends Token {

        public TestToken() {
        }
    }
}

