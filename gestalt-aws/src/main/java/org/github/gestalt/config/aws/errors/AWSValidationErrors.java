package org.github.gestalt.config.aws.errors;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;

import java.util.Arrays;

/**
 * Validation errors for AWS Module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class AWSValidationErrors {

    public static class AWSModuleConfigNotSet extends ValidationError {
        private final String path;
        private final String rawSubstitution;

        public AWSModuleConfigNotSet(String path, String rawSubstitution) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.rawSubstitution = rawSubstitution;
        }

        @Override
        public String description() {
            return "AWSModuleConfig has not been registered. Register by creating a AWSBuilder " +
                "then registering the AWSBuilder.build() results with the Gestalt Builder.addModuleConfig(). " +
                "If you wish to use the aws module with string substitution " +
                "${" + rawSubstitution + "} on the path: " + path;
        }
    }

    public static class AWSSecretInvalid extends ValidationError {
        private final String path;
        private final String rawSubstitution;

        private final String[] parts;

        public AWSSecretInvalid(String path, String rawSubstitution, String[] parts) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.rawSubstitution = rawSubstitution;
            this.parts = parts;
        }

        @Override
        public String description() {
            return "AWS Secret must be in the format secretName:SecretKey " +
                " received ${" + rawSubstitution + "} with parts " + Arrays.toString(parts) + ", on the path: " + path;
        }
    }

    public static class AWSSecretDoesNotExist extends ValidationError {
        private final String path;
        private final String secretName;
        private final String secretKey;
        private final String rawValue;


        public AWSSecretDoesNotExist(String path, String secretName, String secretKey, String rawValue) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.secretName = secretName;
            this.secretKey = secretKey;
            this.rawValue = rawValue;
        }

        @Override
        public String description() {
            return "AWS Secret " + secretName + " does not have the key " + secretKey + ", on the path: " + path +
                " with substitution " + rawValue;
        }
    }

    public static class ExceptionProcessingAWSSecret extends ValidationError {
        private final String path;
        private final String secret;
        private final String transformer;

        private final Exception ex;

        public ExceptionProcessingAWSSecret(String path, String secret, String transformer, Exception ex) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.secret = secret;
            this.transformer = transformer;
            this.ex = ex;
        }

        @Override
        public String description() {
            return "Exception thrown while loading AWS secret: " + secret + ", on path: " + path + " in transformer: " + transformer +
                ", with message: " + ex.getMessage();
        }
    }
}


