package org.github.gestalt.config.aws.s3;


import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * ConfigSourceBuilder for the S3 Config Source.
 *
 * <p>Create a S3ConfigSource to load a config from S3.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class S3ConfigSourceBuilder extends SourceBuilder<S3ConfigSourceBuilder, S3ConfigSource> {

    private S3Client s3;
    private String keyName;
    private String bucketName;

    /**
     * private constructor, use the builder method.
     */
    private S3ConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static S3ConfigSourceBuilder builder() {
        return new S3ConfigSourceBuilder();
    }

    /**
     * Get the S3 client.
     *
     * @return the S3 client
     */
    public S3Client getS3() {
        return s3;
    }

    /**
     * Set the S3 client.
     *
     * @param s3 the S3 client
     * @return builder
     */
    public S3ConfigSourceBuilder setS3(S3Client s3) {
        this.s3 = s3;
        return this;
    }

    /**
     * Get the s3 key name.
     *
     * @return the s3 key name
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Set the s3 key name.
     *
     * @param keyName the s3 key name
     * @return builder
     */
    public S3ConfigSourceBuilder setKeyName(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * Get the s3 bucket name.
     *
     * @return the s3 bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Set the s3 bucket name.
     *
     * @param bucketName the s3 bucket name
     * @return builder
     */
    public S3ConfigSourceBuilder setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    @Override
    public ConfigSourcePackage build() throws GestaltException {
        return buildPackage(new S3ConfigSource(s3, bucketName, keyName));
    }
}
