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
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.TagMergingStrategy;
import org.github.gestalt.config.observations.ObservationMarker;
import org.github.gestalt.config.observations.ObservationService;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorService;
import org.github.gestalt.config.processor.result.ResultsProcessorService;
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

import java.util.*;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;
import static org.github.gestalt.config.utils.ErrorsUtil.checkErrorsShouldFail;

/**
 * Central access point to Gestalt that has API's to build and get configurations.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class GestaltCore implements Gestalt, ConfigReloadListener {
    private static final System.Logger logger = System.getLogger(GestaltCore.class.getName());

    private final ConfigLoaderService configLoaderService;
    private final List<ConfigSourcePackage> sourcePackages;
    private final DecoderService decoderService;
    private final SentenceLexer sentenceLexer;
    private final GestaltConfig gestaltConfig;
    private final ConfigNodeService configNodeService;
    private final ConfigNodeProcessorService configNodeProcessorService;
    private final CoreReloadListenersContainer coreReloadListenersContainer;

    private final SecretConcealer secretConcealer;

    private final List<ValidationError> loadErrors = new ArrayList<>();

    private final Tags defaultTags;

    private final ObservationService observationService;

    private final ResultsProcessorService resultsProcessorService;

    private final DecoderContext decoderContext;

    private final TagMergingStrategy tagMergingStrategy;

    /**
     * Constructor for Gestalt,you can call it manually but the best way to use this is though the GestaltBuilder.
     *
     * @param configLoaderService     configLoaderService to hold all config loaders
     * @param configSourcePackages    sources we wish to load the configs from. We load the sources in the order they are provided.
     *                                Overriding older values with new one where needed
     * @param decoderService          decoderService to hold all decoders
     * @param sentenceLexer           sentenceLexer to parse the configuration paths when doing searches.
     * @param gestaltConfig           configuration for the Gestalt
     * @param configNodeService       configNodeService core functionality to manage nodes
     * @param reloadStrategy          reloadStrategy holds all reload listeners
     * @param secretConcealer         Utility for concealing secrets
     * @param observationService      Manages reporting of observations
     * @param resultsProcessorService Validation Manager, for validating configuration objects
     * @param defaultTags             Default set of tags to apply to all calls to get a configuration where tags are not provided.
     * @param tagMergingStrategy      Strategy for how to merge tags
     */
    public GestaltCore(ConfigLoaderService configLoaderService, List<ConfigSourcePackage> configSourcePackages,
                       DecoderService decoderService,
                       SentenceLexer sentenceLexer,
                       GestaltConfig gestaltConfig,
                       ConfigNodeService configNodeService,
                       ConfigNodeProcessorService configNodeProcessorService,
                       CoreReloadListenersContainer reloadStrategy,
                       SecretConcealer secretConcealer,
                       ObservationService observationService,
                       ResultsProcessorService resultsProcessorService,
                       Tags defaultTags, TagMergingStrategy tagMergingStrategy) {
        this.configLoaderService = configLoaderService;
        this.sourcePackages = configSourcePackages;
        this.decoderService = decoderService;
        this.sentenceLexer = sentenceLexer;
        this.gestaltConfig = gestaltConfig;
        this.configNodeService = configNodeService;
        this.configNodeProcessorService = configNodeProcessorService;
        this.coreReloadListenersContainer = reloadStrategy;
        this.secretConcealer = secretConcealer;
        this.observationService = observationService;
        this.resultsProcessorService = resultsProcessorService;
        this.defaultTags = defaultTags;
        this.decoderContext = new DecoderContext(decoderService, this, secretConcealer, sentenceLexer);
        this.tagMergingStrategy = tagMergingStrategy;
    }

    List<ValidationError> getLoadErrors() {
        return loadErrors;
    }

    @Override
    public DecoderService getDecoderService() {
        return decoderService;
    }

    @Override
    public DecoderContext getDecoderContext() {
        return decoderContext;
    }

    public GestaltConfig getGestaltConfig() {
        return gestaltConfig;
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
            addConfigSourcePackageInternal(sourcePackage);
        }

        postProcessConfigs();
    }

    /**
     * Adds a ConfigSourcePackage to Gestalt, will load and merge the ConfigSourcePackage into Gestalt.
     * This does not raise any reload events, use with caution.
     *
     * @param sourcePackage the ConfigSourcePackage to add to Gestalt
     * @throws GestaltException any exceptions while loading the new ConfigSourcePackage
     */
    private void addConfigSourcePackageInternal(ConfigSourcePackage sourcePackage) throws GestaltException {
        if (sourcePackage == null) {
            throw new GestaltException("No ConfigSourcePackage provided, unable to load config");
        }

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

    /**
     * Adds a ConfigSourcePackage to Gestalt, will load and merge the ConfigSourcePackage into Gestalt.
     * This does not raise any reload events, use with caution.
     *
     * @param sourcePackage the ConfigSourcePackage to add to Gestalt
     * @throws GestaltException any exceptions while loading the new ConfigSourcePackage
     */
    @Override
    public void addConfigSourcePackage(ConfigSourcePackage sourcePackage) throws GestaltException {
        if (sourcePackage == null) {
            throw new GestaltException("No ConfigSourcePackage provided, unable to load config");
        }

        ObservationMarker reloadMarker = null;
        try {
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                reloadMarker = observationService.startObservation("addSource",
                    Tags.of(Tags.of("source", sourcePackage.getConfigSource().name()), sourcePackage.getTags()));
            }

            addConfigSourcePackageInternal(sourcePackage);

            postProcessConfigs();
            coreReloadListenersContainer.reload();
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.finalizeObservation(reloadMarker, Tags.of());
            }
        } catch (Exception ex) {
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.finalizeObservation(reloadMarker, Tags.of("exception", ex.getClass().getCanonicalName()));
            }
            throw ex;
        }
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

        ObservationMarker reloadMarker = null;
        try {
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                reloadMarker = observationService.startObservation("reload",
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
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.finalizeObservation(reloadMarker, Tags.of());
            }
        } catch (Exception ex) {
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.finalizeObservation(reloadMarker, Tags.of("exception", ex.getClass().getCanonicalName()));
            }
            throw ex;
        }
    }

    void postProcessConfigs() throws GestaltException {
        GResultOf<Boolean> results = configNodeService.processConfigNodes();

        if (checkErrorsShouldFail(results, gestaltConfig)) {
            throw new GestaltException("Failed post processing config nodes with errors ",
                results.getErrors());

        } else if (results.hasErrors() && logger.isLoggable(DEBUG)) {
            String errorMsg = ErrorsUtil.buildErrorMessage("Failed post processing config nodes with errors ",
                results.getErrors());
            logger.log(DEBUG, errorMsg);
        }

        loadErrors.addAll(results.getErrors());

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
                combinedPath.append(sentenceLexer.getNormalizedDeliminator());
            }
            combinedPath.append(configPrefix.prefix());
        }

        return combinedPath.toString();
    }

    @Override
    public <T> T getConfig(String path, Class<T> klass) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        return getConfigCommon(path, TypeCapture.of(klass), null).results();
    }

    @Override
    public <T> T getConfig(String path, Class<T> klass, Tags tags) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigCommon(path, TypeCapture.of(klass), tags).results();
    }

    @Override
    public <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        return getConfigCommon(path, klass, null).results();
    }

    @Override
    public <T> T getConfig(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigCommon(path, klass, tags).results();
    }

    @Override
    public <T> GResultOf<T> getConfigResult(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigCommon(path, klass, tags);
    }

    private <T> GResultOf<T> getConfigCommon(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {

        // fail on errors if this is not an optional type.
        // get the default value used if this is an optional type
        // most likely an optional.empty()
        Pair<Boolean, T> isOptionalAndDefault = ClassUtils.isOptionalAndDefault(klass.getRawType());

        Tags resolvedTags = tagMergingStrategy.mergeTags(tags, defaultTags);
        return getConfigurationInternal(path, !isOptionalAndDefault.getFirst(), isOptionalAndDefault.getSecond(), klass, resolvedTags);
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, Class<T> klass) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        return getConfigCommon(path, defaultVal, TypeCapture.of(klass), null).results();
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, Class<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigCommon(path, defaultVal, TypeCapture.of(klass), null).results();

    }

    @Override
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        return getConfigCommon(path, defaultVal, klass, null).results();
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigCommon(path, defaultVal, klass, tags).results();
    }

    @Override
    public <T> GResultOf<T> getConfigResult(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigCommon(path, defaultVal, klass, tags);
    }

    private <T> GResultOf<T> getConfigCommon(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        try {
            Tags resolvedTags = tagMergingStrategy.mergeTags(tags, defaultTags);
            return getConfigurationInternal(path, false, defaultVal, klass, resolvedTags);
        } catch (GestaltException e) {
            logger.log(WARNING, e.getMessage());
        }

        return GResultOf.result(defaultVal);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        return getConfigOptionalCommon(path, TypeCapture.of(klass), defaultTags).map(GResultOf::results);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigOptionalCommon(path, TypeCapture.of(klass), tags).map(GResultOf::results);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        return getConfigOptionalCommon(path, klass, defaultTags).map(GResultOf::results);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigOptionalCommon(path, klass, tags).map(GResultOf::results);
    }

    @Override
    public <T> Optional<GResultOf<T>> getConfigOptionalResult(String path, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigOptionalCommon(path, klass, tags);
    }

    private <T> Optional<GResultOf<T>> getConfigOptionalCommon(String path, TypeCapture<T> klass, Tags tags) {
        try {
            Tags resolvedTags = tagMergingStrategy.mergeTags(tags, defaultTags);
            var results = getConfigurationInternal(path, false, null, klass, resolvedTags);
            return Optional.ofNullable(results);
        } catch (GestaltException e) {
            logger.log(WARNING, e.getMessage());
        }

        return Optional.empty();
    }

    private <T> GResultOf<T> getConfigurationInternal(String path, boolean failOnErrors, T defaultVal, TypeCapture<T> klass, Tags tags)
        throws GestaltException {

        ObservationMarker getConfigMarker = null;
        boolean defaultReturned = false;
        Exception exceptionThrown = null;
        try {
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                getConfigMarker = observationService.startGetConfig(path, klass, tags, failOnErrors);
            }

            String combinedPath = buildPathWithConfigPrefix(klass, path);
            GResultOf<List<Token>> tokens = sentenceLexer.scan(combinedPath);
            if (tokens.hasErrors()) {
                throw new GestaltException("Unable to parse path: " + combinedPath, tokens.getErrors());
            } else {
                GResultOf<T> results = getAndDecodeConfig(combinedPath, tokens.results(), klass, tags);

                getConfigObservations(results, combinedPath, klass, tags, failOnErrors);

                var processedResults = resultsProcessorService.processResults(results, path, !failOnErrors, defaultVal, klass, tags);

                defaultReturned = processedResults.isDefault();
                return processedResults;
            }
        } catch (Exception ex) {
            exceptionThrown = ex;
            throw ex;
        } finally {
            finalizeObservations(getConfigMarker, defaultReturned, exceptionThrown);
        }
    }

    private void finalizeObservations(ObservationMarker getConfigMarker, boolean defaultReturned, Exception exceptionThrown) {
        if (gestaltConfig.isObservationsEnabled() && observationService != null && getConfigMarker != null) {
            Set<Tag> tagSet = new HashSet<>();
            if (defaultReturned) {
                tagSet.add(Tag.of("default", "true"));
            }

            if (exceptionThrown != null) {
                tagSet.add(Tag.of("exception", exceptionThrown.getClass().getCanonicalName()));
            }
            observationService.finalizeObservation(getConfigMarker, Tags.of(tagSet));
        }
    }

    private <T> GResultOf<T> getAndDecodeConfig(String path, List<Token> tokens, TypeCapture<T> klass, Tags tags) {
        GResultOf<ConfigNode> node = configNodeService.navigateToNode(path, tokens, tags);

        if (!node.hasErrors() || node.hasErrors(ValidationLevel.MISSING_VALUE)) {

            List<ValidationError> errors = new ArrayList<>();
            // apply any run time config node processors to the found node.
            GResultOf<ConfigNode> processedResult = configNodeProcessorService.runTimeProcessConfigNodes(path, node.results());
            errors.addAll(processedResult.getErrors());

            ConfigNode processedNode = processedResult.results();

            // if we have no errors or the error is from a missing value, lets try and decode the node.
            // for missing values some decoders like optional decoders will handle the errors.
            GResultOf<T> decodedResults = decoderService.decodeNode(path, tags, processedNode, klass, decoderContext);
            Map<String, List<MetaDataValue<?>>> metadata;
            if (node.results() != null) {
                // If this is a leaf node, get all metadata, otherwise rollup the metadata
                if (node.results() instanceof LeafNode) {
                    metadata = node.results().getMetadata();
                } else {
                    metadata = node.results().getRolledUpMetadata();
                }
            } else {
                metadata = Map.of();
            }

            // if we don't have a result and we received missing node errors.
            // return the errors from the call to navigate to node.
            // So we don't get too many "duplicate" errors
            // Otherwise if we have a result return both sets of errors.
            if (!decodedResults.hasResults() && node.hasErrors(ValidationLevel.MISSING_VALUE)) {
                errors.addAll(node.getErrors());
            } else {
                // if we have a result, only add non missing value errors.
                errors.addAll(node.getErrorsNotLevel(ValidationLevel.MISSING_VALUE));
                errors.addAll(decodedResults.getErrors());
            }

            return GResultOf.resultOf(decodedResults.results(), errors, metadata);
        } else {

            // if we have errors other than the missing values,
            // return the errors and do not attempt to decode.
            return GResultOf.errors(node.getErrors());
        }
    }

    private <T> void getConfigObservations(GResultOf<T> results, String path, TypeCapture<T> klass, Tags tags, boolean isOptional)
        throws GestaltException {
        if (gestaltConfig.isObservationsEnabled() && observationService != null) {

            // record all the details of the request.
            observationService.recordObservation(results, path, klass, tags, isOptional);

            int missing = results.getErrors(ValidationLevel.MISSING_VALUE).size();
            if (missing != 0) {
                observationService.recordObservation("get.config.missing", missing, Tags.of("optional", "false"));
            }

            int missingOptional = results.getErrors(ValidationLevel.MISSING_OPTIONAL_VALUE).size();
            if (missingOptional != 0) {
                observationService.recordObservation("get.config.missing", missingOptional, Tags.of("optional", "true"));
            }

            int errors = results.getErrors(ValidationLevel.ERROR).size();
            if (errors != 0) {
                observationService.recordObservation("get.config.error", errors, Tags.of());
            }

            int warnings = results.getErrors(ValidationLevel.WARN).size();
            if (warnings != 0) {
                observationService.recordObservation("get.config.warning", warnings, Tags.of());
            }
        }
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
