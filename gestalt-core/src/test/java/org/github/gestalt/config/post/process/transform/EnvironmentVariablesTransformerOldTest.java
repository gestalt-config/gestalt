package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.utils.ValidateOf;
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
        ValidateOf<String> validateOfResults = transformer.process("hello", "DB_IDLETIMEOUT");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertNotNull(validateOfResults.results());
        String results = validateOfResults.results();
        Assertions.assertEquals("123", results);
    }

    @Test
    void processMissing() {
        EnvironmentVariablesTransformerOld transformer = new EnvironmentVariablesTransformerOld();
        ValidateOf<String> validateOfResults = transformer.process("hello", "NO_EXIST");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("No Environment Variables found for: NO_EXIST, on path: hello during post process",
            validateOfResults.getErrors().get(0).description());
    }
}
