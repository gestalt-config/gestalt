package org.github.gestalt.config.azure.config;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import org.github.gestalt.config.entity.GestaltModuleConfig;

/**
 * Azure specific configuration.
 * You can either specify the keyVaultUri and credential then Gestalt will use defaults to build the SecretClient
 * or you can provide a SecretClient yourself.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class AzureModuleConfig implements GestaltModuleConfig {

    private String keyVaultUri;

    private TokenCredential credential;

    private SecretClient secretClient;

    AzureModuleConfig() {
    }

    @Override
    public String name() {
        return "azure";
    }

    /**
     * Get Vault URI to use for Azure.
     *
     * @return Vault URI to use for Azure.
     */
    public String getKeyVaultUri() {
        return keyVaultUri;
    }

    /**
     * Set Vault URI to use for Azure.
     *
     * @param keyVaultUri Vault URI to use for Azure.
     */
    public void setKeyVaultUri(String keyVaultUri) {
        this.keyVaultUri = keyVaultUri;
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
     * @param credential TokenCredential for Azure Secrets
     */
    public void setCredential(TokenCredential credential) {
        this.credential = credential;
    }

    /**
     * Return if the secretClient has been set.
     *
     * @return if the secretClient has been set
     */
    public boolean hasSecretsClient() {
        return secretClient != null;
    }

    /**
     * SecretClient to use to communicate with Azure.
     *
     * @return SecretClient to use to communicate with Azure.
     */
    public SecretClient getSecretsClient() {
        return secretClient;
    }

    /**
     * Set the SecretClient to use to communicate with Azure.
     * If this is not set, then you must provide the keyVaultUri and credential
     * it will be constructed using defaults.
     *
     * @param secretsClient Set the SecretsManagerClient to use to communicate with Azure.
     */
    public void setSecretsClient(SecretClient secretsClient) {
        this.secretClient = secretsClient;
    }
}
