package org.github.gestalt.config.post.process.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnvironmentVariablesTransformerTest {

    @Test
    void name() {
        EnvironmentVariablesTransformer transformer = new EnvironmentVariablesTransformer();
        Assertions.assertEquals("envVar", transformer.name());
    }

    @Test
    void process() {
        EnvironmentVariablesTransformer transformer = new EnvironmentVariablesTransformer();
        Assertions.assertEquals("123", transformer.process("hello", "DB_IDLETIMEOUT").get());
        Assertions.assertFalse(transformer.process("hello", "NO_VALUE").isPresent());
    }
}
