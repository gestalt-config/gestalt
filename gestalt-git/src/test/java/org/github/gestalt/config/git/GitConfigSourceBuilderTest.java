package org.github.gestalt.config.git;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GitConfigSourceBuilderTest {

    private final Path localRepoDirectory = Path.of("/local/repo");
    private final CredentialsProvider credentials = mock(CredentialsProvider.class);
    private final SshSessionFactory sshSessionFactory = mock(SshSessionFactory.class);
    private GitConfigSourceBuilder builder;

    @BeforeEach
    void setUp() {
        builder = GitConfigSourceBuilder.builder();
    }

    @Test
    void testSetRepoURI() {
        assertNotNull(builder);
        builder.setRepoURI("https://example.com/repo.git");
        builder.setLocalRepoDirectory(localRepoDirectory);
        builder.setConfigFilePath("config.yaml");
        builder.setBranch("main");
        builder.setCredentials(credentials);
        builder.setSshSessionFactory(sshSessionFactory);
    }
}
