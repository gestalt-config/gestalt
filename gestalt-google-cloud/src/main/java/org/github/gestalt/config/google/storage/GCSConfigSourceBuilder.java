package org.github.gestalt.config.google.storage;

import com.google.cloud.storage.Storage;
import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;

/**
 * ConfigSourceBuilder for the GCS Config Source.
 *
 * <p>Create a GCSConfigSource to load a config from a GCS Storage.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class GCSConfigSourceBuilder extends SourceBuilder<GCSConfigSourceBuilder, GCSConfigSource> {

    private Storage storage;
    private String objectName;
    private String bucketName;


    /**
     * private constructor, use the builder method.
     */
    private GCSConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static GCSConfigSourceBuilder builder() {
        return new GCSConfigSourceBuilder();
    }

    /**
     * Get the GCS Storage client.
     *
     * @return the GCS Storage client
     */
    public Storage getStorage() {
        return storage;
    }

    /**
     * Set the GCS Storage client.
     *
     * @param storage the GCS Storage client
     * @return the builder
     */
    public GCSConfigSourceBuilder setStorage(Storage storage) {
        this.storage = storage;
        return this;
    }

    /**
     * Get the GCS Object Name.
     *
     * @return the GCS Object Name
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Set the GCS Object name.
     *
     * @param objectName the GCS Object name
     * @return the builder
     */
    public GCSConfigSourceBuilder setObjectName(String objectName) {
        this.objectName = objectName;
        return this;
    }

    /**
     * Get the GCS Bucket Name.
     *
     * @return the GCS Bucket Name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Set the GCS Bucket name.
     *
     * @param bucketName the GCS Bucket name
     * @return the builder
     */
    public GCSConfigSourceBuilder setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    @Override
    public ConfigSourcePackage build() throws GestaltException {
        return buildPackage(new GCSConfigSource(storage, bucketName, objectName, tags));
    }
}
