package org.github.gestalt.config.aws.builder;

import org.github.gestalt.config.aws.config.AWSModuleConfig;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Builder for creating AWS specific configuration.
 * You can either specify the region and Gestalt will use defaults to build the SecretsManagerClient
 * or you can provide a SecretsManagerClient yourself.
 *
 * @author Colin Redmond (c) 2023.
 */
final public class AWSBuilder {
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
     * Set region to use for aws
     * @param region region to use for aws
     */
    public AWSBuilder setRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * Region to use for aws
     *
     * @return Region to use for aws
     */
    public String getRegion() {
        return region;
    }

    public AWSModuleConfig build() {
        return new AWSModuleConfig(region);
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
