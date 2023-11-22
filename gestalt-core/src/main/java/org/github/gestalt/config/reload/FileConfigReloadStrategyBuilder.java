package org.github.gestalt.config.reload;

import org.github.gestalt.config.builder.ConfigReloadStrategyBuilder;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.source.FileConfigSource;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Builder for the FileChangeReloadStrategy. Requires a source.
 * Can also provide an optional ExecutorService. If one is not provided then it will use a SingleThreadExecutor.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class FileConfigReloadStrategyBuilder
    extends ConfigReloadStrategyBuilder<FileConfigReloadStrategyBuilder, FileChangeReloadStrategy> {

    ExecutorService executor;

    /**
     * Create a FileConfigReloadStrategyBuilder.
     *
     * @return FileConfigReloadStrategyBuilder
     */
    public static FileConfigReloadStrategyBuilder builder() {
        return new FileConfigReloadStrategyBuilder();
    }

    /**
     * Get the executor for the builder.
     *
     * @return the executor for the builder
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Optional ExecutorService, if not set will use the SingleThreadExecutor.
     *
     * @param executor executor to use.
     * @return builder
     */
    public FileConfigReloadStrategyBuilder setExecutor(ExecutorService executor) {
        Objects.requireNonNull(executor, "executor must not be null");
        this.executor = executor;
        return self();
    }

    /**
     * Build the FileChangeReloadStrategy, will validate all the correct properties have been set.
     *
     * @return the FileChangeReloadStrategy.
     * @throws GestaltConfigurationException if the builder is not configured correctly.
     */
    @Override
    public FileChangeReloadStrategy build() throws GestaltConfigurationException {
        if (this.source == null) {
            throw new GestaltConfigurationException(
                "When building a File Change Reload Strategy with the builder you must set a source");
        }

        if (!(this.source instanceof FileConfigSource)) {
            throw new GestaltConfigurationException(
                "When building a File Change Reload Strategy with the builder the source must be a file");
        }

        if (this.executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }

        return new FileChangeReloadStrategy(this.source, executor);
    }
}
