package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.utils.GResultOf;
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
        GResultOf<String> resultsOf = systemPropertiesTransformer.process("hello", "test", "");

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());
        Assertions.assertNotNull(resultsOf.results());
        String results = resultsOf.results();
        Assertions.assertEquals("value", results);
    }

    @Test
    void processMissing() {
        System.getProperties().put("test", "value");
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        GResultOf<String> resultsOf = systemPropertiesTransformer.process("hello", "no-exist", "");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("No System Property found for: no-exist, on path: hello during post process",
            resultsOf.getErrors().get(0).description());
    }

    @Test
    void processNull() {
        System.getProperties().put("test", "value");
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        GResultOf<String> resultsOf = systemPropertiesTransformer.process("hello", null, "");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("Invalid string: , on path: hello in transformer: sys",
            resultsOf.getErrors().get(0).description());
    }
}
