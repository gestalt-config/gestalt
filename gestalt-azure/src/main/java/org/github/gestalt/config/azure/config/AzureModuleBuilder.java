package org.github.gestalt.config.azure.config;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;

/**
 * Builder for creating Azure specific configuration.
 * You can either specify the keyVaultUri and Gestalt will use defaults to build the SecretsManagerClient
 * or you can provide a SecretsManagerClient yourself.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class AzureModuleBuilder {
    private String keyVaultUri;
    private SecretClient secretClient;
    private TokenCredential credential;
    private BlobClient blobClient;
    private StorageSharedKeyCredential storageSharedKeyCredential;

    private AzureModuleBuilder() {

    }


    /**
     * Create a builder to create the Azure config.
     *
     * @return a builder to create the Azure config.
     */
    public static AzureModuleBuilder builder() {
        return new AzureModuleBuilder();
    }

    /**
     * keyVaultUri to use for Azure secrets.
     *
     * @return keyVaultUri to use for Azure secrets
     */
    public String getKeyVaultUri() {
        return keyVaultUri;
    }

    /**
     * Set keyVaultUri to use for Azure secrets.
     *
     * @param keyVaultUri keyVaultUri to use for Azure secrets.
     * @return the builder
     */
    public AzureModuleBuilder setKeyVaultUri(String keyVaultUri) {
        this.keyVaultUri = keyVaultUri;
        return this;
    }

    /**
     * Get TokenCredential for Azure Secrets.
     *
     * @return TokenCredential for Azure Secrets
     */
    public TokenCredential getCredential() {
        return credential;
    }

    /**
     * Set TokenCredential for Azure Secrets.
     *
     * @param credential TokenCredential for Azure Secrets.
     * @return TokenCredential for Azure Secrets
     */
    public AzureModuleBuilder setCredential(TokenCredential credential) {
        this.credential = credential;
        return this;
    }


    /**
     * SecretsManagerClient to use to communicate with Azure.
     *
     * @return SecretsManagerClient to use to communicate with Azure.
     */
    public SecretClient getSecretsClient() {
        return secretClient;
    }

    /**
     * Set the SecretClient to use to communicate with Azure.
     * If this is not set, then you must provide the keyVaultUri, and
     * it will be constructed using defaults.
     *
     * @param secretsClient Set the SecretsManagerClient to use to communicate with Azure.
     * @return the builder
     */
    public AzureModuleBuilder setSecretClient(SecretClient secretsClient) {
        this.secretClient = secretsClient;
        return this;
    }

    /**
     * Get the Blob Client.
     *
     * @return the Blob Client.
     */
    public BlobClient getBlobClient() {
        return blobClient;
    }

    /**
     * Set the Blob Client.
     *
     * @param blobClient the Blob Client
     * @return the builder
     */
    public AzureModuleBuilder setBlobClient(BlobClient blobClient) {
        this.blobClient = blobClient;
        return this;
    }

    /**
     * Get the StorageSharedKeyCredential for blob storage.
     *
     * @return the StorageSharedKeyCredential for blob storage
     */
    public StorageSharedKeyCredential getStorageSharedKeyCredential() {
        return storageSharedKeyCredential;
    }

    /**
     * Set the StorageSharedKeyCredential for blob storage.
     *
     * @param storageSharedKeyCredential the StorageSharedKeyCredential for blob storage.
     * @return the builder
     */
    public AzureModuleBuilder setStorageSharedKeyCredential(StorageSharedKeyCredential storageSharedKeyCredential) {
        this.storageSharedKeyCredential = storageSharedKeyCredential;
        return this;
    }

    public AzureModuleConfig build() throws GestaltConfigurationException {
        AzureModuleConfig azureModuleConfig = new AzureModuleConfig();
        azureModuleConfig.setKeyVaultUri(keyVaultUri);
        azureModuleConfig.setCredential(credential);
        azureModuleConfig.setSecretsClient(secretClient);
        azureModuleConfig.setBlobClient(blobClient);
        azureModuleConfig.setStorageSharedKeyCredential(storageSharedKeyCredential);

        return azureModuleConfig;
    }
}
