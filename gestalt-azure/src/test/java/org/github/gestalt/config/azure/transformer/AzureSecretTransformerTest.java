package org.github.gestalt.config.azure.transformer;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.github.gestalt.config.azure.config.AzureModuleBuilder;
import org.github.gestalt.config.azure.config.AzureModuleConfig;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AzureSecretTransformerTest {

    final DefaultAzureCredential tokenCredential = Mockito.mock();
    final SecretClient secretClient = Mockito.mock();

    @BeforeEach
    public void setup() {
        Mockito.reset(tokenCredential, secretClient);
    }

    @Test
    void name() {
        AzureSecretTransformer transform = new AzureSecretTransformer();
        Assertions.assertEquals("azureSecret", transform.name());
    }

    @Test
    void process() throws GestaltConfigurationException {
        try (MockedConstruction<SecretClientBuilder> secretClientBuilder = Mockito.mockConstruction(SecretClientBuilder.class,
            (mock, context) -> {
                when(mock.buildClient()).thenReturn(secretClient);
                when(mock.vaultUrl(any())).thenReturn(mock);
                when(mock.credential(any())).thenReturn(mock);
            })) {

            try (
                MockedConstruction<DefaultAzureCredentialBuilder> defaultAzureCredentialBuilder =
                    Mockito.mockConstruction(DefaultAzureCredentialBuilder.class,
                        (mock, context) -> when(mock.build()).thenReturn(tokenCredential))) {

                AzureSecretTransformer transform = new AzureSecretTransformer();
                GestaltConfig gestaltConfig = new GestaltConfig();
                AzureModuleConfig azureModule = AzureModuleBuilder.builder()
                    .setKeyVaultUri("myurl")
                    .setCredential(tokenCredential)
                    .build();

                gestaltConfig.registerModuleConfig(azureModule);
                ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
                transform.applyConfig(config);

                when(secretClient.getSecret("secret")).thenReturn(new KeyVaultSecret("secret", "hello world"));

                var results = transform.process("test", "secret", "azureSecret:secret");

                Assertions.assertTrue(results.hasResults());
                Assertions.assertFalse(results.hasErrors());

                Assertions.assertEquals("hello world", results.results());
            }
        }
    }

    @Test
    void processNoModule() {

        AzureSecretTransformer transform = new AzureSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        var results = transform.process("test", "secret", "azureSecret:secret");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("AzureModuleConfig has not been registered. Register by creating a AzureModuleBuilder then " +
            "registering the AzureModuleBuilder.build() results with the Gestalt Builder.addModuleConfig(). If you wish to use the " +
            "Azure module with string substitution ${azureSecret:secret} on the path: test", results.getErrors().get(0).description());
    }

    @Test
    void processWithCredentials() throws GestaltConfigurationException {
        try (MockedConstruction<SecretClientBuilder> secretClientBuilder = Mockito.mockConstruction(SecretClientBuilder.class,
            (mock, context) -> {
                when(mock.buildClient()).thenReturn(secretClient);
                when(mock.vaultUrl(any())).thenReturn(mock);
                when(mock.credential(any())).thenReturn(mock);
            })) {

            AzureSecretTransformer transform = new AzureSecretTransformer();
            GestaltConfig gestaltConfig = new GestaltConfig();
            AzureModuleConfig azureModule = AzureModuleBuilder
                .builder()
                .setKeyVaultUri("myurl")
                .setCredential(tokenCredential)
                .build();

            gestaltConfig.registerModuleConfig(azureModule);
            ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
            transform.applyConfig(config);

            when(secretClient.getSecret("secret")).thenReturn(new KeyVaultSecret("secret", "hello world"));

            var results = transform.process("test", "secret", "azureSecret:secret");

            Assertions.assertTrue(results.hasResults());
            Assertions.assertFalse(results.hasErrors());

            Assertions.assertEquals("hello world", results.results());

        }
    }

    @Test
    void processWithOutCredentials() throws GestaltConfigurationException {
        try (MockedConstruction<SecretClientBuilder> secretClientBuilder = Mockito.mockConstruction(SecretClientBuilder.class,
            (mock, context) -> {
                when(mock.buildClient()).thenReturn(secretClient);
                when(mock.vaultUrl(any())).thenReturn(mock);
                when(mock.credential(any())).thenReturn(mock);
            })) {

            AzureSecretTransformer transform = new AzureSecretTransformer();
            GestaltConfig gestaltConfig = new GestaltConfig();
            AzureModuleConfig azureModule = AzureModuleBuilder
                .builder()
                .setKeyVaultUri("myurl")
                .build();

            gestaltConfig.registerModuleConfig(azureModule);
            ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
            transform.applyConfig(config);

            when(secretClient.getSecret("secret")).thenReturn(new KeyVaultSecret("secret", "hello world"));

            var results = transform.process("test", "secret", "azureSecret:secret");

            Assertions.assertTrue(results.hasResults());
            Assertions.assertFalse(results.hasErrors());

            Assertions.assertEquals("hello world", results.results());

        }
    }

    @Test
    void processSecretsClient() throws GestaltConfigurationException {
        AzureSecretTransformer transform = new AzureSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        AzureModuleConfig azureModule = AzureModuleBuilder.builder().setSecretClient(secretClient).build();

        gestaltConfig.registerModuleConfig(azureModule);
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        when(secretClient.getSecret("secret")).thenReturn(new KeyVaultSecret("secret", "hello world"));

        var results = transform.process("test", "secret", "azureSecret:secret");

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("hello world", results.results());
    }

    @Test
    void processNull() throws GestaltConfigurationException {
        AzureSecretTransformer transform = new AzureSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        AzureModuleConfig azureModule = AzureModuleBuilder.builder().setSecretClient(secretClient).build();

        gestaltConfig.registerModuleConfig(azureModule);
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        var results = transform.process("test", null, "azureSecret:secret");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Invalid string: azureSecret:secret, on path: test in transformer: azureSecret",
            results.getErrors().get(0).description());

    }

    @Test
    void processWithSecretClientProvided() throws GestaltConfigurationException {

        TokenCredential tokenCredential = Mockito.mock();

        AzureModuleBuilder azureConfigExtension = AzureModuleBuilder.builder()
            .setKeyVaultUri("myURI")
            .setCredential(tokenCredential);

        AzureModuleConfig azureModuleConfig = azureConfigExtension.build();
        azureModuleConfig.setSecretsClient(secretClient);

        AzureSecretTransformer transform = new AzureSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(azureModuleConfig);

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        when(secretClient.getSecret("secret")).thenReturn(new KeyVaultSecret("secret", "hello world"));

        var results = transform.process("test", "secret", "awsSecret:secret");

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("hello world", results.results());

    }

    @Test
    void noAzureConfigSet() throws GestaltConfigurationException {

        TokenCredential tokenCredential = Mockito.mock();

        AzureModuleBuilder azureConfigExtension = AzureModuleBuilder.builder();
        azureConfigExtension.setKeyVaultUri("myURI");
        azureConfigExtension.setCredential(tokenCredential);
        AzureModuleConfig azureModuleConfig = azureConfigExtension.build();
        azureModuleConfig.setSecretsClient(secretClient);

        AzureSecretTransformer transform = new AzureSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(azureModuleConfig);

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        when(secretClient.getSecret("secret")).thenThrow(new RuntimeException("something bad happened"));

        var results = transform.process("test", "secret", "azureSecret:secret");
        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Exception thrown while loading Azure secret: secret, on path: test in transformer: azureSecret, " +
                "with message: something bad happened",
            results.getErrors().get(0).description());
    }
}

