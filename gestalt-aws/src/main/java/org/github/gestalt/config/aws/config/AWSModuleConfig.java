package org.github.gestalt.config.aws.config;

import org.github.gestalt.config.entity.GestaltModuleConfig;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * AWS specific configuration.
 * You can either specify the region and Gestalt will use defaults to build the SecretsManagerClient
 * or you can provide a SecretsManagerClient yourself.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class AWSModuleConfig implements GestaltModuleConfig {

    private String region;

    private SecretsManagerClient secretsClient;

    AWSModuleConfig() {
    }

    public AWSModuleConfig(String region) {
        this.region = region;
    }

    @Override
    public String name() {
        return "aws";
    }

    /**
     * Region to use for aws.
     *
     * @return Region to use for aws
     */
    public String getRegion() {
        return region;
    }

    /**
     * Set region to use for aws.
     *
     * @param region region to use for aws
     */
    public void setRegion(String region) {
        this.region = region;
    }

    public boolean hasSecretsClient() {
        return secretsClient != null;
    }

    /**
     * SecretsManagerClient to use to communicate with AWS.
     *
     * @return SecretsManagerClient to use to communicate with AWS.
     */
    public SecretsManagerClient getSecretsClient() {
        return secretsClient;
    }

    /**
     * Set the SecretsManagerClient to use to communicate with AWS.
     * If this is not set, then you must provide the region and
     * it will be constructed using defaults.
     *
     * @param secretsClient Set the SecretsManagerClient to use to communicate with AWS.
     */
    public void setSecretsClient(SecretsManagerClient secretsClient) {
        this.secretsClient = secretsClient;
    }
}
