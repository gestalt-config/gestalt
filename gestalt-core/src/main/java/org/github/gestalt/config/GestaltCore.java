package org.github.gestalt.config;

import org.github.gestalt.config.annotations.ConfigPrefix;
import org.github.gestalt.config.decoder.DecoderContext;
import org.github.gestalt.config.decoder.DecoderService;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.metrics.MetricsManager;
import org.github.gestalt.config.metrics.MetricsMarker;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.ConfigReloadListener;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.CoreReloadListenersContainer;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.tag.Tag;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ClassUtils;
import org.github.gestalt.config.utils.ErrorsUtil;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.validation.ValidationManager;

import java.util.*;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Central access point to Gestalt that has API's to build and get configurations.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class GestaltCore implements Gestalt, ConfigReloadListener {
    private static final System.Logger logger = System.getLogger(GestaltCore.class.getName());

    private final ConfigLoaderService configLoaderService;
    private final List<ConfigSourcePackage> sourcePackages;
    private final DecoderService decoderService;
    private final SentenceLexer sentenceLexer;
    private final GestaltConfig gestaltConfig;
    private final ConfigNodeService configNodeService;
    private final CoreReloadListenersContainer coreReloadListenersContainer;
    private final List<PostProcessor> postProcessors;

    private final SecretConcealer secretConcealer;

    private final List<ValidationError> loadErrors = new ArrayList<>();

    private final Tags defaultTags;

    private final MetricsManager metricsManager;

    private final ValidationManager validationManager;

    private final DecoderContext decoderContext;

    /**
     * Constructor for Gestalt,you can call it manually but the best way to use this is though the GestaltBuilder.
     *
     * @param configLoaderService  configLoaderService to hold all config loaders
     * @param configSourcePackages sources we wish to load the configs from. We load the sources in the order they are provided.
     *                             Overriding older values with new one where needed
     * @param decoderService       decoderService to hold all decoders
     * @param sentenceLexer        sentenceLexer to parse the configuration paths when doing searches.
     * @param gestaltConfig        configuration for the Gestalt
     * @param configNodeService    configNodeService core functionality to manage nodes
     * @param reloadStrategy       reloadStrategy holds all reload listeners
     * @param postProcessor        postProcessor list of post processors
     * @param secretConcealer      Utility for concealing secrets
     * @param metricsManager       Manages reporting of metrics
     * @param validationManager    Validation Manager, for validating configuration objects
     * @param defaultTags          Default set of tags to apply to all calls to get a configuration where tags are not provided.
     */
    public GestaltCore(ConfigLoaderService configLoaderService, List<ConfigSourcePackage> configSourcePackages,
                       DecoderService decoderService, SentenceLexer sentenceLexer, GestaltConfig gestaltConfig,
                       ConfigNodeService configNodeService, CoreReloadListenersContainer reloadStrategy,
                       List<PostProcessor> postProcessor, SecretConcealer secretConcealer,
                       MetricsManager metricsManager, ValidationManager validationManager, Tags defaultTags) {
        this.configLoaderService = configLoaderService;
        this.sourcePackages = configSourcePackages;
        this.decoderService = decoderService;
        this.sentenceLexer = sentenceLexer;
        this.gestaltConfig = gestaltConfig;
        this.configNodeService = configNodeService;
        this.coreReloadListenersContainer = reloadStrategy;
        this.postProcessors = postProcessor != null ? postProcessor : Collections.emptyList();
        this.secretConcealer = secretConcealer;
        this.metricsManager = metricsManager;
        this.validationManager = validationManager;
        this.defaultTags = defaultTags;
        this.decoderContext = new DecoderContext(decoderService, this, secretConcealer);
    }

    List<ValidationError> getLoadErrors() {
        return loadErrors;
    }

    public DecoderService getDecoderService() {
        return decoderService;
    }

    public DecoderContext getDecoderContext() {
        return decoderContext;
    }

    /**
     * register a core event listener.
     *
     * @param listener to register
     */
    @Override
    public void registerListener(CoreReloadListener listener) {
        coreReloadListenersContainer.registerListener(listener);
    }

    /**
     * remove a core event listener.
     *
     * @param listener to remove
     */
    @Override
    public void removeListener(CoreReloadListener listener) {
        coreReloadListenersContainer.removeListener(listener);
    }

    @Override
    public void loadConfigs() throws GestaltException {
        if (sourcePackages == null || sourcePackages.isEmpty()) {
            throw new GestaltException("No sources provided, unable to load any configs");
        }

        for (ConfigSourcePackage sourcePackage : sourcePackages) {
            ConfigSource source = sourcePackage.getConfigSource();
            ConfigLoader configLoader = configLoaderService.getLoader(source.format());

            GResultOf<List<ConfigNodeContainer>> newNode = configLoader.loadSource(sourcePackage);

            validateLoadResultsForErrors(newNode, source);
            if (newNode.hasResults()) {
                for (ConfigNodeContainer node : newNode.results()) {
                    GResultOf<ConfigNode> mergedNode = configNodeService.addNode(node);
                    validateLoadResultsForErrors(mergedNode, source);
                    loadErrors.addAll(mergedNode.getErrors());
                }
            } else {
                logger.log(WARNING, "Failed to load node: {0} did not have any results", source.name());
            }
        }

        postProcessConfigs();
    }

    /**
     * Find the specific source that we wish to reload.
     * Then reload the config and update the configNodeService with the new config node tree.
     *
     * @param reloadSourcePackage source to reload
     * @throws GestaltException any exception
     */
    @Override
    public void reload(ConfigSourcePackage reloadSourcePackage) throws GestaltException {

        MetricsMarker reloadMarker = null;
        try {
            if (gestaltConfig.isMetricsEnabled() && metricsManager != null) {
                reloadMarker = metricsManager.startMetric("reload",
                    Tags.of(Tags.of("source", reloadSourcePackage.getConfigSource().name()), reloadSourcePackage.getTags()));
            }

            if (reloadSourcePackage == null) {
                throw new GestaltException("No sources provided, unable to reload any configs");
            }

            if (sourcePackages == null || sourcePackages.isEmpty()) {
                throw new GestaltException("No sources provided, unable to reload any configs");
            }

            var sourcePackageOpt = sourcePackages.stream().filter(it -> it.equals(reloadSourcePackage)).findFirst();
            if (sourcePackageOpt.isEmpty()) {
                throw new GestaltException("Can not reload a source that was not registered.");
            }

            var reloadSource = sourcePackageOpt.get().getConfigSource();

            ConfigLoader configLoader = configLoaderService.getLoader(reloadSourcePackage.getConfigSource().format());
            var reloadNodes = configLoader.loadSource(sourcePackageOpt.get());
            validateLoadResultsForErrors(reloadNodes, reloadSource);

            reloadNodes.throwIfNoResults(() -> new GestaltException("no results found reloading source " + reloadSource.name()));

            for (ConfigNodeContainer reloadNode : reloadNodes.results()) {
                GResultOf<ConfigNode> mergedNode = configNodeService.reloadNode(reloadNode);
                validateLoadResultsForErrors(mergedNode, reloadSource);

                mergedNode.throwIfNoResults(() -> new GestaltException("no results found merging source " + reloadSource.name()));

                postProcessConfigs();
            }

            coreReloadListenersContainer.reload();
            if (gestaltConfig.isMetricsEnabled() && metricsManager != null) {
                metricsManager.finalizeMetric(reloadMarker, Tags.of());
            }
        } catch (Exception ex) {
            if (gestaltConfig.isMetricsEnabled() && metricsManager != null) {
                metricsManager.finalizeMetric(reloadMarker, Tags.of("exception", ex.getClass().getCanonicalName()));
            }
            throw ex;
        }
    }

    void postProcessConfigs() throws GestaltException {
        GResultOf<Boolean> results = configNodeService.postProcess(postProcessors);

        if (checkErrorsShouldFail(results)) {
            throw new GestaltException("Failed post processing config nodes with errors ",
                results.getErrors());

        } else if (results.hasErrors() && logger.isLoggable(DEBUG)) {
            String errorMsg = ErrorsUtil.buildErrorMessage("Failed post processing config nodes with errors ",
                results.getErrors());
            logger.log(DEBUG, errorMsg);
        }

        results.throwIfNoResults(() -> new GestaltException("no results found post processing the config nodes"));
    }

    private void validateLoadResultsForErrors(GResultOf<?> results, ConfigSource source)
        throws GestaltConfigurationException {
        if ((gestaltConfig.isTreatWarningsAsErrors() && results.hasErrors()) || // NOPMD
            (results.hasErrors(ValidationLevel.ERROR) && source.failOnErrors())) {  // NOPMD
            throw new GestaltConfigurationException("Failed to load configs from source: " + source.name(), results.getErrors());
        }

        if (results.hasErrors(ValidationLevel.WARN) && logger.isLoggable(WARNING)) {
            String errorMsg = ErrorsUtil.buildErrorMessage(results.getErrors());
            logger.log(WARNING, errorMsg);
        }

        results.throwIfNoResults(() -> new GestaltConfigurationException("No results found for node"));
    }

    private <T> String buildPathWithConfigPrefix(TypeCapture<T> klass, String path) {
        StringBuilder combinedPath = new StringBuilder(path);
        // if the type is annotated with ConfigPrefix add the prefix after the path.
        // if there are multiple annotations, add each of the prefix in order
        ConfigPrefix[] prefix = klass.getAnnotationsByType(ConfigPrefix.class);
        for (ConfigPrefix configPrefix : prefix) {
            if (combinedPath.length() > 0) {
                combinedPath.append(sentenceLexer.getDeliminator());
            }
            combinedPath.append(configPrefix.prefix());
        }

        return combinedPath.toString();
    }

    @Override
    public <T> T getConfig(String path, Class<T> klass) throws GestaltException {
        Objects.requireNonNull(klass);

        return getConfig(path, TypeCapture.of(klass));
    }

    @Override
    public <T> T getConfig(String path, Class<T> klass, Tags tags) throws GestaltException {
        Objects.requireNonNull(klass);

        return getConfig(path, TypeCapture.of(klass), tags);
    }

    @Override
    public <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException {
        return getConfig(path, klass, defaultTags);
    }

    @Override
    public <T> T getConfig(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        // fail on errors if this is not an optional type.
        // get the default value used if this is an optional type
        // most likely an optional.empty()
        Pair<Boolean, T> isOptionalAndDefault = ClassUtils.isOptionalAndDefault(klass.getRawType());

        return getConfigurationInternal(path, !isOptionalAndDefault.getFirst(), isOptionalAndDefault.getSecond(), klass, tags);
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, Class<T> klass) {
        Objects.requireNonNull(klass);

        return getConfig(path, defaultVal, TypeCapture.of(klass));
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, Class<T> klass, Tags tags) {
        Objects.requireNonNull(klass);

        return getConfig(path, defaultVal, TypeCapture.of(klass), tags);

    }

    @Override
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass) {
        return getConfig(path, defaultVal, klass, defaultTags);
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(defaultVal);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        try {
            return getConfigurationInternal(path, false, defaultVal, klass, tags);
        } catch (GestaltException e) {
            logger.log(WARNING, e.getMessage());
        }

        return defaultVal;
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass) {
        Objects.requireNonNull(klass);

        return getConfigOptional(path, TypeCapture.of(klass));
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass, Tags tags) {
        Objects.requireNonNull(klass);

        return getConfigOptional(path, TypeCapture.of(klass), tags);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass) {
        Objects.requireNonNull(klass);

        return getConfigOptional(path, klass, defaultTags);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        try {
            var results = getConfigurationInternal(path, false, null, klass, tags);
            return Optional.ofNullable(results);
        } catch (GestaltException e) {
            logger.log(WARNING, e.getMessage());
        }

        return Optional.empty();
    }

    private <T> T getConfigurationInternal(String path, boolean failOnErrors, T defaultVal, TypeCapture<T> klass, Tags tags)
        throws GestaltException {

        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);
        MetricsMarker getConfigMarker = null;
        boolean defaultReturned = false;
        Exception exceptionThrown = null;
        try {
            if (gestaltConfig.isMetricsEnabled() && metricsManager != null) {
                getConfigMarker = metricsManager.startGetConfig(path, klass, tags, failOnErrors);
            }

            String combinedPath = buildPathWithConfigPrefix(klass, path);
            GResultOf<List<Token>> tokens = sentenceLexer.scan(combinedPath);
            if (tokens.hasErrors()) {
                throw new GestaltException("Unable to parse path: " + combinedPath, tokens.getErrors());
            } else {
                GResultOf<T> results = getAndDecodeConfig2(combinedPath, tokens.results(), klass, tags);

                getConfigMetrics(results);

                if (checkErrorsShouldFail(results)) {
                    if (failOnErrors) {
                        throw new GestaltException("Failed getting config path: " + combinedPath + ", for class: " + klass.getName(),
                            results.getErrors());
                    } else {
                        if (logger.isLoggable(gestaltConfig.getLogLevelForMissingValuesWhenDefaultOrOptional())) {
                            String errorMsg = ErrorsUtil.buildErrorMessage("Failed getting config path: " + combinedPath +
                                ", for class: " + klass.getName() + " returning empty Optional", results.getErrors());
                            logger.log(gestaltConfig.getLogLevelForMissingValuesWhenDefaultOrOptional(), errorMsg);
                        }
                        defaultReturned = true;

                        return defaultVal;
                    }

                } else if (results.hasErrors() && logger.isLoggable(DEBUG)) {
                    String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting config path: " + combinedPath +
                        ", for class: " + klass.getName(), results.getErrors());
                    logger.log(DEBUG, errorMsg);
                }

                if (results.hasResults()) {
                    var resultConfig = results.results();

                    // if we have a result, lets check if validation is enabled and if we should validate the object,
                    // then validate the result.
                    if (gestaltConfig.isValidationEnabled() && shouldValidate(klass)) {
                        var validationResults = validationManager.validator(resultConfig, path, klass, tags);
                        // if there are validation errors we can either fail with an exception or return the default value.
                        if (validationResults.hasErrors()) {
                            updateValidationMetrics(validationResults);

                            if (failOnErrors) {
                                throw new GestaltException("Validation failed for config path: " + combinedPath +
                                    ", and class: " + klass.getName(), validationResults.getErrors());

                            } else {
                                if (logger.isLoggable(WARNING)) {
                                    String errorMsg = ErrorsUtil.buildErrorMessage("Validation failed for config path: " +
                                        combinedPath + ", and class: " + klass.getName() + " returning default value",
                                        validationResults.getErrors());
                                    logger.log(WARNING, errorMsg);
                                }
                                defaultReturned = true;

                                return defaultVal;
                            }
                        }
                    }

                    return resultConfig;
                }
            }

            if (logger.isLoggable(gestaltConfig.getLogLevelForMissingValuesWhenDefaultOrOptional())) {
                String errorMsg = ErrorsUtil.buildErrorMessage("No results for Optional config path: " + combinedPath +
                    ", and class: " + klass.getName() + " returning empty Optional", tokens.getErrors());
                logger.log(gestaltConfig.getLogLevelForMissingValuesWhenDefaultOrOptional(), errorMsg);
            }

            if (failOnErrors) {
                throw new GestaltException("No results for config path: " + combinedPath + ", and class: " + klass.getName());
            } else {
                defaultReturned = true;

                return defaultVal;
            }
        } catch (Exception ex) {
            exceptionThrown = ex;
            throw ex;
        } finally {
            finalizeMetrics(getConfigMarker, defaultReturned, exceptionThrown);
        }
    }

    private void finalizeMetrics(MetricsMarker getConfigMarker, boolean defaultReturned, Exception exceptionThrown) {
        if (gestaltConfig.isMetricsEnabled() && metricsManager != null && getConfigMarker != null) {
            Set<Tag> tagSet = new HashSet<>();
            if (defaultReturned) {
                tagSet.add(Tag.of("default", "true"));
            }

            if (exceptionThrown != null) {
                tagSet.add(Tag.of("exception", exceptionThrown.getClass().getCanonicalName()));
            }
            metricsManager.finalizeMetric(getConfigMarker, Tags.of(tagSet));
        }
    }

    private <T> GResultOf<T> getAndDecodeConfig2(String path, List<Token> tokens, TypeCapture<T> klass, Tags tags) {
        GResultOf<ConfigNode> node = configNodeService.navigateToNode(path, tokens, tags);

        if (!node.hasErrors() || node.hasErrors(ValidationLevel.MISSING_VALUE)) {
            // if we have no errors or the error is from a missing value, lets try and decode the node.
            // for missing values some decoders like optional decoders will handle the errors.
            GResultOf<T> decodedResults = decoderService.decodeNode(path, tags, node.results(), klass, decoderContext);

            // if we don't have a result and we received missing node errors.
            // return the errors from the call to navigate to node.
            // So we don't get too many "duplicate" errors
            // Otherwise if we have a result return both sets of errors.
            List<ValidationError> errors = new ArrayList<>();
            if (!decodedResults.hasResults() && node.hasErrors(ValidationLevel.MISSING_VALUE)) {
                errors.addAll(node.getErrors());
            } else {
                // if we have a result, only add non missing value errors.
                errors.addAll(node.getErrorsNotLevel(ValidationLevel.MISSING_VALUE));
                errors.addAll(decodedResults.getErrors());
            }

            return GResultOf.resultOf(decodedResults.results(), errors);
        } else {

            // if we have errors other than the missing values,
            // return the errors and do not attempt to decode.
            return GResultOf.errors(node.getErrors());
        }
    }

    private <T> boolean checkErrorsShouldFail(GResultOf<T> results) {
        if (results.hasErrors()) {
            return !results.getErrors().stream().allMatch(this::ignoreError) || !results.hasResults();
        } else {
            return false;
        }
    }

    private <T> void getConfigMetrics(GResultOf<T> results) throws GestaltException {
        if (gestaltConfig.isMetricsEnabled() && metricsManager != null) {
            int missing = results.getErrors(ValidationLevel.MISSING_VALUE).size();
            if (missing != 0) {
                metricsManager.recordMetric("get.config.missing", missing, Tags.of("optional", "false"));
            }

            int missingOptional = results.getErrors(ValidationLevel.MISSING_OPTIONAL_VALUE).size();
            if (missingOptional != 0) {
                metricsManager.recordMetric("get.config.missing", missingOptional, Tags.of("optional", "true"));
            }

            int errors = results.getErrors(ValidationLevel.ERROR).size();
            if (errors != 0) {
                metricsManager.recordMetric("get.config.error", errors, Tags.of());
            }

            int warnings = results.getErrors(ValidationLevel.WARN).size();
            if (warnings != 0) {
                metricsManager.recordMetric("get.config.warning", warnings, Tags.of());
            }
        }
    }

    private <T> void updateValidationMetrics(GResultOf<T> results) {
        if (gestaltConfig.isMetricsEnabled() && metricsManager != null) {
            int validationErrors = results.getErrors().size();
            if (validationErrors != 0) {
                metricsManager.recordMetric("get.config.validation.error", validationErrors, Tags.of());
            }
        }
    }

    private boolean ignoreError(ValidationError error) {
        if (error.level().equals(ValidationLevel.WARN) && gestaltConfig.isTreatWarningsAsErrors()) {
            return false;
        } else if (error instanceof ValidationError.ArrayMissingIndex && !gestaltConfig.isTreatMissingArrayIndexAsError()) {
            return true;
        } else if (error.hasNoResults() && !gestaltConfig.isTreatMissingValuesAsErrors()) {
            return true;
        } else if (error.level().equals(ValidationLevel.MISSING_OPTIONAL_VALUE) &&
            !gestaltConfig.isTreatMissingDiscretionaryValuesAsErrors()) {
            return true;
        }

        return error.level() == ValidationLevel.WARN || error.level() == ValidationLevel.DEBUG;
    }

    private <T> boolean shouldValidate(TypeCapture<T> klass) {
        return !klass.isAssignableFrom(String.class) && !ClassUtils.isPrimitiveOrWrapper(klass.getRawType());
    }

    /**
     * Prints out the contents of a config root for the tag.
     *
     * @param tags tags for the config root to print
     * @return string results
     */
    @Override
    public String debugPrint(Tags tags) {
        return configNodeService.debugPrintRoot(tags, secretConcealer);
    }

    /**
     * prints out the contents of all config roots.
     *
     * @return string results
     */
    @Override
    public String debugPrint() {
        return configNodeService.debugPrintRoot(secretConcealer);
    }
}
