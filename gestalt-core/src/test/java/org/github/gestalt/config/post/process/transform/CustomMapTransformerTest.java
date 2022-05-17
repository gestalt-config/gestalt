package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.utils.ValidateOf;
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

        ValidateOf<String> validateOfResults = transformer.process("hello", "test");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("No custom Property found for: test, on path: hello during post process",
            validateOfResults.getErrors().get(0).description());
    }

    @Test
    void process() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);
        ValidateOf<String> validateOfResults = transformer.process("hello", "test");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertNotNull(validateOfResults.results());
        String results = validateOfResults.results();
        Assertions.assertEquals("value", results);
    }

    @Test
    void processMissing() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);
        ValidateOf<String> validateOfResults = transformer.process("hello", "noExist");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("No custom Property found for: noExist, on path: hello during post process",
            validateOfResults.getErrors().get(0).description());
    }
}
