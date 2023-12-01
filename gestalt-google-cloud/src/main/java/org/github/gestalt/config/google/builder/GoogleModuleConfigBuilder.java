package org.github.gestalt.config.google.builder;

import org.github.gestalt.config.google.config.GoogleModuleConfig;

/**
 * Builder for creating Google specific configuration.
 * You can either specify the project ID or it will get it from the default.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class GoogleModuleConfigBuilder {
    private String projectId;

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

    public GoogleModuleConfig build() {
        return new GoogleModuleConfig(projectId);
    }
}
