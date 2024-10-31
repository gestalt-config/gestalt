package org.github.gestalt.config.node;

import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ArrayConfigNodeTest {

    @Test
    void arrayMetadata() {
        LeafNode leaf = new LeafNode("hello");

        ArrayNode arrayNode = new ArrayNode(List.of(leaf), Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));

        Assertions.assertTrue(arrayNode.hasMetadata(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode.getMetadata(IsNoCacheMetadata.NO_CACHE).isEmpty());
        Assertions.assertEquals(true,
            ((IsNoCacheMetadata) arrayNode.getMetadata(IsNoCacheMetadata.NO_CACHE).get(0)).getMetadata());
    }

    @Test
    void arrayMetadataRollupSingle() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));
        ArrayNode arrayNode = new ArrayNode(List.of(leaf));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));
        ArrayNode arrayNode2 = new ArrayNode(List.of(leaf2));

        LeafNode leaf3 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));
        ArrayNode arrayNode3 = new ArrayNode(List.of(leaf3));


        LeafNode leaf4 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));
        ArrayNode arrayNode4 = new ArrayNode(List.of(leaf4));


        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertTrue(arrayNode2.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode2.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) arrayNode2.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());

        Assertions.assertFalse(arrayNode3.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode3.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertFalse(arrayNode4.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode4.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
    }

    @Test
    void arrayMetadataRollupSingleWithNodeMetadata() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));
        ArrayNode arrayNode = new ArrayNode(List.of(leaf), Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));
        ArrayNode arrayNode2 = new ArrayNode(List.of(leaf2), Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));

        LeafNode leaf3 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));
        ArrayNode arrayNode3 = new ArrayNode(List.of(leaf3), Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));

        LeafNode leaf4 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));
        ArrayNode arrayNode4 = new ArrayNode(List.of(leaf4), Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));

        LeafNode leaf5 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));
        ArrayNode arrayNode5 = new ArrayNode(List.of(leaf5), Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));

        LeafNode leaf6 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));
        ArrayNode arrayNode6 = new ArrayNode(List.of(leaf6), Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf7 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));
        ArrayNode arrayNode7 = new ArrayNode(List.of(leaf7), Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));

        LeafNode leaf8 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));
        ArrayNode arrayNode8 = new ArrayNode(List.of(leaf8), Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));


        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertTrue(arrayNode2.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode2.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) arrayNode2.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());

        Assertions.assertTrue(arrayNode3.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode3.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) arrayNode3.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());

        Assertions.assertTrue(arrayNode4.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode4.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) arrayNode4.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());

        Assertions.assertFalse(arrayNode5.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode5.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertFalse(arrayNode6.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode6.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertFalse(arrayNode7.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode7.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertFalse(arrayNode8.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode8.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
    }

    @Test
    void arrayMetadataRollupMultipleNoCacheTrue() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        ArrayNode arrayNode = new ArrayNode(List.of(leaf, leaf2), Map.of(IsNoCacheMetadata.NO_CACHE,
            List.of(new IsNoCacheMetadata(false))));

        Assertions.assertTrue(arrayNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) arrayNode.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    void arrayMetadataRollupMultipleNoCacheTrue2() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        ArrayNode arrayNode = new ArrayNode(List.of(leaf, leaf2), Map.of(IsNoCacheMetadata.NO_CACHE,
            List.of(new IsNoCacheMetadata(true))));

        Assertions.assertTrue(arrayNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) arrayNode.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    void arrayMetadataRollupMultipleNoCacheFalse() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        ArrayNode arrayNode = new ArrayNode(List.of(leaf, leaf2), Map.of(IsNoCacheMetadata.NO_CACHE,
            List.of(new IsNoCacheMetadata(false))));

        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
    }

    @Test
    void arrayMetadataRollupMultipleSecret() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        ArrayNode arrayNode = new ArrayNode(List.of(leaf, leaf2),
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(arrayNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
    }
}

