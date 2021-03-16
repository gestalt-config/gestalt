package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class TransformerPostProcessorTest {

    @Test
    void process() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor  transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${map:test}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("test.path", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("value", validateNode.results().getValue().get());
    }

    @Test
    void processTextWithTransform() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor  transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${map:place}!");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello world!", validateNode.results().getValue().get());
    }

    @Test
    void processTextWithMultipleTransform() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor  transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${map:place} it is ${map:weather} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello world it is sunny today", validateNode.results().getValue().get());
    }

    @Test
    void processNoValue() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor  transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${map:noValue}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("test.path", node);

        Assertions.assertTrue(validateNode.hasErrors());
        Assertions.assertEquals(1, validateNode.getErrors().size());
        Assertions.assertEquals("Unable to find matching key for transform map with key noValue on path test.path",
            validateNode.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validateNode.getErrors().get(0).level());
    }

    @Test
    void processNoMatchingTransform() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor  transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${noTransform:value}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("test.path", node);

        Assertions.assertTrue(validateNode.hasErrors());
        Assertions.assertEquals(1, validateNode.getErrors().size());
        Assertions.assertEquals("Unable to find matching transform for test.path with transform: noTransform. " +
                "make sure you registered all expected transforms",
            validateNode.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validateNode.getErrors().get(0).level());
    }
}
