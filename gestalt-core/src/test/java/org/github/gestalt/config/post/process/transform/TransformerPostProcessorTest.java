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

@SuppressWarnings("VariableDeclarationUsageDistance")
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
    void processTextWithoutTransform() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello Earth!");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello Earth!", validateNode.results().getValue().get());
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
    void processTextWithMultipleDefaults() {

        Map<String, String> customMap = new HashMap<>();
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${map:place:=world} it is ${weather:=sunny} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello world it is sunny today", validateNode.results().getValue().get());
    }

    @Test
    void processTextWithEmptyDefaults() {

        Map<String, String> customMap = new HashMap<>();
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${map:place:=world} it is ${weather:=} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello world it is  today", validateNode.results().getValue().get());
    }

    @Test
    void processTextWithMultipleDefaultsSpecialCharacters() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${map:place:=abc:=} it is ${weather:=aaa.*} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello world it is sunny today", validateNode.results().getValue().get());
    }

    @Test
    void processTextWithDefaultsSpecialCharacters() {

        Map<String, String> customMap = new HashMap<>();
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${map:place:=world:=} it is ${weather:=sunny.*} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello world:= it is sunny.* today", validateNode.results().getValue().get());
    }

    @Test
    void processTextWithMultipleDefaultsButHasValues() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${map:place:=earth} it is ${weather:=overcast} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello world it is sunny today", validateNode.results().getValue().get());
    }

    @Test
    void processMissingTransform() {
        // not sure about this test, this isnt "intended" behaviour. It just happens to happen.

        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${map:place:world}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertTrue(validateNode.hasErrors());
        Assertions.assertEquals(1, validateNode.getErrors().size());
        Assertions.assertEquals("No custom Property found for: place:world, on path: location during post process",
            validateNode.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validateNode.getErrors().get(0).level());

    }

    @Test
    void processInvalidFormat() {
        // not sure about this test, this isnt "intended" behaviour. It just happens to happen.

        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertTrue(validateNode.hasErrors());
        Assertions.assertEquals(1, validateNode.getErrors().size());
        Assertions.assertEquals("Transform doesnt match the expected format with value  on path location",
            validateNode.getErrors().get(0).description());
        Assertions.assertEquals(ValidationLevel.ERROR, validateNode.getErrors().get(0).level());
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
        Assertions.assertEquals("No custom Property found for: noValue, on path: test.path during post process",
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
        Assertions.assertEquals("${map:weather}", validateNode.results().getValue().get());
    }

    @Test
    void processEscapedTransformerSentence() {
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
        Assertions.assertEquals("hello Earth it is ${map:weather} today", validateNode.results().getValue().get());
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
        Assertions.assertEquals("Unable to find matching transform for test.path with the default transformers. " +
                "For key: noValue, make sure you registered all expected transforms",
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
        Assertions.assertEquals("Unable to find matching transform for test.path with the default transformers. " +
                "For key: location, make sure you registered all expected transforms",
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

    @Test
    void processNestedTransform() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("variable", "place");
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${map:${variable}} it is ${map:weather} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello world it is sunny today", validateNode.results().getValue().get());
    }

    @Test
    void processTwoNestedTransform() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("variable", "place");
        customMap.put("source", "map");
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${${source}:${variable}} it is ${map:weather} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello world it is sunny today", validateNode.results().getValue().get());
    }

    @Test
    void processDoubleNestedTransform() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("variable1", "variable2");
        customMap.put("variable2", "place");
        customMap.put("source", "map");
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("hello ${${source}:${variable1}} it is ${map:weather} today");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("hello place it is sunny today", validateNode.results().getValue().get());
    }

    @Test
    void processTripleNestedTransform() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("this.path", "location");
        customMap.put("your.path.location", "greeting");
        customMap.put("my.path.greeting", "good day");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${my.path.${your.path.${this.path}}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("good day", validateNode.results().getValue().get());
    }

    @Test
    void processDeeplyNestedTransform() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("here", "there");
        customMap.put("this.path.there", "location");
        customMap.put("your.path.location", "greeting");
        customMap.put("my.path.greeting", "good day");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${my.path.${your.path.${this.path.${here}}}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("good day", validateNode.results().getValue().get());
    }

    @Test
    void processNestedTransformWithNestedTransforms() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("this.path", "greeting");
        customMap.put("your.path", "${this.path}");
        customMap.put("my.path.greeting", "good day");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${my.path.${your.path}}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("good day", validateNode.results().getValue().get());
    }


    @Test
    void processNestedTransformWithNestedTransformsTimesTwo() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("that.path", "greeting");
        customMap.put("this.path", "${that.path}");
        customMap.put("your.path", "${this.path}");
        customMap.put("my.path.greeting", "good day");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${my.path.${your.path}}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("good day", validateNode.results().getValue().get());
    }

    @Test
    void processTooDeeplyNestedTransform() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("here", "there");
        customMap.put("this.path.there", "location");
        customMap.put("your.path.location", "greeting");
        customMap.put("my.path.greeting", "good day");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${my.path.${your.path.${this.path.${here.${their.${test}}}}}}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertTrue(validateNode.hasErrors());
        Assertions.assertEquals(6, validateNode.getErrors().size());
        Assertions.assertEquals("Exceeded maximum nested substitution depth of 6 on path location for node: " +
                "LeafNode{value='${my.path.${your.path.${this.path.${here.${their.${test}}}}}}'}",
            validateNode.getErrors().get(0).description());
    }

    @Test
    void processNestedTransformWithInfiniteLoop() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("this.path", "${your.path}");
        customMap.put("your.path", "${this.path}");
        customMap.put("my.path.greeting", "good day");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("${my.path.${your.path}}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertTrue(validateNode.hasErrors());
        Assertions.assertEquals(2, validateNode.getErrors().size());
        Assertions.assertEquals("Exceeded maximum nested substitution depth of 6 on path location for node: " +
                "LeafNode{value='${my.path.${your.path}}'}",
            validateNode.getErrors().get(0).description());
    }

    @Test
    void processEscapedNestedTransforms() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("this.path", "greeting");
        customMap.put("your.path", "${this.path}");
        customMap.put("my.path.greeting", "good day");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("\\${my.path.${your.path}.night\\}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("location", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("${my.path.greeting.night}", validateNode.results().getValue().get());
    }

    @Test
    void processNestedDefaults() {

        Map<String, String> customMap = new HashMap<>();
        customMap.put("path1", "sunny");
        customMap.put("path2", "cloudy");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("the weather is ${path1:=${path2:=rainy}}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("weather", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("the weather is sunny", validateNode.results().getValue().get());
    }

    @Test
    void processNestedDefaultsFallback() {

        Map<String, String> customMap = new HashMap<>();
        //customMap.put("path1", "sunny");
        customMap.put("path2", "cloudy");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("the weather is ${path1:=${path2:=rainy}}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("weather", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("the weather is cloudy", validateNode.results().getValue().get());
    }

    @Test
    void processNestedDefaultsFallbackDefault() {

        Map<String, String> customMap = new HashMap<>();
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);

        TransformerPostProcessor transformerPostProcessor = new TransformerPostProcessor(Collections.singletonList(transformer));
        LeafNode node = new LeafNode("the weather is ${path1:=${path2:=rainy}}");
        ValidateOf<ConfigNode> validateNode = transformerPostProcessor.process("weather", node);

        Assertions.assertFalse(validateNode.hasErrors());
        Assertions.assertTrue(validateNode.hasResults());
        Assertions.assertTrue(validateNode.results().getValue().isPresent());
        Assertions.assertEquals("the weather is rainy", validateNode.results().getValue().get());
    }

    @ConfigPriority(10)
    public static class CustomTransformer extends TestCustomMapTransformer {
        public CustomTransformer(Map<String, String> replacementVars) {
            super(replacementVars);
        }

        @Override
        public String name() {
            return "custom";
        }
    }

    public static class CustomTransformerNoPriority extends TestCustomMapTransformer {
        public CustomTransformerNoPriority(Map<String, String> replacementVars) {
            super(replacementVars);
        }

        @Override
        public String name() {
            return "custom";
        }
    }

}

