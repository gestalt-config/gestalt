package org.github.gestalt.config.git;

import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.KeyPasswordProvider;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.util.FS;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.git.builder.GitModuleConfigBuilder;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationGitConfigTest {

    private Path configDirectory;

    @BeforeEach
    void setUp() throws IOException {
        configDirectory = Files.createTempDirectory("gitConfigTest");
        configDirectory.toFile().deleteOnExit();
    }

    @Test
    public void integrationTest() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=git,repoURI=https://github.com/gestalt-config/gestalt.git," +
            "configFilePath=gestalt-git/src/test/resources/include.properties," +
            "localRepoDirectory=" + configDirectory.toAbsolutePath());

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        assertEquals("a", gestalt.getConfig("a", String.class));
        assertEquals("b changed", gestalt.getConfig("b", String.class));
        assertEquals("c", gestalt.getConfig("c", String.class));
    }


    @Test
    void hasStreamWithPassword() throws GestaltException {

        // Must set the git user and password in Env Vars
        String userName = System.getenv("GIT_GESTALT_USER");
        String password = System.getenv("GIT_GESTALT_PASSWORD");

        Assumptions.assumeTrue(userName != null, "must have GIT_GESTALT_USER defined");
        Assumptions.assumeTrue(password != null, "must have GIT_GESTALT_PASSWORD defined");

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=git,repoURI=https://github.com/gestalt-config/gestalt.git," +
            "configFilePath=gestalt-git/src/test/resources/include.properties," +
            "localRepoDirectory=" + configDirectory.toAbsolutePath());

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(GitModuleConfigBuilder.builder()
                .setCredentials(new UsernamePasswordCredentialsProvider(userName, password))
                .build())
            .build();

        gestalt.loadConfigs();

        assertEquals("a", gestalt.getConfig("a", String.class));
        assertEquals("b changed", gestalt.getConfig("b", String.class));
        assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    void hasStreamWithGithubToken() throws GestaltException {

        // Must set the git user and password in Env Vars
        String githubToken = System.getenv("GITHUB_TOKEN");
        Assumptions.assumeTrue(githubToken != null, "must have GITHUB_TOKEN defined");

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=git,repoURI=https://github.com/gestalt-config/gestalt.git," +
            "configFilePath=gestalt-git/src/test/resources/include.properties," +
            "localRepoDirectory=" + configDirectory.toAbsolutePath());

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(GitModuleConfigBuilder.builder()
                .setCredentials(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .build())
            .build();

        gestalt.loadConfigs();

        assertEquals("a", gestalt.getConfig("a", String.class));
        assertEquals("b changed", gestalt.getConfig("b", String.class));
        assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    void hasStreamSSHWithPassword() throws GestaltException {

        // Must set the git user and password in Env Vars
        //String certLocation = System.getenv("GIT_GESTALT_SSH_LOCATION");
        String password = System.getenv("GIT_GESTALT_SSH_PASSWORD");
        Assumptions.assumeTrue(password != null, "must have GIT_GESTALT_SSH_PASSWORD defined");

        Path sshDir = FS.DETECTED.userHome().toPath().resolve(".ssh");

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

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=git,repoURI=https://github.com/gestalt-config/gestalt.git," +
            "configFilePath=gestalt-git/src/test/resources/include.properties," +
            "localRepoDirectory=" + configDirectory.toAbsolutePath());


        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(GitModuleConfigBuilder.builder()
                .setSshSessionFactory(sshdBuilder.build(new JGitKeyCache()))
                .build())
            .build();

        gestalt.loadConfigs();

        assertEquals("a", gestalt.getConfig("a", String.class));
        assertEquals("b changed", gestalt.getConfig("b", String.class));
        assertEquals("c", gestalt.getConfig("c", String.class));
    }
}
