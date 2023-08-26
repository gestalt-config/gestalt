package org.github.gestalt.config.vault;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.response.LogicalResponse;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.github.gestalt.config.post.process.transform.Transformer;
import org.github.gestalt.config.utils.ValidateOf;
import org.github.gestalt.config.vault.config.VaultModuleConfig;
import org.github.gestalt.config.vault.errors.VaultValidationErrors;

/**
 * Allows you to substitute a vault secret using ${vault:secretPath:secretKey}.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class VaultSecretTransformer implements Transformer {

    private static final System.Logger logger = System.getLogger(VaultSecretTransformer.class.getName());

    private Vault vault;

    @Override
    public String name() {
        return "vault";
    }

    @Override
    public void applyConfig(PostProcessorConfig config) {
        VaultModuleConfig moduleConfig = config.getConfig().getModuleConfig(VaultModuleConfig.class);

        if (moduleConfig == null) {
            logger.log(System.Logger.Level.WARNING,
                "VaultModuleConfig has not been registered. " +
                    "if you wish to use the vault module with string substitution ${vault:secretPath:secretKey} " +
                    "then you must register an VaultModuleConfig config moduleConfig using the builder");
        } else {
            if (moduleConfig.getVault() == null) {
                logger.log(System.Logger.Level.ERROR,
                    "VaultModuleConfig was registered but neither the VaultConfig nor the Vault client was provided");
            } else {
                vault = moduleConfig.getVault();
            }
        }
    }

    @Override
    public ValidateOf<String> process(String path, String secretName, String rawValue) {
        if (secretName != null) {
            try {
                String[] secretParts = secretName.split(":");

                if (secretParts.length != 2) {
                    return ValidateOf.inValid(new VaultValidationErrors.VaultSecretInvalid(path, rawValue, secretParts));
                }

                String secretPath = secretParts[0];
                String secretKey = secretParts[1];

                if (vault == null) {
                    return ValidateOf.inValid(new VaultValidationErrors.VaultModuleConfigNotSet(path, rawValue));
                }

                // get the values for the secret path
                LogicalResponse secretPathValues = vault.logical()
                    .read(secretPath);

                // check to see if the secret key exists
                if (!secretPathValues.getData().containsKey(secretKey)) {
                    return ValidateOf.inValid(new VaultValidationErrors.VaultSecretDoesNotExist(path, secretPath, secretKey, rawValue));
                }

                // get and return the secret key
                String value = secretPathValues.getData().get(secretKey);

                return ValidateOf.valid(value);

            } catch (Exception e) {
                return ValidateOf.inValid(new VaultValidationErrors.ExceptionProcessingVaultSecret(path, rawValue, name(), e));
            }
        } else {
            return ValidateOf.inValid(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}
