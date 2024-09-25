package org.github.gestalt.config.git.builder;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.github.gestalt.config.git.config.GitModuleConfig;

/**
 * Builder for creating Git specific configuration.
 * You can either specify the CredentialsProvider or SshSessionFactory.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class GitModuleConfigBuilder {
    private CredentialsProvider credentials;
    private SshSessionFactory sshSessionFactory;

    private GitModuleConfigBuilder() {

    }

    /**
     * Create a builder to create the Google config.
     *
     * @return a builder to create the Google config.
     */
    public static GitModuleConfigBuilder builder() {
        return new GitModuleConfigBuilder();
    }

    /**
     * Get the credentials.
     *
     * @return the credentials.
     */
    public CredentialsProvider getCredentials() {
        return credentials;
    }

    /**
     * Sets the credentials.
     *
     * @param credentials the credentials.
     * @return the builder
     */
    public GitModuleConfigBuilder setCredentials(CredentialsProvider credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * Get the SshSessionFactory.
     *
     * @return the SshSessionFactory
     */
    public SshSessionFactory getSshSessionFactory() {
        return sshSessionFactory;
    }

    /**
     * Set the SshSessionFactory.
     *
     * @param sshSessionFactory the SshSessionFactory
     * @return the builder.
     */
    public GitModuleConfigBuilder setSshSessionFactory(SshSessionFactory sshSessionFactory) {
        this.sshSessionFactory = sshSessionFactory;
        return this;
    }

    public GitModuleConfig build() {
        return new GitModuleConfig(credentials, sshSessionFactory);
    }
}
