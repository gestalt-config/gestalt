package org.github.gestalt.config.git;

import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.KeyPasswordProvider;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.util.FS;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// cicd isn't setup to run this test.
@SuppressWarnings("resource")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitConfigSourceTest {

    private Path configDirectory;

    @BeforeEach
    void setUp() throws IOException {
        configDirectory = Files.createTempDirectory("gitConfigTest");
        configDirectory.toFile().deleteOnExit();
    }

    @Test
    void noURI() throws IOException {
        Path configDirectory = Files.createTempDirectory("gitConfigTest");
        configDirectory.toFile().deleteOnExit();

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI(null)
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        GestaltException exception = Assertions.assertThrows(GestaltException.class, builder::build);

        Assertions.assertEquals("Must provide a git repo URI", exception.getMessage());
    }

    @Test
    void noConfigFilePath() throws IOException {
        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath(null)
            .setLocalRepoDirectory(configDirectory);
        GestaltException exception = Assertions.assertThrows(GestaltException.class, builder::build);

        Assertions.assertEquals("Must provide a path to the configuration file", exception.getMessage());
    }

    @Test
    void noLocalRepoFilePath() {
        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(null);
        GestaltException exception = Assertions.assertThrows(GestaltException.class, builder::build);

        Assertions.assertEquals("Must provide a local directory to sync to", exception.getMessage());
    }

    @Test
    void idTest() throws GestaltException, IOException {
        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();
        Assertions.assertNotNull(source.getConfigSource().id());
    }

    @Test
    void hasStream() throws GestaltException, IOException {
        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertTrue(source.getConfigSource().hasStream());
    }

    @Test
    void hasStreamWithPassword() throws GestaltException, IOException {
        // Must set the git user and password in Env Vars
        String userName = System.getenv("GIT_GESTALT_USER");
        String password = System.getenv("GIT_GESTALT_PASSWORD");

        Assumptions.assumeTrue(userName != null, "must have GIT_GESTALT_USER defined");
        Assumptions.assumeTrue(password != null, "must have GIT_GESTALT_PASSWORD defined");

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setCredentials(new UsernamePasswordCredentialsProvider(userName, password))
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertTrue(source.getConfigSource().hasStream());
        byte[] data = new byte[256];
        source.getConfigSource().loadStream().read(data);

        String configData = new String(data, StandardCharsets.UTF_8);

        Assertions.assertTrue(configData.startsWith("hello=world"));
    }

    @Test
    void hasStreamWithGithubToken() throws GestaltException, IOException {
        // Must set the git user and password in Env Vars
        String githubToken = System.getenv("GITHUB_TOKEN");
        Assumptions.assumeTrue(githubToken != null, "must have GITHUB_TOKEN defined");

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setCredentials(new UsernamePasswordCredentialsProvider(githubToken, ""))
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertTrue(source.getConfigSource().hasStream());
        byte[] data = new byte[256];
        source.getConfigSource().loadStream().read(data);

        String configData = new String(data, StandardCharsets.UTF_8);

        Assertions.assertTrue(configData.startsWith("hello=world"));
    }

    @Test
    @Disabled
    void hasStreamSSHWithPassword() throws GestaltException, IOException {
        // Must set the git user and password in Env Vars
        //String certLocation = System.getenv("GIT_GESTALT_SSH_LOCATION");
        String password = System.getenv("GIT_GESTALT_SSH_PASSWORD");
        Assumptions.assumeTrue(password != null, "must have GIT_GESTALT_SSH_PASSWORD defined");

        Path sshDir = FS.DETECTED.userHome().toPath().resolve(".ssh");

        Assumptions.assumeTrue(sshDir.toFile().exists());

        SshdSessionFactoryBuilder sshdBuilder = new SshdSessionFactoryBuilder()
            .setKeyPasswordProvider((cp) -> new KeyPasswordProvider() {
                @Override
                public char[] getPassphrase(URIish uri, int attempt) {
                    return password.toCharArray();
                }

                @Override
                public void setAttempts(int maxNumberOfAttempts) {

                }

                @Override
                public boolean keyLoaded(URIish uri, int attempt, Exception error) {
                    return true;
                }
            })
            .setHomeDirectory(FS.DETECTED.userHome())
            .setSshDirectory(sshDir.toFile());

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("git@github.com:gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory)
            .setSshSessionFactory(sshdBuilder.build(new JGitKeyCache()));
        ConfigSourcePackage source = builder.build();

        Assertions.assertTrue(source.getConfigSource().hasStream());
        byte[] data = new byte[256];
        source.getConfigSource().loadStream().read(data);

        String configData = new String(data, StandardCharsets.UTF_8);

        Assertions.assertTrue(configData.startsWith("hello=world"));
    }


    @Test
    void loadStream() throws IOException, GestaltException {
        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertTrue(source.getConfigSource().hasStream());
        byte[] data = new byte[256];
        source.getConfigSource().loadStream().read(data);

        String configData = new String(data, StandardCharsets.UTF_8);

        Assertions.assertTrue(configData.startsWith("hello=world"));
    }

    @Test
    void hasList() throws GestaltException, IOException {

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertFalse(source.getConfigSource().hasList());
    }

    @Test
    void loadList() throws GestaltException, IOException {
        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertFalse(source.getConfigSource().hasList());

        GestaltException exception = Assertions.assertThrows(GestaltException.class, () -> source.getConfigSource().loadList());

        Assertions.assertEquals("Unsupported operation loadList on an GitConfigSource", exception.getMessage());
    }

    @Test
    void format() throws GestaltException, IOException {

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertEquals("properties", source.getConfigSource().format());
    }

    @Test
    void formatEmpty() throws GestaltException, IOException {

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertEquals("", source.getConfigSource().format());
    }

    @Test
    void name() throws GestaltException, IOException {
        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertEquals("Git Config Source key: gestalt-git/src/test/resources/default.properties",
            source.getConfigSource().name());
    }

    @Test
    void testEquals() throws IOException, GestaltException {

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();
        ConfigSourcePackage source2 = builder.build();

        Assertions.assertEquals(source.getConfigSource(), source.getConfigSource());
        Assertions.assertNotEquals(source.getConfigSource(), source2.getConfigSource());
        Assertions.assertNotEquals(source.getConfigSource(), null);

        Assertions.assertNotEquals(source.getConfigSource(), 4);

    }

    @Test
    void testHashCode() throws IOException, GestaltException {

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = builder.build();

        Assertions.assertTrue(source.getConfigSource().hashCode() != 0);
    }

    @Test
    void testTags() throws IOException, GestaltException {

        GitConfigSourceBuilder builder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-git/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory)
            .setTags(Tags.of("toy", "ball"));
        ConfigSourcePackage source = builder.build();

        Assertions.assertEquals(Tags.of("toy", "ball"), source.getTags());
    }
}
