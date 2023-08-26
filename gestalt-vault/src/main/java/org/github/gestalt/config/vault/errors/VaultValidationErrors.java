package org.github.gestalt.config.vault.errors;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;

import java.util.Arrays;

/**
 * Validation errors for Vault Module.
 *
 * @author Colin Redmond (c) 2023.
 */
public final class VaultValidationErrors {

    public static class VaultModuleConfigNotSet extends ValidationError {
        private final String path;
        private final String rawSubstitution;

        public VaultModuleConfigNotSet(String path, String rawSubstitution) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.rawSubstitution = rawSubstitution;
        }

        @Override
        public String description() {
            return "VaultModuleConfig has not been registered. Register by creating a VaultBuilder " +
                "then registering the VaultBuilder.build() results with the Gestalt Builder.addModuleConfig(). " +
                "If you wish to use the vault module with string substitution " +
                "${" + rawSubstitution + "} on the path: " + path;
        }
    }

    public static class VaultSecretInvalid extends ValidationError {
        private final String path;
        private final String rawSubstitution;

        private final String[] parts;

        public VaultSecretInvalid(String path, String rawSubstitution, String[] parts) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.rawSubstitution = rawSubstitution;
            this.parts = parts;
        }

        @Override
        public String description() {
            return "Vault Secret must be in the format secretPath:SecretKey " +
                "received ${" + rawSubstitution + "} with parts " + Arrays.toString(parts) + ", on the path: " + path;
        }
    }

    public static class VaultSecretDoesNotExist extends ValidationError {
        private final String path;
        private final String secretName;
        private final String secretKey;
        private final String rawValue;


        public VaultSecretDoesNotExist(String path, String secretName, String secretKey, String rawValue) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.secretName = secretName;
            this.secretKey = secretKey;
            this.rawValue = rawValue;
        }

        @Override
        public String description() {
            return "Vault " + secretName + " does not have the key " + secretKey + ", on the path: " + path +
                " with substitution " + rawValue;
        }
    }

    public static class ExceptionProcessingVaultSecret extends ValidationError {
        private final String path;
        private final String secret;
        private final String transformer;

        private final Exception ex;

        public ExceptionProcessingVaultSecret(String path, String secret, String transformer, Exception ex) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.secret = secret;
            this.transformer = transformer;
            this.ex = ex;
        }

        @Override
        public String description() {
            return "Exception thrown while loading Vault secret: " + secret + ", on path: " + path + " in transformer: " + transformer +
                ", with message: " + ex.getMessage();
        }
    }
}


