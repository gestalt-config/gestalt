package org.github.gestalt.config.post.process.transform;

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
    void processNoValue() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor  transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${map:noValue}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("test.path", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertFalse(validateNode.results().getValue().isPresent());
    }
}
