package org.github.gestalt.config.node;

import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class MapConfigNodeTest {

    @Test
    void mapMetadata() {
        LeafNode leaf = new LeafNode("hello");

        MapNode mapNode = new MapNode(Map.of("test1", leaf), Map.of(IsNoCacheMetadata.NO_CACHE,
            List.of(new IsNoCacheMetadata(true))));

        Assertions.assertTrue(mapNode.hasMetadata(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode.getMetadata(IsNoCacheMetadata.NO_CACHE).isEmpty());
        Assertions.assertEquals(true, ((IsNoCacheMetadata) mapNode.getMetadata(IsNoCacheMetadata.NO_CACHE).get(0))
            .getMetadata());
    }

    @Test
    void mapMetadataRollupSingle() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));
        MapNode mapNode = new MapNode(Map.of("test1", leaf));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));
        MapNode mapNode2 = new MapNode(Map.of("test1", leaf2));

        LeafNode leaf3 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));
        MapNode mapNode3 = new MapNode(Map.of("test1", leaf3));

        LeafNode leaf4 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));
        MapNode mapNode4 = new MapNode(Map.of("test1", leaf4));


        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertTrue(mapNode2.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode2.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) mapNode2.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());

        Assertions.assertFalse(mapNode3.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode3.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertFalse(mapNode4.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode4.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
    }

    @Test
    void mapMetadataRollupSingleWithNodeMetadata() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));
        MapNode mapNode = new MapNode(Map.of("test1", leaf), Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));
        MapNode mapNode2 = new MapNode(Map.of("test1", leaf2), Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));

        LeafNode leaf3 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));
        MapNode mapNode3 = new MapNode(Map.of("test1", leaf3), Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));

        LeafNode leaf4 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));
        MapNode mapNode4 = new MapNode(Map.of("test1", leaf4), Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));

        LeafNode leaf5 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));
        MapNode mapNode5 = new MapNode(Map.of("test1", leaf5), Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));

        LeafNode leaf6 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));
        MapNode mapNode6 = new MapNode(Map.of("test1", leaf6), Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf7 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));
        MapNode mapNode7 = new MapNode(Map.of("test1", leaf7), Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));

        LeafNode leaf8 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));
        MapNode mapNode8 = new MapNode(Map.of("test1", leaf8), Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertTrue(mapNode2.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode2.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) mapNode2.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());

        Assertions.assertTrue(mapNode3.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode3.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) mapNode3.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());

        Assertions.assertTrue(mapNode4.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode4.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) mapNode4.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());

        Assertions.assertFalse(mapNode5.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode5.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertFalse(mapNode6.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode6.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertFalse(mapNode7.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode7.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertFalse(mapNode8.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode8.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
    }

    @Test
    void mapMetadataRollupMultipleNoCacheTrue() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        MapNode mapNode = new MapNode(Map.of("test1", leaf, "test2", leaf2),
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));

        Assertions.assertTrue(mapNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) mapNode.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    void mapMetadataRollupMultipleNoCacheTrue2() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        MapNode mapNode = new MapNode(Map.of("test1", leaf, "test2", leaf2), Map.of(IsNoCacheMetadata.NO_CACHE,
            List.of(new IsNoCacheMetadata(true))));

        Assertions.assertTrue(mapNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) mapNode.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    void mapMetadataRollupMultipleNoCacheFalse() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        MapNode mapNode = new MapNode(Map.of("test1", leaf, "test2", leaf2), Map.of(IsNoCacheMetadata.NO_CACHE,
            List.of(new IsNoCacheMetadata(false))));

        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
    }

    @Test
    void mapMetadataRollupMultipleSecret() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        MapNode mapNode = new MapNode(Map.of("test1", leaf, "test2", leaf2),
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(mapNode.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
    }
}
