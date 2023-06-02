package org.github.gestalt.config.post.process.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UrlDecoderTransformerTest {

    @Test
    void name() {
        URLDecoderTransformer transformer = new URLDecoderTransformer();
        Assertions.assertEquals("urlDecode", transformer.name());
    }

    @Test
    void process() {
        URLDecoderTransformer transformer = new URLDecoderTransformer();
        var validateOfResults = transformer.process("db", "hello+world", "urlDecode:hello world");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertNotNull(validateOfResults.results());
        String results = validateOfResults.results();
        Assertions.assertEquals("hello world", results);
    }

    @Test
    void processNull() {
        URLDecoderTransformer transformer = new URLDecoderTransformer();
        var validateOfResults = transformer.process("db", null, "urlDecode:");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid string: urlDecode:, on path: db in transformer: urlDecode",
            validateOfResults.getErrors().get(0).description());
    }
}
