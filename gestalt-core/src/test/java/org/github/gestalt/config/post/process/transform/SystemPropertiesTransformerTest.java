package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SystemPropertiesTransformerTest {

    @Test
    void name() {
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        Assertions.assertEquals("sys", systemPropertiesTransformer.name());
    }

    @Test
    void process() {
        System.getProperties().put("test", "value");
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        ValidateOf<String> validateOfResults = systemPropertiesTransformer.process("hello", "test", "");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertNotNull(validateOfResults.results());
        String results = validateOfResults.results();
        Assertions.assertEquals("value", results);
    }

    @Test
    void processMissing() {
        System.getProperties().put("test", "value");
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        ValidateOf<String> validateOfResults = systemPropertiesTransformer.process("hello", "no-exist", "");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("No System Property found for: no-exist, on path: hello during post process",
            validateOfResults.getErrors().get(0).description());
    }
}
