package org.github.gestalt.config.processor.config.transform;

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
        var resultsOf = transformer.process("db", "hello+world", "urlDecode:hello world");

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());
        Assertions.assertNotNull(resultsOf.results());
        String results = resultsOf.results();
        Assertions.assertEquals("hello world", results);
    }

    @Test
    void processNull() {
        URLDecoderTransformer transformer = new URLDecoderTransformer();
        var resultsOf = transformer.process("db", null, "urlDecode:");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("Invalid string: urlDecode:, on path: db in transformer: urlDecode",
            resultsOf.getErrors().get(0).description());
    }
}
