package org.github.gestalt.config.google.builder;

import com.google.cloud.storage.Storage;
import org.github.gestalt.config.google.config.GoogleModuleConfig;

/**
 * Builder for creating Google specific configuration.
 * You can either specify the project ID or it will get it from the default.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class GoogleModuleConfigBuilder {
    private String projectId;
    private Storage storage;

    private GoogleModuleConfigBuilder() {

    }

    /**
     * Create a builder to create the Google config.
     *
     * @return a builder to create the Google config.
     */
    public static GoogleModuleConfigBuilder builder() {
        return new GoogleModuleConfigBuilder();
    }

    /**
     * project Id to use for Google.
     *
     * @return project Id to use for Google
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Set project Id to use for Google.
     *
     * @param projectId region to use for aws
     * @return the builder
     */
    public GoogleModuleConfigBuilder setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * Get the storage client.
     *
     * @return the storage client.
     */
    public Storage getStorage() {
        return storage;
    }

    /**
     * Set the storage client.
     *
     * @param storage the storage client
     * @return the builder
     */
    public GoogleModuleConfigBuilder setStorage(Storage storage) {
        this.storage = storage;
        return this;
    }

    public GoogleModuleConfig build() {
        return new GoogleModuleConfig(projectId, storage);
    }
}
