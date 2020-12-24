package org.config.gestalt.entity;

public class GestaltConfig {

    private boolean treatWarningsAsErrors = false;
    private boolean treatMissingArraysAsError = false;

    public boolean isTreatWarningsAsErrors() {
        return treatWarningsAsErrors;
    }

    public void setTreatWarningsAsErrors(boolean treatWarningsAsErrors) {
        this.treatWarningsAsErrors = treatWarningsAsErrors;
    }

    public boolean isTreatMissingArraysAsError() {
        return treatMissingArraysAsError;
    }

    public void setTreatMissingArraysAsError(boolean treatMissingArraysAsError) {
        this.treatMissingArraysAsError = treatMissingArraysAsError;
    }
}
