package org.github.gestalt.config.google.config;

import org.github.gestalt.config.entity.GestaltModuleConfig;

/**
 * Google specific configuration.
 * You can either specify the project ID or it will get it from the default.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class GoogleModuleConfig implements GestaltModuleConfig {

    private String projectId;

    public GoogleModuleConfig() {
    }

    public GoogleModuleConfig(String projectId) {
        this.projectId = projectId;
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

}
