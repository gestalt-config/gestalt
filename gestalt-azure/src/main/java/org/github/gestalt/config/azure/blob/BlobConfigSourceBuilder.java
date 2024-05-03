package org.github.gestalt.config.azure.blob;


import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;

import java.util.Objects;

/**
 * ConfigSourceBuilder for the Azure Blob storage Config Source.
 *
 * <p>Create a BlobConfigSource to load a config from S3.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class BlobConfigSourceBuilder extends SourceBuilder<BlobConfigSourceBuilder, BlobConfigSource> {

    private BlobClient blobClient;
    private StorageSharedKeyCredential credential;
    private String endpoint;
    private String containerName;
    private String blobName;

    /**
     * private constructor, use the builder method.
     */
    private BlobConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static BlobConfigSourceBuilder builder() {
        return new BlobConfigSourceBuilder();
    }

    /**
     * Set the BlobClient you wish to use, this will override all other fields. We dont need to build a BlobClient if it is provided.
     *
     * @param blobClient BlobClient used for BlobConfigSource
     * @return the builder
     */
    public BlobConfigSourceBuilder setBlobClient(BlobClient blobClient) {
        this.blobClient = blobClient;
        return this;
    }

    /**
     * Set the credentials used to communicate with the Azure Blob Storage.
     *
     * @param credential the credentials used to communicate with the Azure Blob Storage
     * @return the builder
     */
    public BlobConfigSourceBuilder setCredential(StorageSharedKeyCredential credential) {
        this.credential = credential;
        return this;
    }

    /**
     *  Set the endpoint to communicate with the Azure Blob Storage.
     *
     * @param endpoint endpoint to communicate with the Azure Blob Storage
     * @return the builder
     */
    public BlobConfigSourceBuilder setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set the container name for the Azure Blob Storage.
     *
     * @param containerName the container name for the Azure Blob Storage.
     * @return the builder
     */
    public BlobConfigSourceBuilder setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Set the specific blob name to load into the BlobConfigSource.
     *
     * @param blobName specific blob name to load into the BlobConfigSource.
     * @return the builder
     */
    public BlobConfigSourceBuilder setBlobName(String blobName) {
        this.blobName = blobName;
        return this;
    }

    /**
     * Build the BlobConfigSource.
     *
     * @return the BlobConfigSource
     * @throws GestaltException any exceptions while building the BlobConfigSource
     */
    @Override
    public ConfigSourcePackage build() throws GestaltException {
        if (blobClient == null) {
            Objects.requireNonNull(endpoint, "Must provided either a BlobClient or a valid endpoint");
            Objects.requireNonNull(blobName, "Must provided either a BlobClient or a valid blobName");
            Objects.requireNonNull(containerName, "Must provided either a BlobClient or a valid containerName");

            BlobClientBuilder blobClientBuilder = new BlobClientBuilder()
                .endpoint(endpoint)
                .blobName(blobName)
                .containerName(containerName);

            if (credential != null) {
                blobClientBuilder.credential(credential);
            }

            return buildPackage(new BlobConfigSource(blobClientBuilder.buildClient()));
        } else {
            return buildPackage(new BlobConfigSource(blobClient));
        }
    }
}
