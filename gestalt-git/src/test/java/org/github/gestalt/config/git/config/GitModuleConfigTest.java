package org.github.gestalt.config.git.config;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GitModuleConfigTest {

    private CredentialsProvider mockCredentialsProvider;
    private SshSessionFactory mockSshSessionFactory;
    private GitModuleConfig gitModuleConfig;

    @BeforeEach
    void setUp() throws IOException {
        // Mock the dependencies
        mockCredentialsProvider = mock(CredentialsProvider.class);
        mockSshSessionFactory = mock(SshSessionFactory.class);

        // Initialize GitModuleConfig with mocks
        gitModuleConfig = new GitModuleConfig(mockCredentialsProvider, mockSshSessionFactory);
    }

    @Test
    void testConstructorInitialization() {
        // Check that the constructor initializes the fields correctly
        assertEquals(mockCredentialsProvider, gitModuleConfig.getCredentials(), "Credentials should match the provided mock");
        assertEquals(mockSshSessionFactory, gitModuleConfig.getSshSessionFactory(), "SSH Session Factory should match the provided mock");
    }

    @Test
    void testConstructorInitialization2() {
        // Initialize GitModuleConfig with mocks
        gitModuleConfig = new GitModuleConfig();
        // Check that the constructor initializes the fields correctly
        assertFalse(gitModuleConfig.hasCredentialsProvider());
        assertNull(gitModuleConfig.getCredentials());
        assertFalse(gitModuleConfig.hasSshSessionFactory());
        assertNull(gitModuleConfig.getSshSessionFactory());
    }

    @Test
    void testSetCredentials() {
        CredentialsProvider newMockCredentials = mock(CredentialsProvider.class);
        gitModuleConfig.setCredentials(newMockCredentials);

        assertEquals(newMockCredentials, gitModuleConfig.getCredentials(), "Credentials should be updated correctly");
    }

    @Test
    void testSetSshSessionFactory() {
        SshSessionFactory newMockSshSessionFactory = mock(SshSessionFactory.class);
        gitModuleConfig.setSshSessionFactory(newMockSshSessionFactory);

        assertEquals(newMockSshSessionFactory, gitModuleConfig.getSshSessionFactory(), "SSH Session Factory should be updated correctly");
    }

    @Test
    void testNameMethod() {
        // Check that the name method returns "git"
        assertEquals("git", gitModuleConfig.name(), "Module name should be 'git'");
    }


}
