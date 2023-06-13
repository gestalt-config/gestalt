package org.github.gestalt.config.google.builder;

import org.github.gestalt.config.google.config.GoogleModuleConfig;

/**
 * Builder for creating Google specific configuration.
 * You can either specify the project ID or it will get it from the default.
 *
 * @author Colin Redmond (c) 2023.
 */
public final class GoogleBuilder {
    private String projectId;

    private GoogleBuilder() {

    }


    /**
     * Create a builder to create the Google config.
     *
     * @return a builder to create the Google config.
     */
    public static GoogleBuilder builder() {
        return new GoogleBuilder();
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
    public GoogleBuilder setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public GoogleModuleConfig build() {
        return new GoogleModuleConfig(projectId);
    }
}
