package org.github.gestalt.config.processor.config.transform;

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
        var resultsOf = transformer.process("db", "myFile.txt", "classpath:myFile.txt");

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());
        Assertions.assertNotNull(resultsOf.results());
        String results = resultsOf.results();
        Assertions.assertTrue(results.startsWith("hello world"));
    }

    @Test
    void processNull() {
        ClasspathTransformer transformer = new ClasspathTransformer();
        var resultsOf = transformer.process("db", null, "classpath:");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("Invalid string: classpath:, on path: db in transformer: classpath",
            resultsOf.getErrors().get(0).description());
    }
}
