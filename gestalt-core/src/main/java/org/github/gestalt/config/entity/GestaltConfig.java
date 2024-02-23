package org.github.gestalt.config.entity;

import org.github.gestalt.config.decoder.ProxyDecoderMode;
import org.github.gestalt.config.post.process.transform.TransformerPostProcessor;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Gestalt.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class GestaltConfig {

    @SuppressWarnings("rawtypes")
    private final Map<Class, GestaltModuleConfig> modulesConfig = new HashMap<>();
    // Treat all warnings as errors
    private boolean treatWarningsAsErrors = false;
    // Treat missing array index's as errors. If false it will inject null values for missing array index's.
    private boolean treatMissingArrayIndexAsError = false;
    // Treat missing object values as errors. If false it will leave the default values or null.
    private boolean treatMissingValuesAsErrors = false;
    // Treat null values in classes after decoding as errors.
    private boolean treatNullValuesInClassAsErrors = true;
    // For the proxy decoder, if we should use a cached value or call gestalt for the most recent value.
    private ProxyDecoderMode proxyDecoderMode = ProxyDecoderMode.CACHE;
    // Provide the log level when we log a message when a config is missing, but we provided a default, or it is Optional.
    private System.Logger.Level logLevelForMissingValuesWhenDefaultOrOptional = System.Logger.Level.DEBUG;
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
    // the regex used to parse string substitutions.
    // Must have a named capture group transform, key, and default, where the key is required and the transform and default are optional.
    private String substitutionRegex = TransformerPostProcessor.DEFAULT_SUBSTITUTION_REGEX;

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
     * Get For the proxy decoder mode, if we should use a cached value or call gestalt for the most recent value.
     *
     * @return the proxy decoder mode
     */
    public ProxyDecoderMode getProxyDecoderMode() {
        return proxyDecoderMode;
    }

    /**
     * Set For the proxy decoder mode, if we should use a cached value or call gestalt for the most recent value.
     *
     * @param proxyDecoderMode if we should use a cached value or call gestalt for the most recent value.
     */
    public void setProxyDecoderMode(ProxyDecoderMode proxyDecoderMode) {
        this.proxyDecoderMode = proxyDecoderMode;
    }


    /**
     * Provide the log level when we log a message when a config is missing, but we provided a default, or it is Optional.
     *
     * @return Log level
     */
    public System.Logger.Level getLogLevelForMissingValuesWhenDefaultOrOptional() {
        return logLevelForMissingValuesWhenDefaultOrOptional;
    }

    /**
     * Provide the log level when we log a message when a config is missing, but we provided a default, or it is Optional.
     *
     * @param logLevelForMissingValuesWhenDefaultOrOptional Log level
     */
    public void setLogLevelForMissingValuesWhenDefaultOrOptional(System.Logger.Level logLevelForMissingValuesWhenDefaultOrOptional) {
        this.logLevelForMissingValuesWhenDefaultOrOptional = logLevelForMissingValuesWhenDefaultOrOptional;
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


    /**
     * the regex used to parse string substitutions.
     * Must have a named capture group transform, key, and default, where the key is required and the transform and default are optional.
     *
     * @return the string substitution regex
     */
    public String getSubstitutionRegex() {
        return substitutionRegex;
    }

    /**
     * the regex used to parse string substitutions.
     * Must have a named capture group transform, key, and default, where the key is required and the transform and default are optional.
     *
     * @param substitutionRegex the string substitution regex
     */
    public void setSubstitutionRegex(String substitutionRegex) {
        this.substitutionRegex = substitutionRegex;
    }

    public void registerModuleConfig(GestaltModuleConfig module) {
        modulesConfig.put(module.getClass(), module);
    }

    @SuppressWarnings("rawtypes")
    public void registerModuleConfig(Map<Class, GestaltModuleConfig> module) {
        modulesConfig.putAll(module);
    }

    @SuppressWarnings("unchecked")
    public <T extends GestaltModuleConfig> T getModuleConfig(Class<T> klass) {
        return (T) modulesConfig.get(klass);
    }
}
