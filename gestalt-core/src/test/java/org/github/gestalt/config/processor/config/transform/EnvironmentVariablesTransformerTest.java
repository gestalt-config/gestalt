package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnvironmentVariablesTransformerTest {

    @Test
    void name() {
        EnvironmentVariablesTransformer transformer = new EnvironmentVariablesTransformer();
        Assertions.assertEquals("env", transformer.name());
    }

    @Test
    void process() {
        EnvironmentVariablesTransformer transformer = new EnvironmentVariablesTransformer();
        GResultOf<String> resultsOf = transformer.process("hello", "DB_IDLETIMEOUT", "");

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());
        Assertions.assertNotNull(resultsOf.results());
        String results = resultsOf.results();
        Assertions.assertEquals("123", results);
    }

    @Test
    void processMissing() {
        EnvironmentVariablesTransformer transformer = new EnvironmentVariablesTransformer();
        GResultOf<String> resultsOf = transformer.process("hello", "NO_EXIST", "");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("No Environment Variables found for: NO_EXIST, on path: hello during post process",
            resultsOf.getErrors().get(0).description());
    }

    @Test
    void processNull() {
        EnvironmentVariablesTransformer transformer = new EnvironmentVariablesTransformer();
        GResultOf<String> resultsOf = transformer.process("hello", null, "env:");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("Invalid string: env:, on path: hello in transformer: env",
            resultsOf.getErrors().get(0).description());
    }
}
