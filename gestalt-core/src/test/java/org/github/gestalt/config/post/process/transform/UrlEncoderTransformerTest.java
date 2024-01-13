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
        var resultsOf = transformer.process("db", "hello world", "urlEncode:hello world");

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());
        Assertions.assertNotNull(resultsOf.results());
        String results = resultsOf.results();
        Assertions.assertEquals("hello+world", results);
    }

    @Test
    void processNull() {
        URLEncoderTransformer transformer = new URLEncoderTransformer();
        var resultsOf = transformer.process("db", null, "urlEncode:");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("Invalid string: urlEncode:, on path: db in transformer: urlEncode",
            resultsOf.getErrors().get(0).description());
    }
}
