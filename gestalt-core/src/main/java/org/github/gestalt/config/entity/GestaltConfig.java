package org.github.gestalt.config.entity;

/**
 * Configuration for Gestalt.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class GestaltConfig {

    // Treat all warnings as errors
    private boolean treatWarningsAsErrors = false;
    // Treat missing array index's as errors. If false it will inject null values for missing array index's.
    private boolean treatMissingArrayIndexAsError = false;
    // Treat missing object values as errors. If false it will leave the default values or null.
    private boolean treatMissingValuesAsErrors = false;
    //Treat null values in classes after decoding as errors.
    private boolean treatNullValuesInClassAsErrors = true;

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
     * Treat null values in classes after decoding as errors.
     *
     * @return Treat null values in classes after decoding as errors.
     */
    public boolean isTreatNullValuesInClassAsErrors() {
        return treatNullValuesInClassAsErrors;
    }

    /**
     * Treat null values in classes after decoding as errors.
     *
     * @param treatNullValuesInClassAsErrors Treat null values in classes after decoding as errors.
     */
    public void setTreatNullValuesInClassAsErrors(boolean treatNullValuesInClassAsErrors) {
        this.treatNullValuesInClassAsErrors = treatNullValuesInClassAsErrors;
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
