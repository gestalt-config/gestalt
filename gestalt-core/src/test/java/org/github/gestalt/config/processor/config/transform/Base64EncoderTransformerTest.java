package org.github.gestalt.config.processor.config.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Base64EncoderTransformerTest {

    @Test
    void name() {
        Base64EncoderTransformer transformer = new Base64EncoderTransformer();
        Assertions.assertEquals("base64Encode", transformer.name());
    }

    @Test
    void process() {
        Base64EncoderTransformer transformer = new Base64EncoderTransformer();
        var resultsOf = transformer.process("db", "hello world", "base64Encode:hello world");

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());
        Assertions.assertNotNull(resultsOf.results());
        String results = resultsOf.results();
        Assertions.assertEquals("aGVsbG8gd29ybGQ=", results);
    }

    @Test
    void processNull() {
        Base64EncoderTransformer transformer = new Base64EncoderTransformer();
        var resultsOf = transformer.process("db", null, "base64Decode:");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("Invalid string: base64Decode:, on path: db in transformer: base64Encode",
            resultsOf.getErrors().get(0).description());
    }
}
