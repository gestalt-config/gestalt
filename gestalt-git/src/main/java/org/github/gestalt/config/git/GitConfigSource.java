package org.github.gestalt.config.git;


import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Loads a file from git.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class GitConfigSource implements ConfigSource {

    private final UUID id = UUID.randomUUID();
    private final Path localRepoDirectory;
    private final String configFilePath;
    private final Tags tags;
    private Git clonedRepo;

    /**
     * Create a new GitConfigSources.
     *
     * @param repoURI the URI to the git repo
     * @param localRepoDirectory the local directory you want to save the git repo to.
     * @param configFilePath the path to the config file in the git repo
     * @param branch the branch you want to pull from git
     * @param credentials If authenticating with credentials, the CredentialsProvider such as UsernamePasswordCredentialsProvider
     * @param sshSessionFactory If using sshd the SshSessionFactory, this uses  apache mina-sshd.
     * The easiest way is to use the apache mina-sshd SshdSessionFactoryBuilder.
     * @throws GestaltException if there is a badly configured git repo
     */
    public GitConfigSource(String repoURI, Path localRepoDirectory, String configFilePath, String branch, CredentialsProvider credentials,
                           SshSessionFactory sshSessionFactory) throws GestaltException {
        this(repoURI, localRepoDirectory, configFilePath, branch, credentials, sshSessionFactory, Tags.of());
    }

    /**
     * Create a new GitConfigSources.
     *
     * @param repoURI the URI to the git repo
     * @param localRepoDirectory the local directory you want to save the git repo to.
     * @param configFilePath the path to the config file in the git repo
     * @param branch the branch you want to pull from git
     * @param credentials If authenticating with credentials, the CredentialsProvider such as UsernamePasswordCredentialsProvider
     * @param sshSessionFactory If using sshd the SshSessionFactory, this uses  apache mina-sshd.
     * The easiest way is to use the apache mina-sshd SshdSessionFactoryBuilder.
     * @param tags tags associated with the source
     * @throws GestaltException if there is a badly configured git repo
     */
    public GitConfigSource(String repoURI, Path localRepoDirectory, String configFilePath, String branch, CredentialsProvider credentials,
                           SshSessionFactory sshSessionFactory, Tags tags) throws GestaltException {
        if (repoURI == null) {
            throw new GestaltException("Must provide a git repo URI");
        }
        if (localRepoDirectory == null) {
            throw new GestaltException("Must provide a local directory to sync to");
        }
        if (configFilePath == null) {
            throw new GestaltException("Must provide a path to the configuration file");
        }

        this.localRepoDirectory = localRepoDirectory;
        this.configFilePath = configFilePath;
        this.tags = tags;

        initializeGitRepo(repoURI, localRepoDirectory, branch, credentials, sshSessionFactory);
    }

    private void initializeGitRepo(String repoURI, Path localRepoDirectory, String branch, CredentialsProvider credentials,
                                   SshSessionFactory sshSessionFactory) throws GestaltException {
        try {
            if (Files.exists(localRepoDirectory) && Files.exists(localRepoDirectory.resolve(".git"))) {
                clonedRepo = Git.open(localRepoDirectory.toFile());
            } else {
                deleteLocalDirectory(localRepoDirectory);

                Files.createDirectories(localRepoDirectory);
                CloneCommand builder = Git.cloneRepository()
                                          .setURI(repoURI)
                                          .setBranch(branch)
                                          .setDirectory(localRepoDirectory.toFile());

                if (credentials != null) {
                    builder.setCredentialsProvider(credentials);
                } else if (sshSessionFactory != null) {
                    builder.setTransportConfigCallback(transport -> {
                        if (transport instanceof SshTransport) {
                            ((SshTransport) transport).setSshSessionFactory(sshSessionFactory);
                        }
                    });
                }

                clonedRepo = builder.call();
            }

            pullLatest(credentials, sshSessionFactory);
        } catch (IOException | GitAPIException e) {
            throw new GestaltException("unable to clone git repo to local machine with error " + e.getMessage(), e);
        }
    }

    private void deleteLocalDirectory(Path dir) throws IOException {
        try (Stream<Path> pathStream = Files.walk(dir)) {
            pathStream.sorted(Comparator.reverseOrder())
                      .map(Path::toFile)
                      .forEach(File::delete);
        }
    }

    private void pullLatest(CredentialsProvider credentials, SshSessionFactory sshSessionFactory) throws GitAPIException {
        // This is needed to set the instance of the SshSessionFactory if there is no known hosts file because otherwise
        // the StrictHostKeyChecking property must be set to false in order to continue with the clone
        if (sshSessionFactory != null) {
            SshSessionFactory.setInstance(sshSessionFactory);
        }

        clonedRepo.pull().setCredentialsProvider(credentials).call();
    }

    @Override
    public boolean hasStream() {
        Path configFile = localRepoDirectory.resolve(configFilePath);
        return Files.exists(configFile);

    }

    @Override
    public InputStream loadStream() throws GestaltException {
        Path configFile = localRepoDirectory.resolve(configFilePath);
        if (!Files.exists(configFile)) {
            throw new GestaltException("unable to find config file at " + configFile.toAbsolutePath());
        }

        try {
            return Files.newInputStream(configFile);
        } catch (IOException e) {
            throw new GestaltException("Exception while trying to read file " + configFile.toAbsolutePath() +
                " with error " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {
        throw new GestaltException("Unsupported operation loadList on an GitConfigSource");
    }

    @Override
    public String format() {
        return format(configFilePath);
    }

    /**
     * Finds the extension of a file to get the file format.
     *
     * @param fileName the name of the file
     * @return the extension of the file
     */
    protected String format(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(index + 1);
        } else {
            return "";
        }
    }

    @Override
    public String name() {
        return "Git Config Source key: " + configFilePath;
    }

    @Override
    public UUID id() {  //NOPMD
        return id;
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GitConfigSource)) {
            return false;
        }
        GitConfigSource that = (GitConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
