package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.metadata.IsEncryptedMetadata;
import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class EncryptionAnnotationMetadataTransformTest {

    private EncryptionAnnotationMetadataTransform transform;

    @BeforeEach
    void setUp() {
        transform = new EncryptionAnnotationMetadataTransform();
    }

    @Test
    void testName() {
        Assertions.assertEquals("encrypt", transform.name());
    }

    @Test
    void testAnnotationTransformWithTrueParameter() {
        var result = transform.annotationTransform("testName", "true");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, List<MetaDataValue<?>>> metadata = result.results();

        Assertions.assertEquals(1, metadata.get(IsNoCacheMetadata.NO_CACHE).size());
        Assertions.assertTrue(((IsNoCacheMetadata) metadata.get(IsNoCacheMetadata.NO_CACHE).get(0)).getMetadata());

        Assertions.assertEquals(1, metadata.get(IsEncryptedMetadata.ENCRYPTED).size());
        Assertions.assertTrue(((IsEncryptedMetadata) metadata.get(IsEncryptedMetadata.ENCRYPTED).get(0)).getMetadata());
    }

    @Test
    void testAnnotationTransformWithFalseParameter() {
        GResultOf<Map<String, List<MetaDataValue<?>>>> result = transform.annotationTransform("testName", "false");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, List<MetaDataValue<?>>> metadata = result.results();

        Assertions.assertEquals(1, metadata.get(IsNoCacheMetadata.NO_CACHE).size());
        Assertions.assertFalse(((IsNoCacheMetadata) metadata.get(IsNoCacheMetadata.NO_CACHE).get(0)).getMetadata());

        Assertions.assertEquals(1, metadata.get(IsEncryptedMetadata.ENCRYPTED).size());
        Assertions.assertFalse(((IsEncryptedMetadata) metadata.get(IsEncryptedMetadata.ENCRYPTED).get(0)).getMetadata());
    }

    @Test
    void testAnnotationTransformWithNullParameter() {
        GResultOf<Map<String, List<MetaDataValue<?>>>> result = transform.annotationTransform("testName", null);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, List<MetaDataValue<?>>> metadata = result.results();

        Assertions.assertEquals(1, metadata.get(IsNoCacheMetadata.NO_CACHE).size());
        Assertions.assertTrue(((IsNoCacheMetadata) metadata.get(IsNoCacheMetadata.NO_CACHE).get(0)).getMetadata());

        Assertions.assertEquals(1, metadata.get(IsEncryptedMetadata.ENCRYPTED).size());
        Assertions.assertTrue(((IsEncryptedMetadata) metadata.get(IsEncryptedMetadata.ENCRYPTED).get(0)).getMetadata());
    }

    @Test
    void testAnnotationTransformWithEmptyParameter() {
        GResultOf<Map<String, List<MetaDataValue<?>>>> result = transform.annotationTransform("testName", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Map<String, List<MetaDataValue<?>>> metadata = result.results();

        Assertions.assertEquals(1, metadata.get(IsNoCacheMetadata.NO_CACHE).size());
        Assertions.assertTrue(((IsNoCacheMetadata) metadata.get(IsNoCacheMetadata.NO_CACHE).get(0)).getMetadata());

        Assertions.assertEquals(1, metadata.get(IsEncryptedMetadata.ENCRYPTED).size());
        Assertions.assertTrue(((IsEncryptedMetadata) metadata.get(IsEncryptedMetadata.ENCRYPTED).get(0)).getMetadata());
    }
}
