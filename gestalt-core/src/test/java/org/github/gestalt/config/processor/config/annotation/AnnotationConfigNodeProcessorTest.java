package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;


class AnnotationConfigNodeProcessorTest {

    @Test
    public void testAnnotationSingle() {
        LeafNode node = new LeafNode("hello@{test}");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertTrue((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    public void testAnnotationSingleBefore() {
        LeafNode node = new LeafNode("@{test}hello");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertTrue((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    public void testAnnotationSingleMiddle() {
        LeafNode node = new LeafNode("hel@{test}lo");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertTrue((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    public void testAnnotationSingleParameter() {
        LeafNode node = new LeafNode("hello@{test:true}");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertTrue((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    public void testAnnotationSingleParameterFalse() {
        LeafNode node = new LeafNode("hello@{test:false}");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertFalse((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    public void testNoAnnotation() {
        LeafNode node = new LeafNode("hello");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertFalse(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
    }

    @Test
    public void testAnnotationErrorNoClosingTag() {
        LeafNode node = new LeafNode("hello@{test");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.WARN, result.getErrors().get(0).level());
        Assertions.assertEquals("Found annotation opening token but not a closing one: hello@{test for path: my.data",
            result.getErrors().get(0).description());

        Assertions.assertEquals("hello@{test", result.results().getValue().get());

        Assertions.assertFalse(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
    }

    @Test
    public void testNotLeaf() {
        LeafNode node = new LeafNode("hello");
        ArrayNode arrayNode = new ArrayNode(List.of(node));

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", arrayNode);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals(1, result.results().size());
        Assertions.assertEquals("hello", result.results().getIndex(0).get().getValue().get());

        Assertions.assertFalse(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
    }

    @Test
    public void testLeafEmptyNode() {
        LeafNode node = new LeafNode("");

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("", result.results().getValue().get());

        Assertions.assertFalse(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
    }

    @Test
    public void testLeafNullNode() {
        LeafNode node = new LeafNode(null);

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.results().getValue().isEmpty());

        Assertions.assertFalse(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
    }

    @Test
    public void testDifferentTokens() {
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setAnnotationOpeningToken("^[");
        gestaltConfig.setAnnotationClosingToken("]");

        ConfigNodeProcessorConfig configNodeProcessorConfig =
            new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        annotationConfigNodeProcessor.applyConfig(configNodeProcessorConfig);

        LeafNode node = new LeafNode("hello@{test}");
        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello@{test}", result.results().getValue().get());

        Assertions.assertFalse(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));

        node = new LeafNode("hello^[test]");
        result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertEquals("hello", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertTrue((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    @Test
    public void testBadRegex() {
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.setAnnotationRegex("testtest");

        ConfigNodeProcessorConfig configNodeProcessorConfig =
            new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);

        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        annotationConfigNodeProcessor.applyConfig(configNodeProcessorConfig);

        LeafNode node = new LeafNode("hello@{test}");
        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.WARN, result.getErrors().get(0).level());
        Assertions.assertEquals("Unable to extract annotation using regex my.data for path: test",
            result.getErrors().get(0).description());

        Assertions.assertEquals("hello@{test}", result.results().getValue().get());

        Assertions.assertFalse(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
    }

    @Test
    public void testUnknownToken() {
        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        LeafNode node = new LeafNode("hello@{abcdef}");
        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.WARN, result.getErrors().get(0).level());
        Assertions.assertEquals("Unknown annotation: abcdef for path: my.data",
            result.getErrors().get(0).description());

        Assertions.assertEquals("hello@{abcdef}", result.results().getValue().get());

        Assertions.assertFalse(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
    }

    @Test
    public void testGoodFollowedByUnknownToken() {
        AnnotationConfigNodeProcessor annotationConfigNodeProcessor =
            new AnnotationConfigNodeProcessor(List.of(new TestAnnotationMetadataTransform()));

        LeafNode node = new LeafNode("hello@{test}@{abcdef}");
        var result = annotationConfigNodeProcessor.process("my.data", node);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.WARN, result.getErrors().get(0).level());
        Assertions.assertEquals("Unknown annotation: abcdef for path: my.data",
            result.getErrors().get(0).description());

        Assertions.assertEquals("hello@{abcdef}", result.results().getValue().get());

        Assertions.assertTrue(result.results().getMetadata().containsKey(IsNoCacheMetadata.NO_CACHE));
        Assertions.assertTrue((boolean) result.results().getMetadata().get(IsNoCacheMetadata.NO_CACHE).get(0).getMetadata());
    }

    public static class TestAnnotationMetadataTransform implements AnnotationMetadataTransform {
        @Override
        public String name() {
            return "test";
        }

        @Override
        public GResultOf<Map<String, List<MetaDataValue<?>>>> annotationTransform(String name, String parameter) {
            boolean value = true;
            if (parameter != null && !parameter.isEmpty()) {
                value = Boolean.parseBoolean(parameter);
            }
            return GResultOf.result(Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(value))));
        }
    }
}
