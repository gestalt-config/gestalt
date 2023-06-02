package org.github.gestalt.config.post.process.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Base64DecoderTransformerTest {

    @Test
    void name() {
        Base64DecoderTransformer transformer = new Base64DecoderTransformer();
        Assertions.assertEquals("base64Decode", transformer.name());
    }

    @Test
    void process() {
        Base64DecoderTransformer transformer = new Base64DecoderTransformer();
        var validateOfResults = transformer.process("db", "aGVsbG8gd29ybGQ=", "base64Decode:aGVsbG8gd29ybGQ=");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertNotNull(validateOfResults.results());
        String results = validateOfResults.results();
        Assertions.assertEquals("hello world", results);
    }

    @Test
    void processBadValue() {
        Base64DecoderTransformer transformer = new Base64DecoderTransformer();
        var validateOfResults = transformer.process("db", "@#*&(*&=", "base64Decode:@#*&(*&=");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid base 64 value: @#*&(*&=, on path: db in with error: Illegal base64 character 40",
            validateOfResults.getErrors().get(0).description());
    }

    @Test
    void processNull() {
        Base64DecoderTransformer transformer = new Base64DecoderTransformer();
        var validateOfResults = transformer.process("db", null, "base64Decode:");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid string: base64Decode:, on path: db in transformer: base64Decode",
            validateOfResults.getErrors().get(0).description());
    }
}
