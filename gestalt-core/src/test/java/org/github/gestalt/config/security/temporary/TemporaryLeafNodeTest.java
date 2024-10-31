package org.github.gestalt.config.security.temporary;

import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.NodeType;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.secret.rules.SecretConcealerManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

class TemporaryLeafNodeTest {

    @Test
    @SuppressWarnings("VariableDeclarationUsageDistance")
    public void temporaryLeafNodeAccessCount() {
        LeafNode tempNode = new LeafNode("secret");
        WeakReference<LeafNode> internalNode = new WeakReference<>(tempNode);
        TemporaryLeafNode leaf = new TemporaryLeafNode(tempNode, 2, Map.of());

        // clear temp node so it can be cleaned up once released from the TemporaryLeafNode
        tempNode = null;

        Assertions.assertEquals("secret", leaf.getValue().get());
        Assertions.assertEquals("secret", leaf.getValue().get());
        Assertions.assertTrue(leaf.getValue().isEmpty());
        Assertions.assertTrue(leaf.getValue().isEmpty());

        // force a GC, so we can clean up the temp node
        System.gc();

        // validate that the temp node has been cleaned up.
        Assertions.assertNull(internalNode.get());
    }

    @Test
    public void temporaryLeafNodeToString() {
        LeafNode tempNode = new LeafNode("secret");
        TemporaryLeafNode leaf = new TemporaryLeafNode(tempNode, 1, Map.of());

        Assertions.assertEquals("TemporaryLeafNode{value='secret'}", leaf.toString());
        Assertions.assertEquals("TemporaryLeafNode{value='secret'}", leaf.toString());
        Assertions.assertEquals("TemporaryLeafNode{value='secret'}", leaf.toString());
        Assertions.assertEquals("TemporaryLeafNode{value='secret'}", leaf.toString());
        Assertions.assertEquals("secret", leaf.getValue().get());
        Assertions.assertTrue(leaf.getValue().isEmpty());
        Assertions.assertTrue(leaf.getValue().isEmpty());
        Assertions.assertEquals("TemporaryLeafNode{value=''}", leaf.toString());
    }

    @Test
    public void temporaryLeafNodePrintWithSecretConcealer() {
        LeafNode tempNode = new LeafNode("secret");
        TemporaryLeafNode leaf = new TemporaryLeafNode(tempNode, 2, Map.of());

        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "");
        Assertions.assertEquals("TemporaryLeafNode{value=''}", leaf.printer("secret", secretConcealer, null));
        Assertions.assertEquals("secret", leaf.getValue().get());
        Assertions.assertEquals("secret", leaf.getValue().get());
        Assertions.assertTrue(leaf.getValue().isEmpty());
        Assertions.assertTrue(leaf.getValue().isEmpty());
        Assertions.assertEquals("TemporaryLeafNode{value=''}", leaf.toString());
    }

    @Test
    public void temporaryLeafNodePrintWithSecretConcealerNull() {
        LeafNode tempNode = new LeafNode("secret");
        TemporaryLeafNode leaf = new TemporaryLeafNode(tempNode, 2, Map.of());

        Assertions.assertEquals("TemporaryLeafNode{value='secret'}", leaf.printer("secret", null, null));
        Assertions.assertEquals("secret", leaf.getValue().get());
        Assertions.assertEquals("secret", leaf.getValue().get());
        Assertions.assertTrue(leaf.getValue().isEmpty());
        Assertions.assertTrue(leaf.getValue().isEmpty());
        Assertions.assertEquals("TemporaryLeafNode{value=''}", leaf.toString());
    }

    @Test
    public void temporaryLeafNode() {
        TemporaryLeafNode leaf = new TemporaryLeafNode(new LeafNode("secret"), 2, Map.of());

        Assertions.assertEquals(NodeType.LEAF, leaf.getNodeType());
        Assertions.assertTrue(leaf.getKey("test").isEmpty());
        Assertions.assertTrue(leaf.getIndex(0).isEmpty());
    }

    @Test
    public void temporaryLeafNodeEquals() {
        TemporaryLeafNode leaf = new TemporaryLeafNode(new LeafNode("secret"), 2, Map.of());
        TemporaryLeafNode leaf2 = new TemporaryLeafNode(new LeafNode("secret"), 1, Map.of());
        TemporaryLeafNode leaf3 = new TemporaryLeafNode(new LeafNode("cert"), 1, Map.of());

        Assertions.assertEquals(leaf, leaf);
        Assertions.assertEquals(leaf, leaf2);
        Assertions.assertNotEquals(leaf, leaf3);
        Assertions.assertNotEquals(leaf, null);
        Assertions.assertNotEquals(leaf, new Object());
    }

    @Test
    public void temporaryLeafNodeHash() {
        TemporaryLeafNode leaf = new TemporaryLeafNode(new LeafNode("secret"), 2, Map.of());

        Assertions.assertTrue(leaf.hashCode() != 0);
    }

    @Test
    public void temporaryLeafNodeESize() {
        TemporaryLeafNode leaf = new TemporaryLeafNode(new LeafNode("secret"), 2, Map.of());

        Assertions.assertEquals(1, leaf.size());
    }
}

