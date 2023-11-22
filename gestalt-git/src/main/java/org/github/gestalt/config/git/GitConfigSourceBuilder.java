package org.github.gestalt.config.git;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;

import java.nio.file.Path;

/**
 * Builder that allows you to construct a GitConfigSource.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class GitConfigSourceBuilder extends SourceBuilder<GitConfigSourceBuilder, GitConfigSource> {
    private String repoURI;
    private Path localRepoDirectory;
    private String configFilePath;
    private String branch;
    private CredentialsProvider credentials;
    private SshSessionFactory sshSessionFactory;

    /**
     * private constructor, use the builder method.
     */
    private GitConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static GitConfigSourceBuilder builder() {
        return new GitConfigSourceBuilder();
    }

    /**
     * Set the URI to the git repo. Depending on your authentication method it can be either https or git or sshd.
     *
     * @param repoURI the URI to the git repo. Depending on your authentication method it can be either https or git or sshd
     * @return the builder
     */
    public GitConfigSourceBuilder setRepoURI(String repoURI) {
        this.repoURI = repoURI;
        return this;
    }

    /**
     * Set the local directory you want to save the git repo to.
     *
     * @param localRepoDirectory the local directory you want to save the git repo to.
     * @return the builder
     */
    public GitConfigSourceBuilder setLocalRepoDirectory(Path localRepoDirectory) {
        this.localRepoDirectory = localRepoDirectory;
        return this;
    }

    /**
     * Set the path to the config file in the git repo.
     *
     * @param configFilePath the path to the config file in the git repo
     * @return the builder
     */
    public GitConfigSourceBuilder setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
        return this;
    }

    /**
     * Set the branch you want to pull from git.
     *
     * @param branch the branch you want to pull from git
     * @return the builder
     */
    public GitConfigSourceBuilder setBranch(String branch) {
        this.branch = branch;
        return this;
    }

    /**
     * Set the credentials for the git config source.
     *
     * @param credentials If authenticating with credentials, the CredentialsProvider such as UsernamePasswordCredentialsProvider
     * @return the builder
     */
    public GitConfigSourceBuilder setCredentials(CredentialsProvider credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * Setup the ssh session factory.
     *
     * @param sshSessionFactory If using sshd the SshSessionFactory, this uses  apache mina-sshd.
     *     The easiest way is to use the apache mina-sshd SshdSessionFactoryBuilder.
     * @return the builder
     */
    public GitConfigSourceBuilder setSshSessionFactory(SshSessionFactory sshSessionFactory) {
        this.sshSessionFactory = sshSessionFactory;
        return this;
    }


    /**
     * Builds the GitConfigSource, The GitConfigSource will try and download the repo to the provided folder.
     * So if there are any errors it will happen during construction.
     *
     * @return the built config source
     * @throws GestaltException any exceptions thrown while constructing the GitConfigSource.
     */
    @Override
    public ConfigSourcePackage<GitConfigSource> build() throws GestaltException {
        return buildPackage(new GitConfigSource(repoURI, localRepoDirectory, configFilePath, branch, credentials, sshSessionFactory, tags));
    }
}
