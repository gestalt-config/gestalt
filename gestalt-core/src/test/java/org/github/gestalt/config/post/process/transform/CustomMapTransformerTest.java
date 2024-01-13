package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class CustomMapTransformerTest {

    @Test
    void name() {
        CustomMapTransformer transformer = new CustomMapTransformer(Collections.emptyMap());
        Assertions.assertEquals("map", transformer.name());
    }

    @Test
    void defaultCustomTransformer() {
        CustomMapTransformer transformer = new CustomMapTransformer();

        GResultOf<String> results = transformer.process("hello", "test", "");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("No custom Property found for: test, on path: hello during post process",
            results.getErrors().get(0).description());
    }

    @Test
    void process() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);
        GResultOf<String> resultsOf = transformer.process("hello", "test", "");

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());
        Assertions.assertNotNull(resultsOf.results());
        String results = resultsOf.results();
        Assertions.assertEquals("value", results);
    }

    @Test
    void processMissing() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);
        GResultOf<String> results = transformer.process("hello", "noExist", "");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("No custom Property found for: noExist, on path: hello during post process",
            results.getErrors().get(0).description());
    }

    @Test
    void processNull() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);
        GResultOf<String> results = transformer.process("hello", null, "map:");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("No custom Property found for: null, on path: hello during post process",
            results.getErrors().get(0).description());
    }
}
