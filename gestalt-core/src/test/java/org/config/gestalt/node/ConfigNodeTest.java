package org.config.gestalt.node;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class ConfigNodeTest {

    @Test
    void getNodeType() {
        ArrayNode arrayNode = new ArrayNode(Collections.singletonList(new LeafNode("a")));
        Map<String, ConfigNode> mapNode = new HashMap<>();
        mapNode.put("test", new LeafNode("leaf"));
        MapNode objectNode = new MapNode(mapNode);
        LeafNode leaf = new LeafNode("leaf");

        Assertions.assertEquals(NodeType.ARRAY, arrayNode.getNodeType());
        Assertions.assertEquals(NodeType.MAP, objectNode.getNodeType());
        Assertions.assertEquals(NodeType.LEAF, leaf.getNodeType());
        Assertions.assertEquals(NodeType.ARRAY.getType(), arrayNode.getNodeType().getType());
    }

    @Test
    void getValue() {
        ArrayNode arrayNode = new ArrayNode(Collections.singletonList(new LeafNode("a")));
        Map<String, ConfigNode> mapNode = new HashMap<>();
        mapNode.put("test", new LeafNode("leaf"));
        MapNode objectNode = new MapNode(mapNode);
        LeafNode leaf = new LeafNode("leaf");

        Assertions.assertEquals(Optional.empty(), arrayNode.getValue());
        Assertions.assertEquals(Optional.empty(), objectNode.getValue());
        Assertions.assertEquals("leaf", leaf.getValue().orElse(""));
    }

    @Test
    void getIndex() {
        ArrayNode arrayNode = new ArrayNode(Collections.singletonList(new LeafNode("a")));
        Map<String, ConfigNode> mapNode = new HashMap<>();
        mapNode.put("test", new LeafNode("leaf"));
        MapNode objectNode = new MapNode(mapNode);
        LeafNode leaf = new LeafNode("leaf");

        Assertions.assertEquals("a", arrayNode.getIndex(0).flatMap(ConfigNode::getValue).orElse(""));
        Assertions.assertEquals(Optional.empty(), objectNode.getIndex(0));
        Assertions.assertEquals(Optional.empty(), leaf.getIndex(0));
    }

    @Test
    void getKey() {
        ArrayNode arrayNode = new ArrayNode(Collections.singletonList(new LeafNode("a")));
        Map<String, ConfigNode> mapNode = new HashMap<>();
        mapNode.put("test", new LeafNode("leaf"));
        MapNode objectNode = new MapNode(mapNode);
        LeafNode leaf = new LeafNode("leaf");

        Assertions.assertEquals(Optional.empty(), arrayNode.getKey("test"));
        Assertions.assertEquals("leaf", objectNode.getKey("test").flatMap(ConfigNode::getValue).orElse(""));
        Assertions.assertEquals(Optional.empty(), leaf.getKey("test"));
    }

    @Test
    void size() {
        ArrayNode arrayNode = new ArrayNode(Arrays.asList(new LeafNode("a"), new LeafNode("b")));
        Map<String, ConfigNode> mapNode = new HashMap<>();
        mapNode.put("test", new LeafNode("leaf"));
        mapNode.put("test2", new LeafNode("leaf2"));
        MapNode objectNode = new MapNode(mapNode);
        LeafNode leaf = new LeafNode("leaf");

        Assertions.assertEquals(2, arrayNode.size());
        Assertions.assertEquals(2, objectNode.size());
        Assertions.assertEquals(1, leaf.size());
    }

    @Test
    void testEquals() {
        ArrayNode arrayNode = new ArrayNode(Collections.singletonList(new LeafNode("a")));
        ArrayNode arrayNodeSame = new ArrayNode(Collections.singletonList(new LeafNode("a")));
        ArrayNode arrayNodeDiff = new ArrayNode(Collections.singletonList(new LeafNode("not same")));


        Map<String, ConfigNode> mapNode = new HashMap<>();
        mapNode.put("test", new LeafNode("leaf"));
        MapNode objectNode = new MapNode(mapNode);
        Map<String, ConfigNode> mapNodeSame = new HashMap<>();
        mapNodeSame.put("test", new LeafNode("leaf"));
        MapNode objectNodeSame = new MapNode(mapNodeSame);
        Map<String, ConfigNode> mapNodeDiff = new HashMap<>();
        mapNodeDiff.put("not same", new LeafNode("not same"));
        MapNode objectNodeDif = new MapNode(mapNodeDiff);


        LeafNode leaf = new LeafNode("leaf");
        LeafNode leafSame = new LeafNode("leaf");
        LeafNode leafDiff = new LeafNode("not same");

        Assertions.assertEquals(arrayNode, arrayNode);
        Assertions.assertEquals(arrayNode, arrayNodeSame);
        Assertions.assertNotEquals(arrayNode, arrayNodeDiff);
        Assertions.assertNotEquals(arrayNode, objectNode);

        Assertions.assertEquals(objectNode, objectNode);
        Assertions.assertEquals(objectNode, objectNodeSame);
        Assertions.assertNotEquals(objectNode, objectNodeDif);
        Assertions.assertNotEquals(objectNode, arrayNode);


        Assertions.assertEquals(leaf, leaf);
        Assertions.assertEquals(leaf, leafSame);
        Assertions.assertNotEquals(leaf, leafDiff);
        Assertions.assertNotEquals(leaf, arrayNode);
    }

    @Test
    void hash() {
        ArrayNode arrayNode = new ArrayNode(Arrays.asList(new LeafNode("a"), new LeafNode("b")));
        Map<String, ConfigNode> mapNode = new HashMap<>();
        mapNode.put("test", new LeafNode("leaf"));
        mapNode.put("test2", new LeafNode("leaf2"));
        MapNode objectNode = new MapNode(mapNode);
        LeafNode leaf = new LeafNode("leaf");

        Assertions.assertEquals(5089, arrayNode.hashCode());
        Assertions.assertEquals(12049761, objectNode.hashCode());
        Assertions.assertEquals(3317629, leaf.hashCode());
    }

    @Test
    void nullArray() {
        ArrayNode arrayNode = new ArrayNode(null);
        Assertions.assertEquals(0, arrayNode.size());
    }

    @Test
    void toStringTest() {
        ArrayNode arrayNode = new ArrayNode(Arrays.asList(new LeafNode("a"), new LeafNode("b")));
        Map<String, ConfigNode> mapNode = new HashMap<>();
        mapNode.put("test", new LeafNode("leaf"));
        mapNode.put("test2", new LeafNode("leaf2"));
        MapNode objectNode = new MapNode(mapNode);
        LeafNode leaf = new LeafNode("leaf");

        Assertions.assertEquals("ArrayNode{values=[LeafNode{value='a'}, LeafNode{value='b'}]}",
            arrayNode.toString());
        Assertions.assertEquals("MapNode{mapNode={test2=LeafNode{value='leaf2'}, test=LeafNode{value='leaf'}}}",
            objectNode.toString());
        Assertions.assertEquals("LeafNode{value='leaf'}", leaf.toString());
    }
}

