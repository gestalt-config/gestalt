package org.github.gestalt.config.entity;

import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class ConfigNodeContainerTest {

    @Test
    void getConfigNode() {

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

        UUID id = UUID.randomUUID();

        ConfigNodeContainer cfgNode = new ConfigNodeContainer(root1, id);

        Assertions.assertEquals(root1, cfgNode.getConfigNode());
    }

    @Test
    void getId() {

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

        UUID id = UUID.randomUUID();

        ConfigNodeContainer cfgNode = new ConfigNodeContainer(root1, id);

        Assertions.assertEquals(id, cfgNode.getId());
    }

    @Test
    void testEquals() {
        ConfigNode[] arrayNode = new ConfigNode[2];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode = new HashMap<>();
        dbNode.put("name", new LeafNode("test"));
        dbNode.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root1Node = new HashMap<>();
        root1Node.put("db", new MapNode(dbNode));
        root1Node.put("admin", new ArrayNode(Arrays.asList(arrayNode)));

        ConfigNode[] arrayNode2 = new ConfigNode[2];
        arrayNode2[0] = new LeafNode("John");
        arrayNode2[1] = new LeafNode("Steve");

        Map<String, ConfigNode> dbNode2 = new HashMap<>();
        dbNode2.put("name", new LeafNode("test"));
        dbNode2.put("port", new LeafNode("3306"));

        Map<String, ConfigNode> root2Node = new HashMap<>();
        root2Node.put("db", new MapNode(dbNode2));
        root2Node.put("admin", new ArrayNode(Arrays.asList(arrayNode2)));
        ConfigNode root2 = new MapNode(root2Node);

        UUID id = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        ConfigNode root1 = new MapNode(root1Node);
        ConfigNodeContainer cfgNode = new ConfigNodeContainer(root1, id);
        ConfigNodeContainer cfgNode2 = new ConfigNodeContainer(root2, id2);
        ConfigNodeContainer cfgNode3 = new ConfigNodeContainer(root2, id);

        Assertions.assertEquals(cfgNode, cfgNode);
        Assertions.assertEquals(cfgNode, cfgNode3);
        Assertions.assertNotEquals(cfgNode, cfgNode2);
        Assertions.assertNotEquals(cfgNode, null);
        Assertions.assertNotEquals(cfgNode, Integer.valueOf(3));
    }

    @Test
    void testHashCode() {

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

        UUID id = UUID.fromString("9d4e9197-5898-45e6-9056-a4a29d2c2a64");

        ConfigNodeContainer cfgNode = new ConfigNodeContainer(root1, id);
        Assertions.assertEquals(-928228650, cfgNode.hashCode());
    }
}
