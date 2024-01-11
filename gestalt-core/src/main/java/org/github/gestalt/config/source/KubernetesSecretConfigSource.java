package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Load a config source as kubernetes files secrets.
 * https://kubernetes.io/docs/concepts/configuration/secret/#projection-of-secret-keys-to-specific-paths
 * Where the name of the file is the path for the configuration and
 * the value in the file is the value of the configuration.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class KubernetesSecretConfigSource implements ConfigSource {
    public static final String K8_SECRET_CONFIG = "k8Secret";

    private final Path path;
    private final UUID id = UUID.randomUUID();
    private final Tags tags;

    /**
     * Constructor for a Kubernetes Files Config Source.
     *
     * @param file where to load the directory with the configuration
     * @throws GestaltException any exceptions.
     */
    public KubernetesSecretConfigSource(File file) throws GestaltException {
        this(Objects.requireNonNull(file, "Kubernetes Secret file can not be null").toPath(), Tags.of());
    }

    /**
     * Constructor for a Kubernetes Files Config Source.
     *
     * @param file where to load the directory with the configuration
     * @param tags tags associated with the source
     * @throws GestaltException any exceptions.
     */
    public KubernetesSecretConfigSource(File file, Tags tags) throws GestaltException {
        this(Objects.requireNonNull(file, "Kubernetes Secret file can not be null").toPath(), tags);
    }

    /**
     * Constructor for a Kubernetes Files Config Source.
     *
     * @param path where to load the directory with the configuration
     * @throws GestaltException any exceptions.
     */
    public KubernetesSecretConfigSource(Path path) throws GestaltException {
        this(path, Tags.of());
    }

    /**
     * Constructor for a Kubernetes Files Config Source.
     *
     * @param path where to load the directory with the configuration
     * @param tags tags associated with the source
     * @throws GestaltException any exceptions.
     */
    public KubernetesSecretConfigSource(Path path, Tags tags) throws GestaltException {
        this.path = validatePath(path);
        this.tags = tags;
    }

    private Path validatePath(Path path) throws GestaltException {
        Objects.requireNonNull(path, "Kubernetes Secret path can not be null");
        if (!Files.isDirectory(path)) {
            throw new GestaltException("Kubernetes Secret path does not exist at: " + path);
        } else if (isEmpty(path)) {
            throw new GestaltException("Kubernetes Secret path is empty: " + path);
        }
        return path;
    }

    private boolean isEmpty(Path path) throws GestaltException {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
            return !directory.iterator().hasNext();
        } catch (IOException e) {
            throw new GestaltException("IOException while testing if path is empty.", e);
        }
    }

    /**
     * The path to the file.
     *
     * @return path to the file
     */
    public Path getPath() {
        return path;
    }

    @Override
    public boolean hasStream() {
        return false;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        throw new GestaltException("Unsupported operation loadStream on an KubernetesFilesConfigSource");
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {
        List<Pair<String, String>> results = new ArrayList<>();
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                for (Path secretFile : directory) {
                    if (Files.isRegularFile(secretFile)) {
                        String key = secretFile.getFileName().toString();
                        String value = Files.readString(secretFile);
                        Pair<String, String> secret = new Pair<>(key, value);
                        results.add(secret);
                    }
                }
            } catch (IOException e) {
                throw new GestaltException("IOException while loading kubernetes secrets from path: " + path);
            }
        }
        return results;
    }

    @Override
    public String format() {
        return K8_SECRET_CONFIG;
    }

    @Override
    public String name() {
        return "Kubernetes Secret source: " + path.toString();
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
        if (!(o instanceof KubernetesSecretConfigSource)) {
            return false;
        }
        KubernetesSecretConfigSource that = (KubernetesSecretConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
