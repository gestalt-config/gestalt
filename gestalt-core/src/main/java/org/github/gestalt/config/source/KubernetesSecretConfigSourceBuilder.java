package org.github.gestalt.config.source;

import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * ConfigSourceBuilder for the Kubernetes Secret Config Source.
 *
 * <p>Load a config source as kubernetes files secrets.
 * https://kubernetes.io/docs/concepts/configuration/secret/#projection-of-secret-keys-to-specific-paths
 * Where the name of the file is the path for the configuration and
 * the value in the file is the value of the configuration.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class KubernetesSecretConfigSourceBuilder
    extends SourceBuilder<KubernetesSecretConfigSourceBuilder, KubernetesSecretConfigSource> {

    private Path path;

    /**
     * private constructor, use the builder method.
     */
    private KubernetesSecretConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static KubernetesSecretConfigSourceBuilder builder() {
        return new KubernetesSecretConfigSourceBuilder();
    }

    /**
     * Get the folder to watch for the Kubernetes secrets.
     *
     * @return the folder to watch for the Kubernetes secrets.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Set the folder to watch for the Kubernetes secrets.
     *
     * @param path the folder to watch for the Kubernetes secrets.
     * @return the builder
     */
    public KubernetesSecretConfigSourceBuilder setPath(Path path) {
        Objects.requireNonNull(path, "path must not be null when building a Kubernetes Secret config source");
        this.path = path;
        return this;
    }

    /**
     * Get the folder to watch for the Kubernetes secrets.
     *
     * @return the folder to watch for the Kubernetes secrets.
     */
    public File getFile() {
        return path.toFile();
    }

    /**
     * Set the folder to watch for the Kubernetes secrets.
     *
     * @param file the folder to watch for the Kubernetes secrets.
     * @return the builder
     */
    public KubernetesSecretConfigSourceBuilder setFile(File file) {
        Objects.requireNonNull(file, "file must not be null when building a Kubernetes Secret config source");
        this.path = file.toPath();
        return this;
    }

    @Override
    public ConfigSourcePackage<KubernetesSecretConfigSource> build() throws GestaltException {
        return buildPackage(new KubernetesSecretConfigSource(path, tags));
    }
}
