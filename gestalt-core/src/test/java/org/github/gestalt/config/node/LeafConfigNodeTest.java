package org.github.gestalt.config.node;

import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class LeafConfigNodeTest {

    @Test
    void leafMetadata() {
        LeafNode leaf = new LeafNode("hello", Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));

        Assertions.assertTrue(leaf.hasMetadata(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(leaf.getMetadata(IsNoCacheMetadata.NO_CACHE).isEmpty());
        Assertions.assertEquals(true, ((IsNoCacheMetadata) leaf.getMetadata(IsNoCacheMetadata.NO_CACHE).get(0)).getMetadata());
    }

    @Test
    void leafMetadataRollupSingle() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true))));

        LeafNode leaf3 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false))));

        LeafNode leaf4 = new LeafNode("hello",
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        Assertions.assertFalse(leaf.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(leaf.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertTrue(leaf2.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(leaf2.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) leaf2.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());

        Assertions.assertFalse(leaf3.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(leaf3.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertFalse(leaf4.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(leaf4.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
    }

    @Test
    void leafMetadataRollupMultiple() {
        LeafNode leaf = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(false)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        LeafNode leaf2 = new LeafNode("hello",
            Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true)),
                IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));

        Assertions.assertFalse(leaf.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(leaf.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));

        Assertions.assertTrue(leaf2.getRolledUpMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse(leaf2.getRolledUpMetadata().containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) leaf2.getRolledUpMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }
}

