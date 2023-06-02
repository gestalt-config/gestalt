package org.github.gestalt.config.post.process.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClasspathTransformerTest {

    @Test
    void name() {
        ClasspathTransformer transformer = new ClasspathTransformer();
        Assertions.assertEquals("classpath", transformer.name());
    }

    @Test
    void process() {

        ClasspathTransformer transformer = new ClasspathTransformer();
        var validateOfResults = transformer.process("db", "myFile.txt", "classpath:myFile.txt");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertNotNull(validateOfResults.results());
        String results = validateOfResults.results();
        Assertions.assertTrue(results.startsWith("hello world"));
    }

    @Test
    void processNull() {
        ClasspathTransformer transformer = new ClasspathTransformer();
        var validateOfResults = transformer.process("db", null, "classpath:");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid string: classpath:, on path: db in transformer: classpath",
            validateOfResults.getErrors().get(0).description());
    }
}
