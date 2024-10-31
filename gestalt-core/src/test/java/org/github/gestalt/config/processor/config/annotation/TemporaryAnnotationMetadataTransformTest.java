package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.IsTemporaryMetadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemporaryAnnotationMetadataTransformTest {

    private TemporaryAnnotationMetadataTransform transform;

    @BeforeEach
    void setUp() {
        transform = new TemporaryAnnotationMetadataTransform();
    }

    @Test
    void testName() {
        Assertions.assertEquals("temp", transform.name());
    }

    @Test
    void testAnnotationTransformDefaultParameter() {
        // Test with no parameter, should default to value 1
        var result = transform.annotationTransform("tempAnnotation", null);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertTrue(result.results().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertTrue(result.results().containsKey(IsTemporaryMetadata.TEMPORARY));

        Assertions.assertEquals(1, result.results().get(IsTemporaryMetadata.TEMPORARY).size());
        Assertions.assertEquals(1, result.results().get(IsTemporaryMetadata.TEMPORARY).get(0).getMetadata());

        Assertions.assertEquals(true, result.results().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    void testAnnotationTransformWithParameter() {
        // Test with a valid integer parameter
        var result = transform.annotationTransform("tempAnnotation", "5");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertTrue(result.results().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertTrue(result.results().containsKey(IsTemporaryMetadata.TEMPORARY));

        Assertions.assertEquals(5, result.results().get(IsTemporaryMetadata.TEMPORARY).get(0).getMetadata());

        Assertions.assertEquals(true, result.results().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    void testAnnotationTransformWithEmptyParameter() {
        // Test with an empty parameter, should default to value 1
        var result = transform.annotationTransform("tempAnnotation", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertTrue(result.results().containsKey(IsTemporaryMetadata.TEMPORARY));
        Assertions.assertEquals(1, result.results().get(IsTemporaryMetadata.TEMPORARY).get(0).getMetadata());
    }
}
