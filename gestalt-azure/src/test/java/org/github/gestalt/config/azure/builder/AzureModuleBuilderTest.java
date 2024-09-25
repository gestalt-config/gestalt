package org.github.gestalt.config.azure.builder;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.github.gestalt.config.azure.config.AzureModuleBuilder;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AzureModuleBuilderTest {

    @Test
    public void name() {
    }

    @Test
    public void createAzureConfig() throws GestaltConfigurationException {

        TokenCredential tokenCredential = Mockito.mock();

        AzureModuleBuilder builder = AzureModuleBuilder.builder()
            .setKeyVaultUri("test")
            .setCredential(tokenCredential);

        Assertions.assertEquals("test", builder.getKeyVaultUri());
        Assertions.assertNull(builder.getSecretsClient());
        Assertions.assertNotNull(builder.getCredential());
        Assertions.assertEquals("test", builder.build().getKeyVaultUri());
    }

    @Test
    public void createAzureConfigClient() throws GestaltConfigurationException {
        SecretClient client = Mockito.mock();

        AzureModuleBuilder builder = AzureModuleBuilder.builder()
            .setSecretClient(client);

        Assertions.assertNull(builder.getKeyVaultUri());
        Assertions.assertNotNull(builder.getSecretsClient());
        Assertions.assertNull(builder.build().getKeyVaultUri());
        Assertions.assertNotNull(builder.build().getSecretsClient());
    }


    @Test
    public void createAzureCredential() throws GestaltConfigurationException {
        SecretClient client = Mockito.mock();
        final DefaultAzureCredential tokenCredential = Mockito.mock();

        AzureModuleBuilder builder = AzureModuleBuilder.builder()
            .setSecretClient(client)
            .setCredential(tokenCredential);

        Assertions.assertNull(builder.getKeyVaultUri());
        Assertions.assertNotNull(builder.getSecretsClient());
        Assertions.assertNotNull(builder.getCredential());
        Assertions.assertNull(builder.build().getKeyVaultUri());
        Assertions.assertNotNull(builder.build().getSecretsClient());
    }

    @Test
    public void createAzureBuild() throws GestaltConfigurationException {
        SecretClient client = Mockito.mock();
        final DefaultAzureCredential tokenCredential = Mockito.mock();

        AzureModuleBuilder builder = AzureModuleBuilder.builder()
            .setSecretClient(client)
            .setKeyVaultUri("test")
            .setCredential(tokenCredential);

        var built = builder.build();
        Assertions.assertNotNull(built.getCredential());
        Assertions.assertNotNull(built.getSecretsClient());
        Assertions.assertTrue(built.hasSecretsClient());
        Assertions.assertTrue(built.hasCredential());
        Assertions.assertNotNull(built.getCredential());
        Assertions.assertEquals("azure", built.name());
    }

    @Test
    public void createAzureBuildBlobClient() throws GestaltConfigurationException {
        BlobClient blobClient = Mockito.mock();

        AzureModuleBuilder builder = AzureModuleBuilder.builder()
            .setBlobClient(blobClient);

        var built = builder.build();
        Assertions.assertNull(built.getCredential());
        Assertions.assertNotNull(built.getBlobClient());
        Assertions.assertNull(built.getSecretsClient());
        Assertions.assertFalse(built.hasSecretsClient());
        Assertions.assertFalse(built.hasCredential());
        Assertions.assertEquals("azure", built.name());
        Assertions.assertEquals(blobClient, built.getBlobClient());
        Assertions.assertNull(builder.getStorageSharedKeyCredential());
        Assertions.assertNotNull(builder.getBlobClient());
    }

    @Test
    public void createAzureBuildStorageSharedKeyCredential() throws GestaltConfigurationException {
        StorageSharedKeyCredential storageSharedKeyCredential  = Mockito.mock();

        AzureModuleBuilder builder = AzureModuleBuilder.builder()
                .setStorageSharedKeyCredential(storageSharedKeyCredential);

        var built = builder.build();
        Assertions.assertNull(built.getCredential());
        Assertions.assertNull(built.getBlobClient());
        Assertions.assertNull(built.getSecretsClient());
        Assertions.assertNotNull(built.getStorageSharedKeyCredential());
        Assertions.assertFalse(built.hasSecretsClient());
        Assertions.assertEquals("azure", built.name());
        Assertions.assertEquals(storageSharedKeyCredential, built.getStorageSharedKeyCredential());
        Assertions.assertNotNull(builder.getStorageSharedKeyCredential());
        Assertions.assertNull(builder.getBlobClient());
    }
}
