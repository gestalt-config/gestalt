package org.github.gestalt.config.azure.builder;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.security.keyvault.secrets.SecretClient;
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

        Assertions.assertNotNull(builder.build().getCredential());
        Assertions.assertNotNull(builder.build().getSecretsClient());
        Assertions.assertTrue(builder.build().hasSecretsClient());
        Assertions.assertNotNull(builder.build().getCredential());
        Assertions.assertEquals("azure", builder.build().name());
    }

    @Test
    public void createAWSConfigEmpty() {

        AzureModuleBuilder builder = AzureModuleBuilder.builder();

        GestaltConfigurationException e = Assertions.assertThrows(GestaltConfigurationException.class, builder::build);
        Assertions.assertEquals("AzureModuleConfig was built but one of the secret client or the vault endpoint must be provided",
            e.getMessage());
    }

}
