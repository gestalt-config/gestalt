package org.github.gestalt.config.entity;

/**
 * Configuration for Gestalt.
 *
 * @author Colin Redmond
 */
public class GestaltConfig {

    // Treat all warnings as errors
    private boolean treatWarningsAsErrors = false;
    // Treat missing array index's as errors. If false it will inject null values for missing array index's.
    private boolean treatMissingArrayIndexAsError = false;
    // Treat missing object values as errors. If false it will leave the default values or null.
    private boolean treatMissingValuesAsErrors = false;

    // this setting is specific to environment vars loader, as there can often be false positive errors.
    private boolean envVarsTreatErrorsAsWarnings = false;

    // Java date decoder format.
    private String dateDecoderFormat = null;
    // Java local date time decoder format.
    private String localDateTimeFormat = null;
    // Java local date decoder format.
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
