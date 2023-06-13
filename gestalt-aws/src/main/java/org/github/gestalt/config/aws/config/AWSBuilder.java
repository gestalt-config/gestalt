package org.github.gestalt.config.aws.config;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Builder for creating AWS specific configuration.
 * You can either specify the region and Gestalt will use defaults to build the SecretsManagerClient
 * or you can provide a SecretsManagerClient yourself.
 *
 * @author Colin Redmond (c) 2023.
 */
public final class AWSBuilder {
    private String region;
    private SecretsManagerClient secretsClient;

    private AWSBuilder() {

    }


    /**
     * Create a builder to create the AWS config.
     *
     * @return a builder to create the AWS config.
     */
    public static AWSBuilder builder() {
        return new AWSBuilder();
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
     * @return the builder
     */
    public AWSBuilder setRegion(String region) {
        this.region = region;
        return this;
    }

    public AWSModuleConfig build() throws GestaltConfigurationException {
        if (region == null && secretsClient == null) {
            throw new GestaltConfigurationException("AWSModuleConfig was built but one of the secret client " +
                "or the region must be provided");
        }

        AWSModuleConfig awsModuleConfig = new AWSModuleConfig();
        awsModuleConfig.setRegion(region);
        awsModuleConfig.setSecretsClient(secretsClient);

        return awsModuleConfig;
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
     * @return the builder
     */
    public AWSBuilder setSecretsClient(SecretsManagerClient secretsClient) {
        this.secretsClient = secretsClient;
        return this;
    }
}
