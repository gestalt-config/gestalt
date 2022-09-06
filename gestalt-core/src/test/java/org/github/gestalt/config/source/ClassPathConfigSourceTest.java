package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassPathConfigSourceTest {

    @Test
    void loadFile() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("/test.properties");

        Assertions.assertTrue(classPathConfigSource.hasStream());
        Assertions.assertNotNull(classPathConfigSource.loadStream());
    }

    @Test
    void loadFileNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> new ClassPathConfigSource(null));

        Assertions.assertEquals("Class path resource cannot be null", exception.getMessage());
    }

    @Test
    void loadPathNonExistentFile() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("/test.properties.notExist");
        GestaltException exception = Assertions.assertThrows(GestaltException.class, classPathConfigSource::loadStream);

        Assertions.assertEquals("Unable to load classpath resource from /test.properties.notExist", exception.getMessage());
    }

    @Test
    void fileType() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("/test.properties");

        Assertions.assertEquals("properties", classPathConfigSource.format());
    }

    @Test
    void fileTypeJson() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("/test.json");

        Assertions.assertEquals("json", classPathConfigSource.format());
    }

    @Test
    void name() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("/test.properties");

        Assertions.assertEquals("Class Path resource: /test.properties", classPathConfigSource.name());
    }


    @Test
    void noFileType() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("/test");

        Assertions.assertEquals("", classPathConfigSource.format());
    }

    @Test
    void unsupportedList() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("test.properties");

        Assertions.assertFalse(classPathConfigSource.hasList());
        Assertions.assertThrows(GestaltException.class, classPathConfigSource::loadList);
    }

    @Test
    void equals() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("/test.properties");

        ClassPathConfigSource classPathConfigSource2 = new ClassPathConfigSource("/test.properties");

        Assertions.assertEquals(classPathConfigSource, classPathConfigSource);
        Assertions.assertNotEquals(classPathConfigSource, classPathConfigSource2);
        Assertions.assertNotEquals(classPathConfigSource, null);
        Assertions.assertNotEquals(classPathConfigSource, 1L);
    }

    @Test
    void hash() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("/test.properties");
        Assertions.assertTrue(classPathConfigSource.hashCode() != 0);
    }

    @Test
    void tags() throws GestaltException {
        ClassPathConfigSource classPathConfigSource = new ClassPathConfigSource("/test.properties", Tags.of("toy", "ball"));
        Assertions.assertEquals(Tags.of("toy", "ball"), classPathConfigSource.getTags());
    }
}
