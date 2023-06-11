package org.github.gestalt.config.aws.builder;

import org.github.gestalt.config.aws.config.AWSModuleConfig;

/**
 * Builder for creating the AWS configuration.
 *
 * @author Colin Redmond (c) 2023.
 */
final public class AWSBuilder {
    private String region;

    private AWSBuilder() {

    }

    public static AWSBuilder builder() {
        return new AWSBuilder();
    }

    public AWSBuilder setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public AWSModuleConfig build() {
        return new AWSModuleConfig(region);
    }
}
