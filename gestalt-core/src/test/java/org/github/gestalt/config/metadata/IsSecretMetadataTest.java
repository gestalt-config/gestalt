package org.github.gestalt.config.metadata;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class IsSecretMetadataTest {

    @Test
    public void keyValue() {
        var metadata1 = new IsSecretMetadata(false);

        Assertions.assertEquals(IsSecretMetadata.SECRET, metadata1.keyValue());
    }

    @Test
    public void testRollup() {
        var metadata1 = new IsSecretMetadata(false);
        var metadata2 = new IsSecretMetadata(true);
        var metadata3 = new IsNoCacheMetadata(true);
        Map<String, List<MetaDataValue<?>>> metadataMap1 = Map.of(IsSecretMetadata.SECRET, List.of(metadata1));
        Map<String, List<MetaDataValue<?>>> metadataMap2 = Map.of(IsSecretMetadata.SECRET, List.of(metadata2));
        Map<String, List<MetaDataValue<?>>> metadataMap3 = Map.of(IsNoCacheMetadata.NO_CACHE, List.of(metadata3));

        Assertions.assertEquals(1, metadata1.rollup(metadataMap1).size());
        Assertions.assertTrue(metadata1.rollup(metadataMap1).containsKey(IsSecretMetadata.SECRET));
        Assertions.assertFalse((boolean) metadata1.rollup(metadataMap1).get(IsSecretMetadata.SECRET).get(0).getMetadata());

        Assertions.assertEquals(1, metadata2.rollup(metadataMap1).size());
        Assertions.assertTrue(metadata2.rollup(metadataMap1).containsKey(IsSecretMetadata.SECRET));
        Assertions.assertFalse((boolean) metadata2.rollup(metadataMap1).get(IsSecretMetadata.SECRET).get(0).getMetadata());

        Assertions.assertEquals(1, metadata1.rollup(metadataMap2).size());
        Assertions.assertTrue(metadata1.rollup(metadataMap2).containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) metadata1.rollup(metadataMap2).get(IsSecretMetadata.SECRET).get(0).getMetadata());

        Assertions.assertEquals(1, metadata2.rollup(metadataMap2).size());
        Assertions.assertTrue(metadata2.rollup(metadataMap2).containsKey(IsSecretMetadata.SECRET));
        Assertions.assertTrue((boolean) metadata2.rollup(metadataMap2).get(IsSecretMetadata.SECRET).get(0).getMetadata());

        Assertions.assertEquals(1, metadata2.rollup(metadataMap3).size());
        Assertions.assertFalse(metadata2.rollup(metadataMap3).containsKey(IsSecretMetadata.SECRET));
    }
}
