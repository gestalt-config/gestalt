package org.config.gestalt.source;

import org.config.gestalt.exceptions.GestaltException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileConfigSourceTest {

    @Test
    void loadFile() throws GestaltException {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        File testFile = new File(testFileURL.getFile());

        FileConfigSource fileConfigSource = new FileConfigSource(testFile);

        Assertions.assertTrue(fileConfigSource.hasStream());
        Assertions.assertNotNull(fileConfigSource.loadStream());
    }


    @Test
    void loadPath() throws GestaltException {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        File testFile = new File(testFileURL.getFile());

        FileConfigSource fileConfigSource = new FileConfigSource(testFile.toPath());

        Assertions.assertNotNull(fileConfigSource.loadStream());
    }

    @Test
    void loadFileNull() {
        NullPointerException exception = Assertions.assertThrows(NullPointerException.class, () -> new FileConfigSource((File) null));

        Assertions.assertEquals("file can not be null", exception.getMessage());
    }

    @Test
    void loadPathNullFile() {
        NullPointerException exception = Assertions.assertThrows(NullPointerException.class, () -> new FileConfigSource((Path) null));

        Assertions.assertEquals("Path can not be null", exception.getMessage());
    }

    @Test
    void loadPathNonExistentFile() {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        File testFile = new File(testFileURL.getFile() + ".notExist");

        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> new FileConfigSource(testFile));

        Assertions.assertTrue(exception.getMessage().startsWith("File does not exist from path"),
            "should start with File does not exist from path");
    }

    @Test
    void loadDirectory() {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        File testFile = new File(testFileURL.getFile());

        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> new FileConfigSource(testFile.toPath().getParent()));

        Assertions.assertTrue(exception.getMessage().startsWith("Path is not a regular file"),
            "should start with Path is not a regular file");
    }

    @Test
    void fileType() throws GestaltException {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        File testFile = new File(testFileURL.getFile());

        FileConfigSource fileConfigSource = new FileConfigSource(testFile.toPath());

        Assertions.assertEquals("properties", fileConfigSource.format());
    }

    @Test
    void name() throws GestaltException {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        File testFile = new File(testFileURL.getFile());

        FileConfigSource fileConfigSource = new FileConfigSource(testFile.toPath());

        assertThat(fileConfigSource.name())
            .contains("File source: ")
            .contains("test.properties");
    }


    @Test
    void noFileType() throws GestaltException {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test");
        File testFile = new File(testFileURL.getFile());

        FileConfigSource fileConfigSource = new FileConfigSource(testFile.toPath());

        Assertions.assertEquals("", fileConfigSource.format());
    }

    @Test
    void unsupportedList() throws GestaltException {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test");
        File testFile = new File(testFileURL.getFile());

        FileConfigSource fileConfigSource = new FileConfigSource(testFile.toPath());

        Assertions.assertFalse(fileConfigSource.hasList());
        Assertions.assertThrows(GestaltException.class, fileConfigSource::loadList);
    }
}
