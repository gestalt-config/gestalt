package org.github.gestalt.config.google.config;

import com.google.cloud.storage.Storage;
import org.github.gestalt.config.entity.GestaltModuleConfig;

/**
 * Google specific configuration.
 * You can either specify the project ID or it will get it from the default.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class GoogleModuleConfig implements GestaltModuleConfig {

    private String projectId;
    private Storage storage;

    public GoogleModuleConfig() {
    }

    public GoogleModuleConfig(String projectId) {
        this(projectId, null);
    }

    public GoogleModuleConfig(String projectId, Storage storage) {
        this.projectId = projectId;
        this.storage = storage;
    }

    @Override
    public String name() {
        return "google";
    }

    /**
     * ProjectId to use for Google.
     *
     * @return ProjectId to use for Google
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Set projectId to use for Google.
     *
     * @param projectId projectId to use for Google
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * If the storage client has been set.
     *
     * @return If the storage client has been set.
     */
    public boolean hasStorage() {
        return storage != null;
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
     */
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

}
