package org.github.gestalt.config.node;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.TestSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigNodeManagerTest {

    @Test
    public void testAddNode() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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
    public void testAddNodeTags() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults =
            configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource(Tags.of("toy", "ball"))));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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
    public void testMergeNodesSameTags() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults =
            configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource(Tags.of("toy", "ball"))));
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

        ValidateOf<ConfigNode> validateOfResults2 =
            configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource(Tags.of("toy", "ball"))));
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
    public void testMergeNodesDifferentTags() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults =
            configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource(Tags.of("toy", "ball"))));
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

        ValidateOf<ConfigNode> validateOfResults2 =
            configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource(Tags.of("toy", "car"))));
        Assertions.assertTrue(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());

        Assertions.assertEquals(1, validateOfResults2.getErrors().size());
        Assertions.assertEquals("Missing array index: 0 for path: admin", validateOfResults2.getErrors().get(0).description());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("New Name", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertTrue(results2.getKey("db").get().getKey("port").isEmpty());
        Assertions.assertEquals("123abc", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertTrue(results2.getKey("admin").get().getIndex(0).isEmpty());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());
    }

    @Test
    public void testMergeArrayOfNodes() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        Assertions.assertEquals(2, validateOfResults.getErrors().size());

        assertThat(validateOfResults.getErrors())
            .anyMatch(it -> "Leaf nodes are empty for path: admin[0].port".equals(it.description()))
            .anyMatch(it -> "Missing array index: 2 for path: admin".equals(it.description()));

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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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
            .anyMatch(it -> "Leaf nodes are empty for path: admin[0].port".equals(it.description()))
            .anyMatch(it -> "Unable to find node matching path: admin[0], for class: MapNode, during merging maps"
                .equals(it.description()));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults3 = configNodeManager.addNode(new ConfigNodeContainer(root3, new TestSource()));
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
    public void testMerge3NodesTags() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults =
            configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource(Tags.of("toy", "ball"))));
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

        ValidateOf<ConfigNode> validateOfResults2 =
            configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource(Tags.of("toy", "ball"))));
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

        ValidateOf<ConfigNode> validateOfResults3 =
            configNodeManager.addNode(new ConfigNodeContainer(root3, new TestSource()));
        Assertions.assertFalse(validateOfResults3.hasErrors());
        Assertions.assertTrue(validateOfResults3.hasResults());
        Assertions.assertNotNull(validateOfResults3.results());

        ConfigNode results3 = validateOfResults3.results();
        Assertions.assertEquals(2, results3.size());

        Assertions.assertEquals("New Name", results3.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertTrue(results3.getKey("db").get().getKey("port").isEmpty());
        Assertions.assertTrue(results3.getKey("db").get().getKey("password").isEmpty());
        Assertions.assertEquals("5000", results3.getKey("db").get().getKey("timeout").get().getValue().get());

        Assertions.assertEquals(1, results3.getKey("admin").get().size());
        Assertions.assertEquals("Matt", results3.getKey("admin").get().getIndex(0).get().getValue().get());
    }

    @Test
    public void testMergeMismatchedNodes() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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
            .anyMatch(it -> "Unable to find node matching path: db, for class: MapNode, during merging maps".equals(it.description()))
            .anyMatch(it -> "Leaf nodes are empty for path: db.name".equals(it.description()));
    }

    @Test
    public void testMergeEmptyLeafNode() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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
            .anyMatch(it -> "Empty node value provided for path: db.name".equals(it.description()))
            .anyMatch(it -> "Empty node value provided for path: db.autoCommit".equals(it.description()));
    }

    @Test
    public void testMergeEmptyKey() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource()));
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("name"));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.name", tokens, Tags.of());

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        ConfigNode configNode = navigateValidate.results();
        Assertions.assertEquals("test", configNode.getValue().get());

        tokens = Arrays.asList(new ObjectToken("admin"), new ArrayToken(0));
        navigateValidate = configNodeManager.navigateToNode("admin[0]", tokens, Tags.of());

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        configNode = navigateValidate.results();
        Assertions.assertEquals("John", configNode.getValue().get());
    }

    @Test
    public void testNavigateToNodeTagged() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults =
            configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode[] arrayNode2 = new ConfigNode[2];
        arrayNode2[0] = new LeafNode("Matt");
        arrayNode2[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("New Name"));
        dbNode2.put("password", new LeafNode("123abc"));
        dbNode2.put("poolSize", new LeafNode("8"));

        Map<String, ConfigNode> rootNode2 = new HashMap<>();
        rootNode2.put("db", new MapNode(dbNode2));
        rootNode2.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(rootNode2);

        ValidateOf<ConfigNode> validateOfResults2 =
            configNodeManager.addNode(new ConfigNodeContainer(root2, new TestSource(Tags.of("toy", "ball"))));
        Assertions.assertFalse(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());


        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());


        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("name"));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.name", tokens, Tags.of());

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        ConfigNode configNode = navigateValidate.results();
        Assertions.assertEquals("test", configNode.getValue().get());

        navigateValidate = configNodeManager.navigateToNode("db.name", tokens, Tags.of("toy", "ball"));

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        configNode = navigateValidate.results();
        Assertions.assertEquals("New Name", configNode.getValue().get());

        tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("poolSize"));
        navigateValidate = configNodeManager.navigateToNode("db.poolSize", tokens, Tags.of("toy", "car"));

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals(1, navigateValidate.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: db.poolSize, for class: ObjectToken, during navigating to next node",
            navigateValidate.getErrors().get(0).description());

        tokens = Arrays.asList(new ObjectToken("admin"), new ArrayToken(0));
        navigateValidate = configNodeManager.navigateToNode("admin[0]", tokens, Tags.of());

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        configNode = navigateValidate.results();
        Assertions.assertEquals("John", configNode.getValue().get());

        navigateValidate = configNodeManager.navigateToNode("admin[0]", tokens, Tags.of("toy", "ball"));

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        configNode = navigateValidate.results();
        Assertions.assertEquals("Matt", configNode.getValue().get());
    }

    @Test
    public void testNavigateToInvalidArrayIndex() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("admin"), new ArrayToken(2));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("admin[0]", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find node matching path: admin[0], for class: ArrayToken, during navigating to next node",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToInvalidMapNode() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("password"));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.password", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find node matching path: db.password, for class: ObjectToken, during navigating to next node",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToMismatchedNodeMap() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("admin"), new ObjectToken("user"));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("admin.user", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Mismatched Nodes on path: admin.user, expected: MapNode received: ArrayNode",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToMismatchedNodeArray() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ArrayToken(0));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db[0]", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Mismatched Nodes on path: db[0], expected: ArrayNode received: MapNode",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToInvalidToken() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new TestToken());
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.test", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("unsupported token: TestToken for path: db.test",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullToken() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), null);
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.test", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Null or Empty Token on path: db.test",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullTokenValue() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken(null));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.test", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find node matching path: db.test, for class: ObjectToken, during navigating to next node",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullLeafToken() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("name"));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.name", tokens, Tags.of());

        Assertions.assertFalse(navigateValidate.hasErrors());
        Assertions.assertTrue(navigateValidate.hasResults());

        Assertions.assertFalse(navigateValidate.results().getValue().isPresent());


        tokens = Arrays.asList(new ObjectToken("db"), new ObjectToken("port"));
        navigateValidate = configNodeManager.navigateToNode("db.port", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find node matching path: db.port, for class: ObjectToken, during navigating to next node",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullArrayToken() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
        Assertions.assertTrue(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        List<Token> tokens = Arrays.asList(new ObjectToken("admin"), new ArrayToken(2));
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("array[2]", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Unable to find node matching path: array[2], for class: ArrayToken, during navigating to next node",
            navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testNavigateToNullRoot() {
        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        List<Token> tokens = Arrays.asList(new ObjectToken("db"), new TestToken());
        ValidateOf<ConfigNode> navigateValidate = configNodeManager.navigateToNode("db.test", tokens, Tags.of());

        Assertions.assertTrue(navigateValidate.hasErrors());
        Assertions.assertFalse(navigateValidate.hasResults());

        Assertions.assertEquals("Null Nodes on path: db.test", navigateValidate.getErrors().get(0).description());
    }

    @Test
    public void testReloadNode() throws GestaltException {
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

        TestSource source = new TestSource(UUID.randomUUID());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, source));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        ConfigNode results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        Assertions.assertEquals("test", results.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results.getKey("db").get().getKey("port").get().getValue().get());

        Assertions.assertEquals("John", results.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Steve", results.getKey("admin").get().getIndex(1).get().getValue().get());

        // reload
        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("test2"));
        dbNode2.put("port", new LeafNode("33061"));
        Map<String, ConfigNode> root2Node = new HashMap<>();
        root2Node.put("db", new MapNode(dbNode2));
        root2Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root2 = new MapNode(root2Node);
        validateOfResults = configNodeManager.reloadNode(new ConfigNodeContainer(root2, new TestSource()));

        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        results = validateOfResults.results();
        Assertions.assertEquals(2, results.size());

        Assertions.assertEquals("test", results.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results.getKey("db").get().getKey("port").get().getValue().get());

        Assertions.assertEquals("John", results.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Steve", results.getKey("admin").get().getIndex(1).get().getValue().get());
    }

    @Test
    public void testMergeNodesReload() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ValidateOf<ConfigNode> validateOfResults = configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));
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

        ConfigSource s2 = new TestSource();
        ValidateOf<ConfigNode> validateOfResults2 = configNodeManager.addNode(new ConfigNodeContainer(root2, s2));
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

        ConfigNode[] arrayNode2reload = new ConfigNode[4];
        arrayNode2reload[1] = new LeafNode("Matt");
        arrayNode2reload[2] = new LeafNode("Paul");
        arrayNode2reload[3] = new LeafNode("June");

        Map<String, ConfigNode> dbNode2Reload = new HashMap<>();
        dbNode2Reload.put("name", new LeafNode("New Name2"));
        dbNode2Reload.put("password", new LeafNode("123abcefg"));

        Map<String, ConfigNode> rootNode2Reload = new HashMap<>();
        rootNode2Reload.put("db", new MapNode(dbNode2Reload));
        rootNode2Reload.put("admin", new ArrayNode(Arrays.asList(arrayNode2reload)));
        ConfigNode root2Reload = new MapNode(rootNode2Reload);

        validateOfResults2 = configNodeManager.reloadNode(new ConfigNodeContainer(root2Reload, s2));
        Assertions.assertFalse(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());

        results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("New Name2", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abcefg", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("John", results2.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());
        Assertions.assertEquals("June", results2.getKey("admin").get().getIndex(3).get().getValue().get());
    }

    @Test
    public void testMerge3NodesTagsThenReload() throws GestaltException {
        // create source 1
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

        // add source 1 to the configNodeManager with tags
        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        ConfigSource s1 = new TestSource(Tags.of("toy", "ball"));
        ValidateOf<ConfigNode> validateOfResults =
            configNodeManager.addNode(new ConfigNodeContainer(root1, s1));
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertNotNull(validateOfResults.results());

        // create source 2
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

        // add source 2 to the configNodeManager with tags
        ConfigSource s2 = new TestSource(Tags.of("toy", "ball"));
        ValidateOf<ConfigNode> validateOfResults2 =
            configNodeManager.addNode(new ConfigNodeContainer(root2, s2));
        Assertions.assertFalse(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());

        ConfigNode results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        // create source 2
        ConfigNode[] arrayNode3 = new ConfigNode[1];
        arrayNode3[0] = new LeafNode("Matt");

        Map<String, ConfigNode> dbNode3 = new HashMap<>();
        dbNode3.put("name", new LeafNode("New Name"));
        dbNode3.put("timeout", new LeafNode("5000"));

        Map<String, ConfigNode> rootNode3 = new HashMap<>();
        rootNode3.put("db", new MapNode(dbNode3));
        rootNode3.put("admin", new ArrayNode(Arrays.asList(arrayNode3)));
        ConfigNode root3 = new MapNode(rootNode3);

        // add source 2 to the configNodeManager without tags
        ConfigSource s3 = new TestSource();
        ValidateOf<ConfigNode> validateOfResults3 =
            configNodeManager.addNode(new ConfigNodeContainer(root3, s3));
        Assertions.assertFalse(validateOfResults3.hasErrors());
        Assertions.assertTrue(validateOfResults3.hasResults());
        Assertions.assertNotNull(validateOfResults3.results());

        ConfigNode results3 = validateOfResults3.results();
        Assertions.assertEquals(2, results3.size());

        //validate results are not merged with the tagged results.
        Assertions.assertEquals("New Name", results3.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertTrue(results3.getKey("db").get().getKey("port").isEmpty());
        Assertions.assertTrue(results3.getKey("db").get().getKey("password").isEmpty());
        Assertions.assertEquals("5000", results3.getKey("db").get().getKey("timeout").get().getValue().get());

        Assertions.assertEquals(1, results3.getKey("admin").get().size());
        Assertions.assertEquals("Matt", results3.getKey("admin").get().getIndex(0).get().getValue().get());

        // update source 2
        ConfigNode[] arrayNode2reload = new ConfigNode[4];
        arrayNode2reload[1] = new LeafNode("Matt");
        arrayNode2reload[2] = new LeafNode("Paul");
        arrayNode2reload[3] = new LeafNode("June");

        Map<String, ConfigNode> dbNode2Reload = new HashMap<>();
        dbNode2Reload.put("name", new LeafNode("New Name2"));
        dbNode2Reload.put("password", new LeafNode("123abcefg"));

        Map<String, ConfigNode> rootNode2Reload = new HashMap<>();
        rootNode2Reload.put("db", new MapNode(dbNode2Reload));
        rootNode2Reload.put("admin", new ArrayNode(Arrays.asList(arrayNode2reload)));
        ConfigNode root2Reload = new MapNode(rootNode2Reload);

        // reload source 2 with tags.
        validateOfResults2 = configNodeManager.reloadNode(new ConfigNodeContainer(root2Reload, s2));
        Assertions.assertFalse(validateOfResults2.hasErrors());
        Assertions.assertTrue(validateOfResults2.hasResults());
        Assertions.assertNotNull(validateOfResults2.results());

        // validate we merge with the tag nodes, but not the untagged nodes.
        results2 = validateOfResults2.results();
        Assertions.assertEquals(2, results2.size());

        Assertions.assertEquals("New Name2", results2.getKey("db").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("3306", results2.getKey("db").get().getKey("port").get().getValue().get());
        Assertions.assertEquals("123abcefg", results2.getKey("db").get().getKey("password").get().getValue().get());

        Assertions.assertEquals("John", results2.getKey("admin").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Matt", results2.getKey("admin").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Paul", results2.getKey("admin").get().getIndex(2).get().getValue().get());
        Assertions.assertEquals("June", results2.getKey("admin").get().getIndex(3).get().getValue().get());
    }

    @Test
    public void testPostProcessor() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));

        ValidateOf<Boolean> validateOf = configNodeManager.postProcess(Arrays.asList(new TestPostProcessor("abc"),
            new TestPostProcessor("def")));
        Assertions.assertFalse(validateOf.hasErrors());
        Assertions.assertTrue(validateOf.hasResults());
        Assertions.assertNotNull(validateOf.results());

        Boolean results = validateOf.results();

        Assertions.assertTrue(results);

        Assertions.assertEquals("test abc def",
            configNodeManager.navigateToNode("db.name", Arrays.asList(new ObjectToken("db"), new ObjectToken("name")), Tags.of())
                             .results().getValue().get());
        Assertions.assertEquals("3306 abc def",
            configNodeManager.navigateToNode("db.port", Arrays.asList(new ObjectToken("db"), new ObjectToken("port")), Tags.of())
                             .results().getValue().get());

        Assertions.assertEquals("John abc def",
            configNodeManager.navigateToNode("admin", Arrays.asList(new ObjectToken("admin"), new ArrayToken(0)), Tags.of())
                             .results().getValue().get());
        Assertions.assertEquals("Steve abc def",
            configNodeManager.navigateToNode("admin", Arrays.asList(new ObjectToken("admin"), new ArrayToken(1)), Tags.of())
                             .results().getValue().get());
    }

    @Test
    public void testPostProcessorNullProcessors() {

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        GestaltException e = Assertions.assertThrows(GestaltException.class, () -> configNodeManager.postProcess(null));

        Assertions.assertEquals("No postProcessors provided", e.getMessage());
    }

    @Test
    public void testPostProcessorEmpty() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));

        ValidateOf<Boolean> validateOf = configNodeManager.postProcess(Collections.emptyList());
        Assertions.assertFalse(validateOf.hasErrors());
        Assertions.assertTrue(validateOf.hasResults());
        Assertions.assertNotNull(validateOf.results());

        Boolean results = validateOf.results();

        Assertions.assertTrue(results);

        Assertions.assertEquals("test",
            configNodeManager.navigateToNode("db.name", Arrays.asList(new ObjectToken("db"), new ObjectToken("name")), Tags.of())
                             .results().getValue().get());
        Assertions.assertEquals("3306",
            configNodeManager.navigateToNode("db.port", Arrays.asList(new ObjectToken("db"), new ObjectToken("port")), Tags.of())
                             .results().getValue().get());

        Assertions.assertEquals("John",
            configNodeManager.navigateToNode("admin", Arrays.asList(new ObjectToken("admin"), new ArrayToken(0)), Tags.of())
                             .results().getValue().get());
        Assertions.assertEquals("Steve",
            configNodeManager.navigateToNode("admin", Arrays.asList(new ObjectToken("admin"), new ArrayToken(1)), Tags.of())
                             .results().getValue().get());
    }

    @Test
    public void testPostProcessorErrors() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));

        ValidateOf<Boolean> validateOf = configNodeManager.postProcess(Arrays.asList(new TestPostProcessorErrors(),
            new TestPostProcessor("abc")));
        Assertions.assertTrue(validateOf.hasErrors());
        Assertions.assertTrue(validateOf.hasResults());
        Assertions.assertNotNull(validateOf.results());

        Boolean results = validateOf.results();

        Assertions.assertTrue(results);

        Assertions.assertEquals("test abc",
            configNodeManager.navigateToNode("db.name", Arrays.asList(new ObjectToken("db"), new ObjectToken("name")), Tags.of())
                             .results().getValue().get());
        Assertions.assertEquals("3306 abc",
            configNodeManager.navigateToNode("db.port", Arrays.asList(new ObjectToken("db"), new ObjectToken("port")), Tags.of())
                             .results().getValue().get());

        Assertions.assertEquals("John abc",
            configNodeManager.navigateToNode("admin", Arrays.asList(new ObjectToken("admin"), new ArrayToken(0)), Tags.of())
                             .results().getValue().get());
        Assertions.assertEquals("Steve abc",
            configNodeManager.navigateToNode("admin", Arrays.asList(new ObjectToken("admin"), new ArrayToken(1)), Tags.of())
                             .results().getValue().get());


        Assertions.assertEquals(4, validateOf.getErrors().size());
        assertThat(validateOf.getErrors().get(0).description()).startsWith("Leaf nodes are empty for path: ");
    }

    @Test
    public void testPostProcessorNoResults() throws GestaltException {
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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));

        ValidateOf<Boolean> validateOf = configNodeManager.postProcess(Arrays.asList(new TestPostProcessorNoResults(),
            new TestPostProcessor("abc")));
        Assertions.assertTrue(validateOf.hasErrors());
        Assertions.assertTrue(validateOf.hasResults());
        Assertions.assertNotNull(validateOf.results());

        Boolean results = validateOf.results();

        Assertions.assertTrue(results);

        Assertions.assertEquals(4, validateOf.getErrors().size());
        assertThat(validateOf.getErrors().get(0).description()).startsWith("Unable to find node matching path");
    }

    @Test
    public void testPostProcessorUnknownTokenArray() throws GestaltException {
        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new TestNode();

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));

        ValidateOf<Boolean> validateOf = configNodeManager.postProcess(List.of(new TestPostProcessor("abc")));
        Assertions.assertTrue(validateOf.hasErrors());
        Assertions.assertTrue(validateOf.hasResults());
        Assertions.assertNotNull(validateOf.results());

        Boolean results = validateOf.results();

        Assertions.assertTrue(results);

        Assertions.assertEquals("test abc",
            configNodeManager.navigateToNode("db.name", Arrays.asList(new ObjectToken("db"), new ObjectToken("name")), Tags.of())
                             .results().getValue().get());
        Assertions.assertEquals("3306 abc",
            configNodeManager.navigateToNode("db.port", Arrays.asList(new ObjectToken("db"), new ObjectToken("port")), Tags.of())
                             .results().getValue().get());

        Assertions.assertEquals("John abc",
            configNodeManager.navigateToNode("admin", Arrays.asList(new ObjectToken("admin"), new ArrayToken(0)), Tags.of())
                             .results().getValue().get());

        Assertions.assertEquals(2, validateOf.getErrors().size());
        Assertions.assertEquals("Unknown node type: org.github.gestalt.config.node.ConfigNodeManagerTest$TestNode " +
                "on Path: admin[1] while post processing",
            validateOf.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: admin, for class: ArrayNode, during post processing",
            validateOf.getErrors().get(1).description());
    }

    @Test
    public void testPostProcessorUnknownTokenMap() throws GestaltException {
        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new TestNode());

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));
        ConfigNode root1 = new MapNode(root1Node);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        configNodeManager.addNode(new ConfigNodeContainer(root1, new TestSource()));

        ValidateOf<Boolean> validateOf = configNodeManager.postProcess(List.of(new TestPostProcessor("abc")));
        Assertions.assertTrue(validateOf.hasErrors());
        Assertions.assertTrue(validateOf.hasResults());
        Assertions.assertNotNull(validateOf.results());

        Boolean results = validateOf.results();

        Assertions.assertTrue(results);


        Assertions.assertEquals("test abc",
            configNodeManager.navigateToNode("db.name", Arrays.asList(new ObjectToken("db"), new ObjectToken("name")), Tags.of())
                             .results().getValue().get());

        Assertions.assertEquals("John abc",
            configNodeManager.navigateToNode("admin", Arrays.asList(new ObjectToken("admin"), new ArrayToken(0)), Tags.of())
                             .results().getValue().get());
        Assertions.assertEquals("Steve abc",
            configNodeManager.navigateToNode("admin", Arrays.asList(new ObjectToken("admin"), new ArrayToken(1)), Tags.of())
                             .results().getValue().get());

        Assertions.assertEquals(2, validateOf.getErrors().size());
        Assertions.assertEquals("Unknown node type: org.github.gestalt.config.node.ConfigNodeManagerTest$TestNode " +
                "on Path: db.port while post processing",
            validateOf.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: db, for class: MapNode, during post processing",
            validateOf.getErrors().get(1).description());
    }

    public static class TestToken extends Token {

        public TestToken() {
        }
    }

    public static class TestNode implements ConfigNode {

        @Override
        public NodeType getNodeType() {
            return null;
        }

        @Override
        public Optional<String> getValue() {
            return Optional.of("testNode");
        }

        @Override
        public Optional<ConfigNode> getIndex(int index) {
            return Optional.empty();
        }

        @Override
        public Optional<ConfigNode> getKey(String key) {
            return Optional.empty();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    public static class TestPostProcessor implements PostProcessor {
        private final String add;

        public TestPostProcessor(String add) {
            this.add = add;
        }

        @Override
        public ValidateOf<ConfigNode> process(String path, ConfigNode currentNode) {
            if (currentNode instanceof LeafNode) {
                return ValidateOf.valid(new LeafNode(currentNode.getValue().get() + " " + add));
            }
            return ValidateOf.valid(currentNode);
        }
    }

    public static class TestPostProcessorErrors implements PostProcessor {
        public TestPostProcessorErrors() {
        }

        @Override
        public ValidateOf<ConfigNode> process(String path, ConfigNode currentNode) {
            if (currentNode instanceof LeafNode) {
                return ValidateOf.validateOf(currentNode,
                        List.of(new ValidationError.LeafNodesHaveNoValues(currentNode.getValue().get())));
            }
            return ValidateOf.valid(currentNode);
        }
    }

    public static class TestPostProcessorNoResults implements PostProcessor {
        public TestPostProcessorNoResults() {
        }

        @Override
        public ValidateOf<ConfigNode> process(String path, ConfigNode currentNode) {
            if (currentNode instanceof LeafNode) {
                return ValidateOf.valid(null);
            }
            return ValidateOf.valid(currentNode);
        }
    }
}

