package org.github.gestalt.config.aws.config;

import org.github.gestalt.config.entity.GestaltModuleConfig;

/**
 * AWS specific configuration.
 *
 * @author Colin Redmond (c) 2023.
 */
public class AWSModuleConfig implements GestaltModuleConfig {

    private String region;

    public AWSModuleConfig() {
    }

    public AWSModuleConfig(String region) {
        this.region = region;
    }

    @Override
    public String name() {
        return "aws";
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
