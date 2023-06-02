package org.github.gestalt.config.post.process.transform;

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
        var validateOfResults = transformer.process("db", testFile.getAbsolutePath(), "file:" + testFile.getAbsolutePath());

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        Assertions.assertNotNull(validateOfResults.results());
        String results = validateOfResults.results();
        Assertions.assertTrue(results.startsWith("hello world"));
    }

    @Test
    void processNull() {
        FileTransformer transformer = new FileTransformer();
        var validateOfResults = transformer.process("db", null, "file:");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid string: file:, on path: db in transformer: file",
            validateOfResults.getErrors().get(0).description());
    }
}
