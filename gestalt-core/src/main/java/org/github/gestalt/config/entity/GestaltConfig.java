package org.github.gestalt.config.entity;

import java.time.format.DateTimeFormatter;

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
    private DateTimeFormatter dateDecoderFormat = DateTimeFormatter.ISO_DATE_TIME;
    // Java local date time decoder format.
    private DateTimeFormatter localDateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;
    // Java local date decoder format.
    private DateTimeFormatter localDateFormat = DateTimeFormatter.ISO_DATE_TIME;


    // Token that represents the opening of a string substitution.
    private String substitutionOpeningToken = "${";

    // Token that represents the closing of a string substitution.
    private String substitutionClosingToken = "}";

    // the maximum nested substitution depth.
    private int maxSubstitutionNestedDepth = 5;

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
    public DateTimeFormatter getDateDecoderFormat() {
        return dateDecoderFormat;
    }

    /**
     * Java date decoder format.
     *
     * @param dateDecoderFormat Java date decoder format
     */
    public void setDateDecoderFormat(DateTimeFormatter dateDecoderFormat) {
        this.dateDecoderFormat = dateDecoderFormat;
    }

    /**
     * Java local date time decoder format.
     *
     * @return Java local date time decoder format.
     */
    public DateTimeFormatter getLocalDateTimeFormat() {
        return localDateTimeFormat;
    }

    /**
     * Java local date time decoder format.
     *
     * @param localDateTimeFormat Java local date time decoder format.
     */
    public void setLocalDateTimeFormat(DateTimeFormatter localDateTimeFormat) {
        this.localDateTimeFormat = localDateTimeFormat;
    }

    /**
     * Java local date decoder format.
     *
     * @return Java local date decoder format.
     */
    public DateTimeFormatter getLocalDateFormat() {
        return localDateFormat;
    }

    /**
     * Java local date decoder format.
     *
     * @param localDateFormat Java local date decoder format.
     */
    public void setLocalDateFormat(DateTimeFormatter localDateFormat) {
        this.localDateFormat = localDateFormat;
    }

    /**
     * Get the token that represents the opening of a string substitution.
     *
     * @return Token that represents the opening of a string substitution.
     */
    public String getSubstitutionOpeningToken() {
        return substitutionOpeningToken;
    }

    /**
     * Set the token that represents the opening of a string substitution.
     *
     * @param substitutionOpeningToken Token that represents the opening of a string substitution.
     */
    public void setSubstitutionOpeningToken(String substitutionOpeningToken) {
        this.substitutionOpeningToken = substitutionOpeningToken;
    }

    /**
     * Get the token that represents the closing of a string substitution.
     *
     * @return Token that represents the closing of a string substitution.
     */
    public String getSubstitutionClosingToken() {
        return substitutionClosingToken;
    }

    /**
     * Set the token that represents the opening of a string substitution.
     *
     * @param substitutionClosingToken Token that represents the closing of a string substitution.
     */
    public void setSubstitutionClosingToken(String substitutionClosingToken) {
        this.substitutionClosingToken = substitutionClosingToken;
    }

    /**
     * Get the maximum string substitution nested depth.
     * If you have nested or recursive substitutions that go deeper than this it will fail.
     *
     * @return the maximum string substitution nested depth.
     */
    public int getMaxSubstitutionNestedDepth() {
        return maxSubstitutionNestedDepth;
    }

    /**
     * Set the maximum string substitution nested depth.
     * If you have nested or recursive substitutions that go deeper than this it will fail.
     *
     * @param maxSubstitutionNestedDepth the maximum string substitution nested depth.
     */
    public void setMaxSubstitutionNestedDepth(int maxSubstitutionNestedDepth) {
        this.maxSubstitutionNestedDepth = maxSubstitutionNestedDepth;
    }
}
