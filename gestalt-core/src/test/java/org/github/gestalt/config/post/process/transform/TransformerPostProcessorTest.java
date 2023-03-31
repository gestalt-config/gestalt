package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TransformerPostProcessorTest {

    @Test
    void process() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
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

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
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

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
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

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
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

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${noTransform:value}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("test.path", node);

        Assertions.assertTrue(validateNode.hasErrors());
        Assertions.assertEquals(1, validateNode.getErrors().size());
        Assertions.assertEquals("Unable to find matching transform for test.path with transform: noTransform. " +
                "make sure you registered all expected transforms",
            validateNode.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validateNode.getErrors().get(0).level());
    }

    @Test
    void processDefaultTransformer() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("weather", "sunny");
        CustomMapTransformer customMapTransformer = new CustomMapTransformer(customMap);

        Map<String, String> customMap2 = new HashMap<>();
        customMap2.put("place", "Moon");
        customMap2.put("weather", "cold");
        CustomTransformer customTransformer = new CustomTransformer(customMap2);
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        EnvironmentVariablesTransformer environmentVariablesTransformer = new EnvironmentVariablesTransformer();

        System.setProperty("place", "Earth");

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(
            List.of(customMapTransformer, systemPropertiesTransformer, environmentVariablesTransformer, customTransformer));
        LeafNode node = new LeafNode("hello ${place} it is ${map:weather} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello Earth it is sunny today", validateNode.results().getValue().get());
    }

    @Test
    void processEscapedTransformer() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("weather", "sunny");
        CustomMapTransformer customMapTransformer = new CustomMapTransformer(customMap);

        Map<String, String> customMap2 = new HashMap<>();
        customMap2.put("place", "Moon");
        customMap2.put("weather", "cold");
        CustomTransformer customTransformer = new CustomTransformer(customMap2);
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        EnvironmentVariablesTransformer environmentVariablesTransformer = new EnvironmentVariablesTransformer();

        System.setProperty("place", "Earth");

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(
            List.of(customMapTransformer, systemPropertiesTransformer, environmentVariablesTransformer, customTransformer));
        LeafNode node = new LeafNode("\\${map:weather}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("\\${map:weather}", validateNode.results().getValue().get());
    }

    @Test
    void processEscapedTransformerSentance() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("weather", "sunny");
        CustomMapTransformer customMapTransformer = new CustomMapTransformer(customMap);

        Map<String, String> customMap2 = new HashMap<>();
        customMap2.put("place", "Moon");
        customMap2.put("weather", "cold");
        CustomTransformer customTransformer = new CustomTransformer(customMap2);
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        EnvironmentVariablesTransformer environmentVariablesTransformer = new EnvironmentVariablesTransformer();

        System.setProperty("place", "Earth");

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(
            List.of(customMapTransformer, systemPropertiesTransformer, environmentVariablesTransformer, customTransformer));
        LeafNode node = new LeafNode("hello ${place} it is \\${map:weather} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello Earth it is \\${map:weather} today", validateNode.results().getValue().get());
    }

    @Test
    void processDefaultTransformerNoValue() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("weather", "sunny");
        CustomMapTransformer customMapTransformer = new CustomMapTransformer(customMap);

        Map<String, String> customMap2 = new HashMap<>();
        customMap2.put("place", "Moon");
        customMap2.put("weather", "cold");
        CustomTransformer customTransformer = new CustomTransformer(customMap2);
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        EnvironmentVariablesTransformer environmentVariablesTransformer = new EnvironmentVariablesTransformer();

        System.setProperty("place", "Earth");

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(
            List.of(customMapTransformer, systemPropertiesTransformer, environmentVariablesTransformer, customTransformer));
        LeafNode node = new LeafNode("hello ${noValue} it is ${map:weather} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("test.path", node);

        Assertions.assertTrue(validateNode.hasErrors());
        Assertions.assertEquals(1, validateNode.getErrors().size());
        Assertions.assertEquals("Unable to find matching transform for test.path with the default transformers . " +
                "make sure you registered all expected transforms",
            validateNode.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validateNode.getErrors().get(0).level());
    }

    @Test
    void processDefaultTransformerNoValueInDefault() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("weather", "sunny");
        CustomMapTransformer customMapTransformer = new CustomMapTransformer(customMap);

        Map<String, String> customMap2 = new HashMap<>();
        customMap2.put("location", "Moon");
        customMap2.put("weather", "cold");
        CustomTransformerNoPriority customTransformer = new CustomTransformerNoPriority(customMap2);
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        EnvironmentVariablesTransformer environmentVariablesTransformer = new EnvironmentVariablesTransformer();

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(
            List.of(customMapTransformer, systemPropertiesTransformer, environmentVariablesTransformer, customTransformer));
        LeafNode node = new LeafNode("hello ${location} it is ${map:weather} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("test.path", node);

        Assertions.assertTrue(validateNode.hasErrors());
        Assertions.assertEquals(1, validateNode.getErrors().size());
        Assertions.assertEquals("Unable to find matching transform for test.path with the default transformers . " +
                "make sure you registered all expected transforms",
            validateNode.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validateNode.getErrors().get(0).level());
    }

    @Test
    void processStrangeNamesTransformer() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("weather.*", "sunny");
        CustomMapTransformer customMapTransformer = new CustomMapTransformer(customMap);

        Map<String, String> customMap2 = new HashMap<>();
        customMap2.put("place@", "Moon");
        customMap2.put("weather.*", "cold");
        CustomTransformer customTransformer = new CustomTransformer(customMap2);
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        EnvironmentVariablesTransformer environmentVariablesTransformer = new EnvironmentVariablesTransformer();

        System.setProperty("place@", "Earth");

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(
            List.of(customMapTransformer, systemPropertiesTransformer, environmentVariablesTransformer, customTransformer));
        LeafNode node = new LeafNode("hello ${place@} it is ${map:weather.*} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello Earth it is sunny today", validateNode.results().getValue().get());
    }

    @ConfigPriority(10)
    public static class CustomTransformer extends CustomMapTransformer {
        public CustomTransformer(Map<String, String> replacementVars) {
            super(replacementVars);
        }

        @Override
        public String name() {
            return "custom";
        }
    }

    public static class CustomTransformerNoPriority extends CustomMapTransformer {
        public CustomTransformerNoPriority(Map<String, String> replacementVars) {
            super(replacementVars);
        }

        @Override
        public String name() {
            return "custom";
        }
    }

}
