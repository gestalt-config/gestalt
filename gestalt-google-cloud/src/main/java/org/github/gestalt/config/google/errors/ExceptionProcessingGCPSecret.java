package org.github.gestalt.config.google.errors;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;

/**
 * Validation errors for Google Cloud Module.
 *
 * @author Colin Redmond (c) 2023.
 */
public final class ExceptionProcessingGCPSecret extends ValidationError {
    private final String path;
    private final String secret;
    private final String transformer;

    private final Exception ex;

    public ExceptionProcessingGCPSecret(String path, String secret, String transformer, Exception ex) {
        super(ValidationLevel.ERROR);
        this.path = path;
        this.secret = secret;
        this.transformer = transformer;
        this.ex = ex;
    }

    @Override
    public String description() {
        return "Exception thrown while loading GCP secret: " + secret + ", on path: " + path + " in transformer: " + transformer +
            ", with message: " + ex.getMessage();
    }
}

