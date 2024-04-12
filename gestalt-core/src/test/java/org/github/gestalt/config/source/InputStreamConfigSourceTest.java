package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

class InputStreamConfigSourceTest {

    @Test
    void loadFile() throws GestaltException {
        InputStreamConfigSource source = new InputStreamConfigSource(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "properties");

        Assertions.assertTrue(source.hasStream());
        Assertions.assertNotNull(source.loadStream());
    }

    @Test
    void loadStringNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> new InputStreamConfigSource(null, "properties"));

        Assertions.assertEquals("The InputStream provided was null", exception.getMessage());
    }

    @Test
    void loadFormatNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> new InputStreamConfigSource(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), null));

        Assertions.assertEquals("The InputStream format provided was null", exception.getMessage());
    }


    @Test
    void fileType() throws GestaltException {
        InputStreamConfigSource source = new InputStreamConfigSource(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "properties");

        Assertions.assertEquals("properties", source.format());
    }

    @Test
    void fileTypeJson() throws GestaltException {
        InputStreamConfigSource source = new InputStreamConfigSource(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "json");

        Assertions.assertEquals("json", source.format());
    }

    @Test
    void name() throws GestaltException {
        InputStreamConfigSource source = new InputStreamConfigSource(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "properties");

        Assertions.assertEquals("String format: properties", source.name());
    }

    @Test
    void unsupportedList() throws GestaltException {
        InputStreamConfigSource source = new InputStreamConfigSource(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "properties");

        Assertions.assertFalse(source.hasList());
        Assertions.assertThrows(GestaltException.class, source::loadList);
    }

    @Test
    void equals() throws GestaltException {
        InputStreamConfigSource source = new InputStreamConfigSource(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "properties");

        InputStreamConfigSource source2 = new InputStreamConfigSource(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "properties");

        Assertions.assertEquals(source, source);
        Assertions.assertNotEquals(source, source2);
        Assertions.assertNotEquals(source, null);
        Assertions.assertNotEquals(source, 1L);
    }

    @Test
    void hash() throws GestaltException {
        InputStreamConfigSource source = new InputStreamConfigSource(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "properties");

        Assertions.assertTrue(source.hashCode() != 0);
    }

    @Test
    @SuppressWarnings("removal")
    void tags() throws GestaltException {
        InputStreamConfigSource source = new InputStreamConfigSource(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), "properties");
        Assertions.assertEquals(Tags.of(), source.getTags());
    }
}
