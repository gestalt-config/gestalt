package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringConfigSourceTest {

    @Test
    void loadFile() throws GestaltException {
        StringConfigSource source = new StringConfigSource("test=abc", "properties");

        Assertions.assertTrue(source.hasStream());
        Assertions.assertNotNull(source.loadStream());
    }

    @Test
    void loadStringNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> new StringConfigSource(null, "properties"));

        Assertions.assertEquals("The string provided was null", exception.getMessage());
    }

    @Test
    void loadFormatNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> new StringConfigSource("test=abc", null));

        Assertions.assertEquals("The string format provided was null", exception.getMessage());
    }


    @Test
    void fileType() throws GestaltException {
        StringConfigSource source = new StringConfigSource("test", "properties");

        Assertions.assertEquals("properties", source.format());
    }

    @Test
    void fileTypeJson() throws GestaltException {
        StringConfigSource source = new StringConfigSource("test", "json");

        Assertions.assertEquals("json", source.format());
    }

    @Test
    void name() throws GestaltException {
        StringConfigSource source = new StringConfigSource("test", "properties");

        Assertions.assertEquals("String format: properties", source.name());
    }

    @Test
    void unsupportedList() throws GestaltException {
        StringConfigSource source = new StringConfigSource("test", "properties");

        Assertions.assertFalse(source.hasList());
        Assertions.assertThrows(GestaltException.class, source::loadList);
    }

    @Test
    void equals() throws GestaltException {
        StringConfigSource source = new StringConfigSource("test", "properties");

        StringConfigSource source2 = new StringConfigSource("test", "properties");

        Assertions.assertEquals(source, source);
        Assertions.assertNotEquals(source, source2);
        Assertions.assertNotEquals(source, null);
        Assertions.assertNotEquals(source, 1L);
    }

    @Test
    void hash() throws GestaltException {
        StringConfigSource source = new StringConfigSource("test", "properties");
        Assertions.assertTrue(source.hashCode() != 0);
    }

    @Test
    void tags() throws GestaltException {
        StringConfigSource source = new StringConfigSource("test", "properties", Tags.of("toy", "ball"));
        Assertions.assertEquals(Tags.of("toy", "ball"), source.getTags());
    }
}
