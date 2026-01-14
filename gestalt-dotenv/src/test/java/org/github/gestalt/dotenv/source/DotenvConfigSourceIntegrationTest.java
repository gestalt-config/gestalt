package org.github.gestalt.dotenv.source;

import io.github.cdimascio.dotenv.Dotenv;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.utils.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DotenvConfigSourceIntegrationTest {

    @Test
    void loadListFromRealDotenv() throws IOException, GestaltException {
        // Create a temporary .env file
        Path tempFile = Files.createTempFile("test", ".env");
        tempFile.toFile().deleteOnExit();

        String content = "TEST_KEY=hello_world\nANOTHER=42\n# a comment\n";
        Files.writeString(tempFile, content);

        // Build a Dotenv that loads from the temp file by specifying directory and filename
        // Dotenv loads .env from the working directory by default; use directory and filename options
        Dotenv dotenv = Dotenv.configure()
            .directory(tempFile.getParent().toString())
            .filename(tempFile.getFileName().toString())
            .load();

        DotenvConfigSource source = new DotenvConfigSource(dotenv);

        List<Pair<String, String>> list = source.loadList();
        assertNotNull(list);
        // Convert to a map-like check
        List<String> keys = list.stream().map(Pair::getFirst).collect(Collectors.toList());
        assertTrue(keys.contains("TEST_KEY"));
        assertTrue(keys.contains("ANOTHER"));

        // verify values
        assertTrue(list.stream().anyMatch(p -> p.getFirst().equals("TEST_KEY") && p.getSecond().equals("hello_world")));
        assertTrue(list.stream().anyMatch(p -> p.getFirst().equals("ANOTHER") && p.getSecond().equals("42")));
    }
}

