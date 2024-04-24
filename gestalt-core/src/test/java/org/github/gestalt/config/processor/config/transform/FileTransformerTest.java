package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.integration.GestaltIntegrationTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

class FileTransformerTest {

    @Test
    void name() {
        FileTransformer transformer = new FileTransformer();
        Assertions.assertEquals("file", transformer.name());
    }

    @Test
    void process() {
        URL devFileURL = GestaltIntegrationTests.class.getClassLoader().getResource("myFile.txt");

        FileTransformer transformer = new FileTransformer();
        File testFile = new File(devFileURL.getFile());
        var resultsOf = transformer.process("db", testFile.getAbsolutePath(), "file:" + testFile.getAbsolutePath());

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());
        Assertions.assertNotNull(resultsOf.results());
        String results = resultsOf.results();
        Assertions.assertTrue(results.startsWith("hello world"));
    }

    @Test
    void processFileDoesntExist() {

        FileTransformer transformer = new FileTransformer();
        var resultsOf = transformer.process("db", "^&ASD*A&N&*A^BD(*&", "file:^&ASD*A&N&*A^BD(*&");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertTrue(resultsOf.getErrors().get(0).description()
            .startsWith("Exception transforming file while reading file"));
    }

    @Test
    void processNull() {
        FileTransformer transformer = new FileTransformer();
        var resultsOf = transformer.process("db", null, "file:");

        Assertions.assertFalse(resultsOf.hasResults());
        Assertions.assertTrue(resultsOf.hasErrors());

        Assertions.assertEquals(1, resultsOf.getErrors().size());
        Assertions.assertEquals("Invalid string: file:, on path: db in transformer: file",
            resultsOf.getErrors().get(0).description());
    }
}
