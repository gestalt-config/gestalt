package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KubernetesSecretConfigSourceTest {

    private Path kubernetesPath;

    @BeforeAll
    public void setup() throws URISyntaxException {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        Path testFileDir = Paths.get(testFileURL.toURI());
        kubernetesPath = testFileDir.getParent().resolve("kubernetes");
    }

    @Test
    void getPath() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        Assertions.assertEquals(kubernetesPath, source.getPath());
    }

    @Test
    void setupNullPath() {
        NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
            () -> new KubernetesSecretConfigSource((Path) null));
        Assertions.assertEquals("Kubernetes Secret path can not be null", exception.getMessage());
    }

    @Test
    void setupNullFile() {
        NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
            () -> new KubernetesSecretConfigSource((File) null));
        Assertions.assertEquals("Kubernetes Secret file can not be null", exception.getMessage());
    }

    @Test
    void setupNotADirectory() throws URISyntaxException {
        URL testFileURL = FileConfigSourceTest.class.getClassLoader().getResource("test.properties");
        Path testFileDir = Paths.get(testFileURL.toURI());

        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> new KubernetesSecretConfigSource(testFileDir.toFile()));
        Assertions.assertEquals("Kubernetes Secret path does not exist at: " + testFileDir, exception.getMessage());
    }

    @Test
    void setupEmptyDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("test" + Math.random());

        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> new KubernetesSecretConfigSource(tempDir));
        Assertions.assertEquals("Kubernetes Secret path is empty: " + tempDir.toString(), exception.getMessage());
    }

    @Test
    void hasStream() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        Assertions.assertFalse(source.hasStream());
    }

    @Test
    void loadStream() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);

        Assertions.assertThrows(GestaltException.class, source::loadStream);
    }

    @Test
    void hasList() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        Assertions.assertTrue(source.hasList());
    }

    @Test
    void loadList() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        var results = source.loadList();
        assertThat(results).hasSize(3)
            .contains(new Pair<>("db.host.password", "abcdef"))
            .contains(new Pair<>("db.host.uri", "jdbc:postgresql://localhost:5432/mydb1"))
            .contains(new Pair<>("subservice.booking.token", "111222333"));
    }

    @Test
    void format() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        Assertions.assertEquals("mapConfig", source.format());
    }

    @Test
    void name() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        assertThat(source.name()).startsWith("Kubernetes Secret source: ");
    }

    @Test
    void idTest() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        KubernetesSecretConfigSource source2 = new KubernetesSecretConfigSource(kubernetesPath);
        Assertions.assertNotEquals(source.id(), source2.id());
    }

    @Test
    void getTags() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        Assertions.assertEquals(Tags.of(), source.getTags());
    }

    @Test
    void getTagsWithTags() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath, Tags.of("env", "dev"));
        Assertions.assertEquals(Tags.of("env", "dev"), source.getTags());
    }

    @Test
    @SuppressWarnings("removal")
    void getTagsWithTagsFile() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath.toFile(), Tags.of("env", "dev"));
        Assertions.assertEquals(Tags.of("env", "dev"), source.getTags());
    }

    @Test
    void testEquals() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        KubernetesSecretConfigSource source2 = new KubernetesSecretConfigSource(kubernetesPath);

        Assertions.assertEquals(source, source);
        Assertions.assertNotEquals(source, source2);
        Assertions.assertNotEquals(source, null);
        Assertions.assertNotEquals(source, 1L);
    }

    @Test
    void testHashCode() throws GestaltException {
        KubernetesSecretConfigSource source = new KubernetesSecretConfigSource(kubernetesPath);
        Assertions.assertTrue(source.hashCode() != 0);
    }
}
