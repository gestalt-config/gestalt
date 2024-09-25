package org.github.gestalt.config.git.config;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.github.gestalt.config.entity.GestaltModuleConfig;

/**
 * Google specific configuration.
 * You can either specify the project ID or it will get it from the default.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class GitModuleConfig implements GestaltModuleConfig {

    private CredentialsProvider credentials;
    private SshSessionFactory sshSessionFactory;

    public GitModuleConfig() {
    }

    public GitModuleConfig(CredentialsProvider credentials, SshSessionFactory sshSessionFactory) {
        this.credentials = credentials;
        this.sshSessionFactory = sshSessionFactory;
    }

    @Override
    public String name() {
        return "git";
    }

    public boolean hasSshSessionFactory() {
        return sshSessionFactory != null;
    }

    public SshSessionFactory getSshSessionFactory() {
        return sshSessionFactory;
    }

    public void setSshSessionFactory(SshSessionFactory sshSessionFactory) {
        this.sshSessionFactory = sshSessionFactory;
    }

    public boolean hasCredentialsProvider() {
        return credentials != null;
    }

    public CredentialsProvider getCredentials() {
        return credentials;
    }

    public void setCredentials(CredentialsProvider credentials) {
        this.credentials = credentials;
    }
}
