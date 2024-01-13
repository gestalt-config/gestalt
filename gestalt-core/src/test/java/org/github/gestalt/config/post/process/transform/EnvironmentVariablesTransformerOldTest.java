package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnvironmentVariablesTransformerOldTest {

    @Test
    void name() {
        EnvironmentVariablesTransformerOld transformer = new EnvironmentVariablesTransformerOld();
        Assertions.assertEquals("envVar", transformer.name());
    }

    @Test
    void process() {
        EnvironmentVariablesTransformerOld transformer = new EnvironmentVariablesTransformerOld();
        GResultOf<String> resultsOf = transformer.process("hello", "DB_IDLETIMEOUT", "");

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());
        Assertions.assertNotNull(resultsOf.results());
        String results = resultsOf.results();
        Assertions.assertEquals("123", results);
    }

    @Test
    void processMissing() {
        EnvironmentVariablesTransformerOld transformer = new EnvironmentVariablesTransformerOld();
        GResultOf<String> resultsOf = transformer.process("hello", "NO_EXIST", "");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("No Environment Variables found for: NO_EXIST, on path: hello during post process",
            resultsOf.getErrors().get(0).description());
    }

    @Test
    void processNull() {
        EnvironmentVariablesTransformerOld transformer = new EnvironmentVariablesTransformerOld();
        GResultOf<String> resultsOf = transformer.process("hello", null, "env:");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("Invalid string: env:, on path: hello in transformer: envVar",
            resultsOf.getErrors().get(0).description());
    }
}
