package org.github.gestalt.config.post.process.transform;

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
        var validateOfResults = transformer.process("db", "hello world", "base64Encode:hello world");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertNotNull(validateOfResults.results());
        String results = validateOfResults.results();
        Assertions.assertEquals("aGVsbG8gd29ybGQ=", results);
    }

    @Test
    void processNull() {
        Base64EncoderTransformer transformer = new Base64EncoderTransformer();
        var validateOfResults = transformer.process("db", null, "base64Decode:");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid string: base64Decode:, on path: db in transformer: base64Encode",
            validateOfResults.getErrors().get(0).description());
    }
}
