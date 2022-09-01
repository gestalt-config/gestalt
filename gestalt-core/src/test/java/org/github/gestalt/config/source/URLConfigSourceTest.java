package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class URLConfigSourceTest {
    private static final String testURL = "https://raw.githubusercontent.com/gestalt-config/gestalt/main/gestalt-core/src/test/resources/default.properties";
    private static final String badFormattedTestURL = "test file";

    @Test
    void loadFile() throws GestaltException {
        URLConfigSource fileConfigSource = new URLConfigSource(testURL);

        Assertions.assertTrue(fileConfigSource.hasStream());
        Assertions.assertNotNull(fileConfigSource.loadStream());
    }


    @Test
    void loadPath() throws GestaltException {
        URLConfigSource fileConfigSource = new URLConfigSource(testURL);

        Assertions.assertNotNull(fileConfigSource.loadStream());
    }

    @Test
    void loadFileNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> new URLConfigSource(null));

        Assertions.assertEquals("The url string provided was null", exception.getMessage());
    }

    @Test
    void loadPathNotURL() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> new URLConfigSource(badFormattedTestURL));

        Assertions.assertEquals(exception.getMessage(), "Exception creating URL test file, with error: no protocol: test file");
    }

    @Test
    void name() throws GestaltException {
        URLConfigSource fileConfigSource = new URLConfigSource(testURL);

        assertThat(fileConfigSource.name())
            .contains("URL format: ")
            .contains(testURL);
    }


    @Test
    void fileType() throws GestaltException {
        URLConfigSource fileConfigSource = new URLConfigSource(testURL);

        Assertions.assertEquals("properties", fileConfigSource.format());
    }

    @Test
    void unsupportedList() throws GestaltException {
        URLConfigSource fileConfigSource = new URLConfigSource(testURL);

        Assertions.assertFalse(fileConfigSource.hasList());
        Assertions.assertThrows(GestaltException.class, fileConfigSource::loadList);
    }

    @Test
    void equals() throws GestaltException {
        URLConfigSource fileConfigSource = new URLConfigSource(testURL);
        URLConfigSource fileConfigSource2 = new URLConfigSource(testURL);

        Assertions.assertEquals(fileConfigSource, fileConfigSource);
        Assertions.assertNotEquals(fileConfigSource, fileConfigSource2);
        Assertions.assertNotEquals(fileConfigSource, null);
        Assertions.assertNotEquals(fileConfigSource, 1L);
    }

    @Test
    void hash() throws GestaltException {
        URLConfigSource fileConfigSource = new URLConfigSource(testURL);
        Assertions.assertTrue(fileConfigSource.hashCode() != 0);
    }

    @Test
    void tags() throws GestaltException {
        URLConfigSource fileConfigSource = new URLConfigSource(testURL, Tags.of("toy", "ball"));
        Assertions.assertEquals(Tags.of("toy", "ball"), fileConfigSource.getTags());
    }
}
