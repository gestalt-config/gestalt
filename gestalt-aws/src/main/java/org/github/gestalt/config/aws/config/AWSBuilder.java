package org.github.gestalt.config.aws.config;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Builder for creating AWS specific configuration.
 * You can either specify the region and Gestalt will use defaults to build the SecretsManagerClient
 * or you can provide a SecretsManagerClient yourself.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class AWSBuilder {
    private String region;
    private SecretsManagerClient secretsClient;
    private S3Client s3Client;

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
        AWSModuleConfig awsModuleConfig = new AWSModuleConfig();
        awsModuleConfig.setRegion(region);
        awsModuleConfig.setSecretsClient(secretsClient);
        awsModuleConfig.setS3Client(s3Client);

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
     * If this is not set, then you must provide the region, and
     * it will be constructed using defaults.
     *
     * @param secretsClient Set the SecretsManagerClient to use to communicate with AWS.
     * @return the builder
     */
    public AWSBuilder setSecretsClient(SecretsManagerClient secretsClient) {
        this.secretsClient = secretsClient;
        return this;
    }

    /**
     * Get the S3 Client.
     *
     * @return the S3 Client.
     */
    public S3Client getS3Client() {
        return s3Client;
    }

    /**
     * Set the S3 Client.
     *
     * @param s3Client the S3 Client
     * @return the builder
     */
    public AWSBuilder setS3Client(S3Client s3Client) {
        this.s3Client = s3Client;
        return this;
    }
}
