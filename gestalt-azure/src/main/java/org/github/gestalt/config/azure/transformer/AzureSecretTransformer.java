package org.github.gestalt.config.azure.transformer;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.github.gestalt.config.azure.config.AzureModuleConfig;
import org.github.gestalt.config.azure.errors.AzureValidationErrors;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.processor.config.transform.Transformer;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Allows you to substitute an Azure secret using ${azureSecret:secretName}.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class AzureSecretTransformer implements Transformer {

    private static final System.Logger logger = System.getLogger(AzureSecretTransformer.class.getName());
    private SecretClient secretClient;

    @Override
    public String name() {
        return "azureSecret";
    }

    @Override
    public void applyConfig(ConfigNodeProcessorConfig config) {
        AzureModuleConfig moduleConfig = config.getConfig().getModuleConfig(AzureModuleConfig.class);

        if (moduleConfig == null) {
            logger.log(System.Logger.Level.WARNING, "AzureModuleConfig has not been registered. " +
                "if you wish to use the Azure module with string substitution ${azureSecret:key} " +
                "then you must register an AzureModuleConfig config moduleConfig using the builder");
        } else {
            if (moduleConfig.hasSecretsClient()) {
                secretClient = moduleConfig.getSecretsClient();
            } else if (moduleConfig.getKeyVaultUri() != null) {
                SecretClientBuilder secretClientBuilder = new SecretClientBuilder()
                    .vaultUrl(moduleConfig.getKeyVaultUri());

                // set the credentials if they were provided
                if (moduleConfig.getCredential() != null) {
                    secretClientBuilder.credential(moduleConfig.getCredential());
                } else {
                    secretClientBuilder.credential(new DefaultAzureCredentialBuilder().build());
                }
                secretClient = secretClientBuilder.buildClient();
            } else {
                logger.log(System.Logger.Level.ERROR, "AzureModuleConfig was registered but neither the secret client " +
                    "nor the keyVaultUri was provided");
            }
        }
    }

    @Override
    public GResultOf<String> process(String path, String secretNameKey, String rawValue) {
        if (secretNameKey != null) {
            try {
                if (secretClient == null) {
                    return GResultOf.errors(new AzureValidationErrors.AzureModuleConfigNotSet(path, rawValue));
                }

                KeyVaultSecret secret = secretClient.getSecret(secretNameKey);

                return GResultOf.result(secret.getValue());
            } catch (Exception ex) {
                return GResultOf.errors(new AzureValidationErrors.ExceptionProcessingAzureSecret(path, secretNameKey, name(), ex));
            }
        } else {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}

