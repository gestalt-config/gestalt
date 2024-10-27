package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.node.LeafNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;


class AnnotationConfigNodeProcessorTest {

    public static class TestAnnotationMetadataTransform implements AnnotationMetadataTransform {
        @Override
        public String name() {
            return "test";
        }

        @Override
        public Map<String, List<MetaDataValue<?>>> annotationTransform(String name, String parameter) {
           return Map.of(IsNoCacheMetadata.NO_CACHE_METADATA, List.of(new IsNoCacheMetadata(true)));
        }
    }

    @Test
    public void testAnnotationSingle() {
        LeafNode node = new LeafNode("hello@{test}");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor = new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE_METADATA));
        Assertions.assertTrue((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE_METADATA).get(0).getMetadata());
    }

    @Test
    public void testAnnotationSingleBefore() {
        LeafNode node = new LeafNode("@{test}hello");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor = new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE_METADATA));
        Assertions.assertTrue((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE_METADATA).get(0).getMetadata());
    }

    @Test
    public void testAnnotationSingleMiddle() {
        LeafNode node = new LeafNode("hel@{test}lo");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor = new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE_METADATA));
        Assertions.assertTrue((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE_METADATA).get(0).getMetadata());
    }

}
