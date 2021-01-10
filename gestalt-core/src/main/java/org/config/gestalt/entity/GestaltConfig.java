package org.config.gestalt.entity;

public class GestaltConfig {

    private boolean treatWarningsAsErrors = false;
    private boolean treatMissingArrayIndexAsError = false;
    private boolean treatMissingValuesAsErrors = false;

    // this setting is specific to environment vars loader, as there can often be false positive errors.
    private boolean envVarsTreatErrorsAsWarnings = false;

    private String dateDecoderFormat = null;
    private String localDateTimeFormat = null;
    private String localDateFormat = null;

    public boolean isTreatWarningsAsErrors() {
        return treatWarningsAsErrors;
    }

    public void setTreatWarningsAsErrors(boolean treatWarningsAsErrors) {
        this.treatWarningsAsErrors = treatWarningsAsErrors;
    }

    public boolean isTreatMissingArrayIndexAsError() {
        return treatMissingArrayIndexAsError;
    }

    public void setTreatMissingArrayIndexAsError(boolean treatMissingArrayIndexAsError) {
        this.treatMissingArrayIndexAsError = treatMissingArrayIndexAsError;
    }

    public boolean isTreatMissingValuesAsErrors() {
        return treatMissingValuesAsErrors;
    }

    public void setTreatMissingValuesAsErrors(boolean treatMissingValuesAsErrors) {
        this.treatMissingValuesAsErrors = treatMissingValuesAsErrors;
    }

    public boolean isEnvVarsTreatErrorsAsWarnings() {
        return envVarsTreatErrorsAsWarnings;
    }

    public void setEnvVarsTreatErrorsAsWarnings(boolean envVarsTreatErrorsAsWarnings) {
        this.envVarsTreatErrorsAsWarnings = envVarsTreatErrorsAsWarnings;
    }

    public String getDateDecoderFormat() {
        return dateDecoderFormat;
    }

    public void setDateDecoderFormat(String dateDecoderFormat) {
        this.dateDecoderFormat = dateDecoderFormat;
    }

    public String getLocalDateTimeFormat() {
        return localDateTimeFormat;
    }

    public void setLocalDateTimeFormat(String localDateTimeFormat) {
        this.localDateTimeFormat = localDateTimeFormat;
    }

    public String getLocalDateFormat() {
        return localDateFormat;
    }

    public void setLocalDateFormat(String localDateFormat) {
        this.localDateFormat = localDateFormat;
    }

}
