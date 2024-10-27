package org.github.gestalt.config.metadata;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class IsNoCacheMetadataTest {

    @Test
    public void keyValue() {
        var metadata1 = new IsNoCacheMetadata(false);

        Assertions.assertEquals(IsNoCacheMetadata.NO_CACHE_METADATA, metadata1.keyValue());
    }

    @Test
    public void testRollup() {
        var metadata1 = new IsNoCacheMetadata(false);
        var metadata2 = new IsNoCacheMetadata(true);
        var metadata3 = new IsSecretMetadata(true);
        Map<String, List<MetaDataValue<?>>> metadataMap1 = Map.of(IsNoCacheMetadata.NO_CACHE_METADATA, List.of(metadata1));
        Map<String, List<MetaDataValue<?>>> metadataMap2 = Map.of(IsNoCacheMetadata.NO_CACHE_METADATA, List.of(metadata2));
        Map<String, List<MetaDataValue<?>>> metadataMap3 = Map.of(IsSecretMetadata.IS_SECRET_METADATA, List.of(metadata3));

        Assertions.assertEquals(1, metadata1.rollup(metadataMap1).size());
        Assertions.assertTrue(metadata1.rollup(metadataMap1).containsKey(IsNoCacheMetadata.NO_CACHE_METADATA));
        Assertions.assertFalse((boolean) metadata1.rollup(metadataMap1).get(IsNoCacheMetadata.NO_CACHE_METADATA).get(0).getMetadata());

        Assertions.assertEquals(1, metadata2.rollup(metadataMap1).size());
        Assertions.assertTrue(metadata2.rollup(metadataMap1).containsKey(IsNoCacheMetadata.NO_CACHE_METADATA));
        Assertions.assertTrue((boolean) metadata2.rollup(metadataMap1).get(IsNoCacheMetadata.NO_CACHE_METADATA).get(0).getMetadata());

        Assertions.assertEquals(1, metadata1.rollup(metadataMap2).size());
        Assertions.assertTrue(metadata1.rollup(metadataMap2).containsKey(IsNoCacheMetadata.NO_CACHE_METADATA));
        Assertions.assertTrue((boolean) metadata1.rollup(metadataMap2).get(IsNoCacheMetadata.NO_CACHE_METADATA).get(0).getMetadata());

        Assertions.assertEquals(1, metadata2.rollup(metadataMap2).size());
        Assertions.assertTrue(metadata2.rollup(metadataMap2).containsKey(IsNoCacheMetadata.NO_CACHE_METADATA));
        Assertions.assertTrue((boolean) metadata2.rollup(metadataMap2).get(IsNoCacheMetadata.NO_CACHE_METADATA).get(0).getMetadata());

        Assertions.assertEquals(2, metadata2.rollup(metadataMap3).size());
        Assertions.assertTrue(metadata2.rollup(metadataMap3).containsKey(IsNoCacheMetadata.NO_CACHE_METADATA));
        Assertions.assertTrue((boolean) metadata2.rollup(metadataMap3).get(IsNoCacheMetadata.NO_CACHE_METADATA).get(0).getMetadata());
    }
}
