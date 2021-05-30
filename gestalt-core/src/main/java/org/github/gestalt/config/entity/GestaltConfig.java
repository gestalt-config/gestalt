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

    /**
     * Treat all warnings as errors.
     *
     * @return Treat all warnings as errors
     */
    public boolean isTreatWarningsAsErrors() {
        return treatWarningsAsErrors;
    }

    /**
     * Treat all warnings as errors.
     *
     * @param treatWarningsAsErrors Treat all warnings as errors.
     */
    public void setTreatWarningsAsErrors(boolean treatWarningsAsErrors) {
        this.treatWarningsAsErrors = treatWarningsAsErrors;
    }

    /**
     * Treat missing array index's as errors.
     *
     * @return Treat missing array index's as errors.
     */
    public boolean isTreatMissingArrayIndexAsError() {
        return treatMissingArrayIndexAsError;
    }

    /**
     * Treat missing array index's as errors.
     *
     * @param treatMissingArrayIndexAsError Treat missing array index's as errors.
     */
    public void setTreatMissingArrayIndexAsError(boolean treatMissingArrayIndexAsError) {
        this.treatMissingArrayIndexAsError = treatMissingArrayIndexAsError;
    }

    /**
     * Treat missing object values as errors.
     *
     * @return Treat missing object values as errors
     */
    public boolean isTreatMissingValuesAsErrors() {
        return treatMissingValuesAsErrors;
    }

    /**
     * Treat missing object values as errors.
     *
     * @param treatMissingValuesAsErrors Treat missing object values as errors
     */
    public void setTreatMissingValuesAsErrors(boolean treatMissingValuesAsErrors) {
        this.treatMissingValuesAsErrors = treatMissingValuesAsErrors;
    }

    /**
     * this setting is specific to environment vars loader and if we should ignore Env Vars Errors.
     *
     * @return if we should ignore Env Vars Errors
     */
    public boolean isEnvVarsTreatErrorsAsWarnings() {
        return envVarsTreatErrorsAsWarnings;
    }

    /**
     * this setting is specific to environment vars loader and if we should ignore Env Vars Errors.
     *
     * @param envVarsTreatErrorsAsWarnings this setting is specific to environment vars loader and if we should ignore Env Vars Errors.
     */
    public void setEnvVarsTreatErrorsAsWarnings(boolean envVarsTreatErrorsAsWarnings) {
        this.envVarsTreatErrorsAsWarnings = envVarsTreatErrorsAsWarnings;
    }

    /**
     * Java date decoder format.
     *
     * @return Java date decoder format
     */
    public String getDateDecoderFormat() {
        return dateDecoderFormat;
    }

    /**
     * Java date decoder format.
     *
     * @param dateDecoderFormat Java date decoder format
     */
    public void setDateDecoderFormat(String dateDecoderFormat) {
        this.dateDecoderFormat = dateDecoderFormat;
    }

    /**
     * Java local date time decoder format.
     *
     * @return Java local date time decoder format.
     */
    public String getLocalDateTimeFormat() {
        return localDateTimeFormat;
    }

    /**
     * Java local date time decoder format.
     *
     * @param localDateTimeFormat Java local date time decoder format.
     */
    public void setLocalDateTimeFormat(String localDateTimeFormat) {
        this.localDateTimeFormat = localDateTimeFormat;
    }

    /**
     * Java local date decoder format.
     *
     * @return Java local date decoder format.
     */
    public String getLocalDateFormat() {
        return localDateFormat;
    }

    /**
     * Java local date decoder format.
     *
     * @param localDateFormat Java local date decoder format.
     */
    public void setLocalDateFormat(String localDateFormat) {
        this.localDateFormat = localDateFormat;
    }
}
