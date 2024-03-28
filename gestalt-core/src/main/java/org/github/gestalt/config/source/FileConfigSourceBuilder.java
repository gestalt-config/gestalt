package org.github.gestalt.config.source;

import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * ConfigSourceBuilder for the File Config Source.
 *
 * <p>Load a config source from a local file.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class FileConfigSourceBuilder extends SourceBuilder<FileConfigSourceBuilder, FileConfigSource> {

    private Path path;

    /**
     * private constructor, use the builder method.
     */
    private FileConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static FileConfigSourceBuilder builder() {
        return new FileConfigSourceBuilder();
    }

    /**
     * Get the path for the config source.
     *
     * @return the path for the config source
     */
    public Path getPath() {
        return path;
    }

    /**
     * Set the path for the config source.
     *
     * @param path the path for the config source
     * @return the builder
     */
    public FileConfigSourceBuilder setPath(Path path) {
        Objects.requireNonNull(path, "path must not be null when building a file config source");
        this.path = path;
        return this;
    }

    /**
     * Get the file for the config source.
     *
     * @return the file for the config source.
     */
    public File getFile() {
        return path.toFile();
    }

    /**
     * Set the file for the config source.
     *
     * @param file Set the file for the config source.
     * @return the builder
     */
    public FileConfigSourceBuilder setFile(File file) {
        Objects.requireNonNull(file, "file must not be null when building a file config source");
        this.path = file.toPath();
        return this;
    }

    @Override
    public ConfigSourcePackage build() throws GestaltException {
        return buildPackage(new FileConfigSource(path));
    }
}
