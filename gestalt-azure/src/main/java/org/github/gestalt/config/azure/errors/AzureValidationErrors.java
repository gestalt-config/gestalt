package org.github.gestalt.config.azure.errors;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;

/**
 * Validation errors for Azure Module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class AzureValidationErrors {

    private AzureValidationErrors() { }

    public static class AzureModuleConfigNotSet extends ValidationError {
        private final String path;
        private final String rawSubstitution;

        public AzureModuleConfigNotSet(String path, String rawSubstitution) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.rawSubstitution = rawSubstitution;
        }

        @Override
        public String description() {
            return "AzureModuleConfig has not been registered. Register by creating a AzureModuleBuilder " +
                "then registering the AzureModuleBuilder.build() results with the Gestalt Builder.addModuleConfig(). " +
                "If you wish to use the Azure module with string substitution " +
                "${" + rawSubstitution + "} on the path: " + path;
        }
    }

    public static class ExceptionProcessingAzureSecret extends ValidationError {
        private final String path;
        private final String secret;
        private final String transformer;

        private final Exception ex;

        public ExceptionProcessingAzureSecret(String path, String secret, String transformer, Exception ex) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.secret = secret;
            this.transformer = transformer;
            this.ex = ex;
        }

        @Override
        public String description() {
            return "Exception thrown while loading Azure secret: " + secret + ", on path: " + path + " in transformer: " + transformer +
                ", with message: " + ex.getMessage();
        }
    }
}


