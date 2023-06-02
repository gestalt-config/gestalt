package org.github.gestalt.config.post.process.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UrlEncoderTransformerTest {

    @Test
    void name() {
        URLEncoderTransformer transformer = new URLEncoderTransformer();
        Assertions.assertEquals("urlEncode", transformer.name());
    }

    @Test
    void process() {
        URLEncoderTransformer transformer = new URLEncoderTransformer();
        var validateOfResults = transformer.process("db", "hello world", "urlEncode:hello world");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertNotNull(validateOfResults.results());
        String results = validateOfResults.results();
        Assertions.assertEquals("hello+world", results);
    }

    @Test
    void processNull() {
        URLEncoderTransformer transformer = new URLEncoderTransformer();
        var validateOfResults = transformer.process("db", null, "urlEncode:");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid string: urlEncode:, on path: db in transformer: urlEncode",
            validateOfResults.getErrors().get(0).description());
    }
}
