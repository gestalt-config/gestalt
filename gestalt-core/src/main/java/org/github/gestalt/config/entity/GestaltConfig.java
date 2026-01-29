package org.github.gestalt.config.entity;

import org.github.gestalt.config.decoder.ProxyDecoderMode;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.processor.config.annotation.AnnotationConfigNodeProcessor;
import org.github.gestalt.config.processor.config.transform.StringSubstitutionProcessor;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Gestalt.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class GestaltConfig {

    @SuppressWarnings("rawtypes")
    private final Map<Class, GestaltModuleConfig> modulesConfig = new HashMap<>();
    // Treat all warnings as errors
    private boolean treatWarningsAsErrors = false;
    // Treat missing array index's as errors. If false it will inject null values for missing array index's.
    private boolean treatMissingArrayIndexAsError = true;
    // Treat missing object values as errors. If false it will leave the default values or null.
    private boolean treatMissingValuesAsErrors = true;
    // Treat missing discretionary values as errors
    private boolean treatMissingDiscretionaryValuesAsErrors = false;
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
    // Token that represents the opening of a string substitution.
    private String runTimeSubstitutionOpeningToken = "#{";
    // Token that represents the closing of a string substitution.
    private String runTimeSubstitutionClosingToken = "}";
    // Token that represents the opening of an annotation.
    private String annotationOpeningToken = "@{";
    // Token that represents the closing of an annotation.
    private String annotationClosingToken = "}";
    // trim the white space before and after an annotation.
    private Boolean annotationTrimWhiteSpace = true;
    // the regex used to parse annotations.
    // Must have a named capture group annotation, and parameter, where the annotation is required and the parameter is optional.
    private String annotationRegex = AnnotationConfigNodeProcessor.DEFAULT_ANNOTATION_REGEX;
    // the maximum nested substitution depth.
    private int maxSubstitutionNestedDepth = 5;
    // the regex used to parse string substitutions.
    // Must have a named capture group transform, key, and default, where the key is required and the transform and default are optional.
    private String substitutionRegex = StringSubstitutionProcessor.DEFAULT_SUBSTITUTION_REGEX;

    private String nodeIncludeKeyword = "$include";

    private Integer nodeNestedIncludeLimit = 5;

    // if observations should be enabled
    private boolean observationsEnabled = false;

    // Treat empty strings as absent
    private boolean treatEmptyStringAsAbsent = false;

    // The sentence lexer used for gestalt.
    private SentenceLexer sentenceLexer = new PathLexer();

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
     * Gets treat missing discretionary values (optional, fields with defaults, fields with default annotations) as an error.
     * If this is false you will be able to get the configuration with default values or an empty Optional.
     * If this is true, if a field is missing and would have had a default it will fail and throw an exception.
     *
     * @return treatMissingDiscretionaryValuesAsErrors the settings for treating missing discretionary values as errors.
     */
    public boolean isTreatMissingDiscretionaryValuesAsErrors() {
        return treatMissingDiscretionaryValuesAsErrors;
    }

    /**
     * Sets treat missing discretionary values (optional, fields with defaults, fields with default annotations) as an error.
     * If this is false you will be able to get the configuration with default values or an empty Optional.
     * If this is true, if a field is missing and would have had a default it will fail and throw an exception.
     *
     * @param treatMissingDiscretionaryValuesAsErrors the settings for treating missing discretionary values as errors.
     */
    public void setTreatMissingDiscretionaryValuesAsErrors(boolean treatMissingDiscretionaryValuesAsErrors) {
        this.treatMissingDiscretionaryValuesAsErrors = treatMissingDiscretionaryValuesAsErrors;
    }

    /**
     * Treat null values in classes after decoding as errors.
     *
     * @return Treat null values in classes after decoding as errors.
     * @deprecated This value is no longer used, Please use {@link #setTreatMissingDiscretionaryValuesAsErrors(boolean)}
     *     and {@link #setTreatMissingValuesAsErrors(boolean)}
     */
    @Deprecated(since = "0.25.0", forRemoval = true)
    public boolean isTreatNullValuesInClassAsErrors() {
        return false;
    }

    /**
     * Treat null values in classes after decoding as errors.
     *
     * @param treatNullValuesInClassAsErrors Treat null values in classes after decoding as errors.
     * @deprecated This value is no longer used, Please use {@link #setTreatMissingDiscretionaryValuesAsErrors(boolean)}
     *     and {@link #setTreatMissingValuesAsErrors(boolean)}
     */
    @Deprecated(since = "0.25.0", forRemoval = true)
    public void setTreatNullValuesInClassAsErrors(boolean treatNullValuesInClassAsErrors) {
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
     * Get the token that represents the opening of a run time string substitution.
     *
     * @return Token that represents the opening of a run time string substitution.
     */
    public String getRunTimeSubstitutionOpeningToken() {
        return runTimeSubstitutionOpeningToken;
    }

    /**
     * Set the token that represents the opening of a  run time string substitution.
     *
     * @param runtTimeSubstitutionOpeningToken Token that represents the opening of a  run time string substitution.
     */
    public void setRunTimeSubstitutionOpeningToken(String runtTimeSubstitutionOpeningToken) {
        this.runTimeSubstitutionOpeningToken = runtTimeSubstitutionOpeningToken;
    }

    /**
     * Get the token that represents the closing of a  run time string substitution.
     *
     * @return Token that represents the closing of a  run time string substitution.
     */
    public String getRunTimeSubstitutionClosingToken() {
        return runTimeSubstitutionClosingToken;
    }

    /**
     * Set the token that represents the opening of a  run time string substitution.
     *
     * @param runTimeSubstitutionClosingToken Token that represents the closing of a  run time string substitution.
     */
    public void setRunTimeSubstitutionClosingToken(String runTimeSubstitutionClosingToken) {
        this.runTimeSubstitutionClosingToken = runTimeSubstitutionClosingToken;
    }

    /**
     * Get the token that represents the opening of an annotation.
     *
     * @return Token that represents the opening of an annotation
     */
    public String getAnnotationOpeningToken() {
        return annotationOpeningToken;
    }

    /**
     * Set the token that represents the opening of an annotation.
     *
     * @param annotationOpeningToken token that represents the opening of an annotation.
     */
    public void setAnnotationOpeningToken(String annotationOpeningToken) {
        this.annotationOpeningToken = annotationOpeningToken;
    }

    /**
     * Get the token that represents the closing of an annotation.
     *
     * @return Token that represents the closing of an annotation.
     */
    public String getAnnotationClosingToken() {
        return annotationClosingToken;
    }

    /**
     * Set the token that represents the opening of an annotation.
     *
     * @param annotationClosingToken Token that represents the closing of an annotation.
     */
    public void setAnnotationClosingToken(String annotationClosingToken) {
        this.annotationClosingToken = annotationClosingToken;
    }

    /**
     * trim the white space before and after an annotation.
     *
     * @return trim the white space before and after an annotation.
     */
    public Boolean getAnnotationTrimWhiteSpace() {  // NOPMD
        return annotationTrimWhiteSpace;
    }

    /**
     * Set if we trim the white space before and after an annotation.
     *
     * @param annotationTrimWhiteSpace trim the white space before and after an annotation.
     */
    public void setAnnotationTrimWhiteSpace(Boolean annotationTrimWhiteSpace) {
        this.annotationTrimWhiteSpace = annotationTrimWhiteSpace;
    }

    /**
     * Get the regex for annotation processing.
     *
     * @return the regex for annotation processing
     */
    public String getAnnotationRegex() {
        return annotationRegex;
    }

    /**
     * Set the regex for annotation processing.
     *
     * @param annotationRegex the regex for annotation processing.
     */
    public void setAnnotationRegex(String annotationRegex) {
        this.annotationRegex = annotationRegex;
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

    /**
     * Get the keyword that is used to determine if a node is an import from a source.
     *
     * @return the keyword that is used to determine if a node is an import from a source
     */
    public String getNodeIncludeKeyword() {
        return nodeIncludeKeyword;
    }

    /**
     * Set the keyword that is used to determine if a node is an import from a source.
     *
     * @param nodeImportKeyword the keyword that is used to determine if a node is an import from a source
     */
    public void setNodeIncludeKeyword(String nodeImportKeyword) {
        this.nodeIncludeKeyword = nodeImportKeyword;
    }

    /**
     * Gets how many nested recursions during including nodes we will check before failing.
     *
     * @return how many nested recursions during including nodes we will check before failing
     */
    public Integer getNodeNestedIncludeLimit() {
        return nodeNestedIncludeLimit;
    }

    /**
     * Sets how many nested recursions during including nodes we will check before failing.
     *
     * @param nodeNestedIncludeLimit how many nested recursions during including nodes we will check before failing
     */
    public void setNodeNestedIncludeLimit(Integer nodeNestedIncludeLimit) {
        this.nodeNestedIncludeLimit = nodeNestedIncludeLimit;
    }

    /**
     * Get if the observations are enabled.
     *
     * @return if the observations are enabled
     */
    public boolean isObservationsEnabled() {
        return observationsEnabled;
    }

    /**
     * set if the observations are enabled.
     *
     * @param observationsEnabled if the observations are enabled
     */
    public void setObservationsEnabled(boolean observationsEnabled) {
        this.observationsEnabled = observationsEnabled;
    }

    /**
     * Returns whether empty string values should be treated as "absent" when binding
     * configuration to POJOs, <strong>only if the configuration key exists</strong>.
     *
     * <p>When this flag is {@code true} and the configuration contains a key whose value
     * is an empty string:
     * <ul>
     *   <li>For primitive and regular object fields, empty strings will <strong>not override</strong>
     *       the default field value.</li>
     *   <li>For {@link java.util.Optional} fields, empty strings will be converted to {@link java.util.Optional#empty()}.</li>
     *   <li>For {@link java.util.Map} or {@link java.util.List} fields, empty strings will result in
     *       an empty collection or cleared map.</li>
     * </ul>
     *
     * <p>If the key is <strong>absent</strong> in the configuration, the field retains
     * its default value, and no conversion or error is performed.
     *
     * <p>Note: empty strings in raw {@link java.util.Map} or other untyped views are
     * <strong>not affected</strong> and will retain the original empty string value.
     *
     * @return {@code true} if empty strings should be treated as absent during POJO binding when the key exists; {@code false} otherwise
     */
    public boolean isTreatEmptyStringAsAbsent() {
        return treatEmptyStringAsAbsent;
    }

    /**
     * Sets whether empty string values should be treated as "absent" when binding
     * configuration to POJOs, <strong>only if the configuration key exists</strong>.
     *
     * <p>See {@link #isTreatEmptyStringAsAbsent()} for detailed behavior depending on
     * field type.
     *
     * @param treatEmptyStringAsAbsent {@code true} to treat empty strings as absent in POJO binding
     *                                 when the key exists; {@code false} to treat them as literal empty strings
     */
    public void setTreatEmptyStringAsAbsent(boolean treatEmptyStringAsAbsent) {
        this.treatEmptyStringAsAbsent = treatEmptyStringAsAbsent;
    }

    /**
     * Get the sentence lexer that will be passed through to the DecoderRegistry.
     * it is used to convert the path requested to tokens, so we can navigate the config tree using the tokens.
     *
     * @return SentenceLexer the lexer
     */
    public SentenceLexer getSentenceLexer() {
        return sentenceLexer;
    }

    /**
     * Set the sentence lexer that will be passed through to the DecoderRegistry.
     * it is used to convert the path requested to tokens, so we can navigate the config tree using the tokens.
     *
     * @param sentenceLexer for the DecoderRegistry
     */
    public void setSentenceLexer(SentenceLexer sentenceLexer) {
        this.sentenceLexer = sentenceLexer;
    }

    /**
     * Register an external module configuration.
     *
     * @param module configuration
     */
    public void registerModuleConfig(GestaltModuleConfig module) {
        modulesConfig.put(module.getClass(), module);
    }

    /**
     * Register external module configurations.
     *
     * @param module configuration
     */
    @SuppressWarnings("rawtypes")
    public void registerModuleConfig(Map<Class, GestaltModuleConfig> module) {
        modulesConfig.putAll(module);
    }

    /**
     * Get an external module configuration.
     *
     * @param klass type of configuration to get
     * @param <T> type of the class
     * @return the module config for a class
     */
    @SuppressWarnings("unchecked")
    public <T extends GestaltModuleConfig> T getModuleConfig(Class<T> klass) {
        return (T) modulesConfig.get(klass);
    }
}
