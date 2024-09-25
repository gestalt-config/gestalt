package org.github.gestalt.config.git.builder;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.github.gestalt.config.git.config.GitModuleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GitModuleConfigBuilderTest {

    private CredentialsProvider mockCredentialsProvider;
    private SshSessionFactory mockSshSessionFactory;

    @BeforeEach
    void setUp() {
        // Mock the dependencies
        mockCredentialsProvider = mock(CredentialsProvider.class);
        mockSshSessionFactory = mock(SshSessionFactory.class);
    }

    @Test
    void testBuilderInitialization() {
        // Test that the builder is initialized properly
        GitModuleConfigBuilder builder = GitModuleConfigBuilder.builder();
        assertNotNull(builder, "Builder should not be null");
    }

    @Test
    void testSetCredentials() {
        // Use the builder to set credentials and verify it's stored correctly
        GitModuleConfigBuilder builder = GitModuleConfigBuilder.builder()
            .setCredentials(mockCredentialsProvider);

        assertEquals(mockCredentialsProvider, builder.getCredentials(), "Credentials should match the mock provided");
    }

    @Test
    void testSetSshSessionFactory() {
        // Use the builder to set the SSH session factory and verify it's stored correctly
        GitModuleConfigBuilder builder = GitModuleConfigBuilder.builder()
            .setSshSessionFactory(mockSshSessionFactory);

        assertEquals(mockSshSessionFactory, builder.getSshSessionFactory(), "SSH Session Factory should match the mock provided");
    }

    @Test
    void testBuild() {
        // Build the GitModuleConfig with the mocked credentials and SSH session factory
        GitModuleConfig gitModuleConfig = GitModuleConfigBuilder.builder()
            .setCredentials(mockCredentialsProvider)
            .setSshSessionFactory(mockSshSessionFactory)
            .build();

        // Verify that the created GitModuleConfig has the correct values
        assertEquals(mockCredentialsProvider, gitModuleConfig.getCredentials());
        assertEquals(mockSshSessionFactory, gitModuleConfig.getSshSessionFactory());
    }

    @Test
    void testBuildWithoutSettingCredentials() {
        // Build the GitModuleConfig without setting credentials to ensure it handles null values
        GitModuleConfig gitModuleConfig = GitModuleConfigBuilder.builder()
            .setSshSessionFactory(mockSshSessionFactory)
            .build();

        // Credentials should be null, SSH session factory should be set
        assertNull(gitModuleConfig.getCredentials(), "Credentials should be null if not set");
        assertEquals(mockSshSessionFactory, gitModuleConfig.getSshSessionFactory(), "SSH Session Factory should match the mock");
    }

    @Test
    void testBuildWithoutSettingSshSessionFactory() {
        // Build the GitModuleConfig without setting the SSH session factory to ensure it handles null values
        GitModuleConfig gitModuleConfig = GitModuleConfigBuilder.builder()
            .setCredentials(mockCredentialsProvider)
            .build();

        // SSH session factory should be null, credentials should be set
        assertEquals(mockCredentialsProvider, gitModuleConfig.getCredentials(), "Credentials should match the mock provided");
        assertNull(gitModuleConfig.getSshSessionFactory(), "SSH Session Factory should be null if not set");
    }
}
