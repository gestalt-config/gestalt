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
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.ConfigReloadListener;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.CoreReloadListenersContainer;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ErrorsUtil;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.*;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;
import static org.github.gestalt.config.entity.ValidationLevel.ERROR;
import static org.github.gestalt.config.entity.ValidationLevel.MISSING_VALUE;

/**
 * Central access point to Gestalt that has API's to build and get configurations.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class GestaltCore implements Gestalt, ConfigReloadListener {
    private static final System.Logger logger = System.getLogger(GestaltCore.class.getName());

    private final ConfigLoaderService configLoaderService;
    private final List<ConfigSource> sources;
    private final DecoderService decoderService;
    private final SentenceLexer sentenceLexer;
    private final GestaltConfig gestaltConfig;
    private final ConfigNodeService configNodeService;
    private final CoreReloadListenersContainer coreReloadListenersContainer;
    private final List<PostProcessor> postProcessors;

    private final List<ValidationError> loadErrors = new ArrayList<>();

    private final Tags defaultTags;

    private final DecoderContext decoderContext;

    /**
     * Constructor for Gestalt,you can call it manually but the best way to use this is though the GestaltBuilder.
     *
     * @param configLoaderService configLoaderService to hold all config loaders
     * @param sources sources we wish to load the configs from. We load the sources in the order they are provided.
     *     Overriding older values with new one where needed
     * @param decoderService decoderService to hold all decoders
     * @param sentenceLexer sentenceLexer to parse the configuration paths when doing searches.
     * @param gestaltConfig configuration for the Gestalt
     * @param configNodeService configNodeService core functionality to manage nodes
     * @param reloadStrategy reloadStrategy holds all reload listeners
     * @param postProcessor postProcessor list of post processors
     * @param defaultTags Default set of tags to apply to all calls to get a configuration where tags are not provided.
     */
    public GestaltCore(ConfigLoaderService configLoaderService, List<ConfigSource> sources, DecoderService decoderService,
                       SentenceLexer sentenceLexer, GestaltConfig gestaltConfig, ConfigNodeService configNodeService,
                       CoreReloadListenersContainer reloadStrategy, List<PostProcessor> postProcessor, Tags defaultTags) {
        this.configLoaderService = configLoaderService;
        this.sources = sources;
        this.decoderService = decoderService;
        this.sentenceLexer = sentenceLexer;
        this.gestaltConfig = gestaltConfig;
        this.configNodeService = configNodeService;
        this.coreReloadListenersContainer = reloadStrategy;
        this.postProcessors = postProcessor != null ? postProcessor : Collections.emptyList();
        this.defaultTags = defaultTags;
        this.decoderContext = new DecoderContext(decoderService, this);
    }

    List<ValidationError> getLoadErrors() {
        return loadErrors;
    }

    public DecoderService getDecoderService() {
        return decoderService;
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
        if (sources == null || sources.isEmpty()) {
            throw new GestaltException("No sources provided, unable to load any configs");
        }

        for (ConfigSource source : sources) {
            ConfigLoader configLoader = configLoaderService.getLoader(source.format());
            ValidateOf<List<ConfigNodeContainer>> newNode = configLoader.loadSource(source);

            validateLoadResultsForErrors(newNode, source);
            if (newNode.hasResults()) {
                for (ConfigNodeContainer node : newNode.results()) {
                    ValidateOf<ConfigNode> mergedNode = configNodeService.addNode(node);
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
     * @param reloadSource source to reload
     * @throws GestaltException any exception
     */
    @Override
    public void reload(ConfigSource reloadSource) throws GestaltException {
        if (reloadSource == null) {
            throw new GestaltException("No sources provided, unable to reload any configs");
        }

        if (sources == null || sources.isEmpty()) {
            throw new GestaltException("No sources provided, unable to reload any configs");
        }

        if (!sources.contains(reloadSource)) {
            throw new GestaltException("Can not reload a source that does not exist.");
        }

        ConfigLoader configLoader = configLoaderService.getLoader(reloadSource.format());
        ValidateOf<List<ConfigNodeContainer>> reloadNodes = configLoader.loadSource(reloadSource);
        validateLoadResultsForErrors(reloadNodes, reloadSource);

        if (!reloadNodes.hasResults()) {
            throw new GestaltException("no results found reloading source " + reloadSource.name());
        }

        for (ConfigNodeContainer reloadNode : reloadNodes.results()) {
            ValidateOf<ConfigNode> mergedNode = configNodeService.reloadNode(reloadNode);
            validateLoadResultsForErrors(mergedNode, reloadSource);

            if (!mergedNode.hasResults()) {
                throw new GestaltException("no results found merging source " + reloadSource.name());
            }

            postProcessConfigs();
        }

        coreReloadListenersContainer.reload();
    }

    void postProcessConfigs() throws GestaltException {
        ValidateOf<Boolean> results = configNodeService.postProcess(postProcessors);

        if (checkErrorsShouldFail(results)) {
            throw new GestaltException("Failed post processing config nodes with errors ",
                results.getErrors());

        } else if (results.hasErrors() && logger.isLoggable(DEBUG)) {
            String errorMsg = ErrorsUtil.buildErrorMessage("Failed post processing config nodes with errors ",
                results.getErrors());
            logger.log(DEBUG, errorMsg);
        }

        if (!results.hasResults()) {
            throw new GestaltException("no results found post processing the config nodes");
        }

        if (!results.results()) {
            throw new GestaltException("Post processing failed");
        }
    }

    private void validateLoadResultsForErrors(ValidateOf<?> results, ConfigSource source)
        throws GestaltConfigurationException {
        if ((gestaltConfig.isTreatWarningsAsErrors() && results.hasErrors()) || // NOPMD
            (results.hasErrors(ValidationLevel.ERROR) && source.failOnErrors())) {  // NOPMD
            throw new GestaltConfigurationException("Failed to load configs from source: " + source.name(), results.getErrors());
        }

        if (results.hasErrors(ValidationLevel.WARN) && logger.isLoggable(WARNING)) {
            String errorMsg = ErrorsUtil.buildErrorMessage(results.getErrors());
            logger.log(WARNING, errorMsg);
        }

        if (!results.hasResults()) {
            throw new GestaltConfigurationException("No results found for node");
        }
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
        Pair<Boolean, T> isOptionalAndDefault = isOptionalAndDefault(klass);

        return getConfigInternal(path, !isOptionalAndDefault.getFirst(), isOptionalAndDefault.getSecond(), klass, tags);
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
            return getConfigInternal(path, false, defaultVal, klass, tags);
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
            var results = getConfigInternal(path, false, null, klass, tags);
            return Optional.ofNullable(results);
        } catch (GestaltException e) {
            logger.log(WARNING, e.getMessage());
        }

        return Optional.empty();
    }

    private <T> T getConfigInternal(String path, boolean failOnErrors, T defaultVal, TypeCapture<T> klass, Tags tags)
        throws GestaltException {

        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        String combinedPath = buildPathWithConfigPrefix(klass, path);
        ValidateOf<List<Token>> tokens = sentenceLexer.scan(combinedPath);
        if (tokens.hasErrors()) {
            throw new GestaltException("Unable to parse path: " + combinedPath, tokens.getErrors());
        } else {
            ValidateOf<T> results = getConfigInternal(combinedPath, tokens.results(), klass, tags);

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

                    return defaultVal;
                }

            } else if (results.hasErrors() && logger.isLoggable(DEBUG)) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting config path: " + combinedPath +
                    ", for class: " + klass.getName(), results.getErrors());
                logger.log(DEBUG, errorMsg);
            }

            if (results.hasResults()) {
                return results.results();
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
            return defaultVal;
        }
    }

    private <T> ValidateOf<T> getConfigInternal(String path, List<Token> tokens, TypeCapture<T> klass, Tags tags) {
        ValidateOf<ConfigNode> node = configNodeService.navigateToNode(path, tokens, tags);

        if (!node.hasErrors() || node.hasErrors(MISSING_VALUE)) {
            // if we have no errors or the error is from a missing value, lets try and decode the node.
            // for missing values some decoders like optional decoders will handle the errors.
            ValidateOf<T> decodedResults = decoderService.decodeNode(path, tags, node.results(), klass, decoderContext);

            // if we don't have a result and we received missing node errors.
            // return the errors from the call to navigate to node.
            // So we don't get too many "duplicate" errors
            // Otherwise if we have a result return both sets of errors.
            List<ValidationError> errors = new ArrayList<>();
            if (!decodedResults.hasResults() && node.hasErrors(MISSING_VALUE)) {
                errors.addAll(node.getErrors());
            } else {
                errors.addAll(node.getErrors());
                errors.addAll(decodedResults.getErrors());
            }

            return ValidateOf.validateOf(decodedResults.results(), errors);
        } else {

            // if we have errors other than the missing values,
            // return the errors and do not attempt to decode.
            return ValidateOf.inValid(node.getErrors());
        }
    }

    private <T> boolean checkErrorsShouldFail(ValidateOf<T> results) {
        if (results.hasErrors()) {
            return !results.getErrors().stream().allMatch(this::ignoreError) || !results.hasResults();
        } else {
            return false;
        }
    }

    private boolean ignoreError(ValidationError error) {
        if (gestaltConfig.isTreatWarningsAsErrors()) {
            return false;
        } else if (error instanceof ValidationError.ArrayMissingIndex && !gestaltConfig.isTreatMissingArrayIndexAsError()) {
            return true;
        } else if (error.hasNoResults() && !gestaltConfig.isTreatMissingValuesAsErrors()) {
            return true;
        } else if (error instanceof ValidationError.NullValueDecodingObject && !gestaltConfig.isTreatNullValuesInClassAsErrors()) {
            return true;
        }

        return error.level() != ERROR && error.level() != MISSING_VALUE;
    }

    @SuppressWarnings("unchecked")
    private <T> Pair<Boolean, T> isOptionalAndDefault(TypeCapture<T> klass) {
        if (Optional.class.isAssignableFrom(klass.getRawType())) {
            return new Pair<>(true, (T) Optional.empty());
        } else if (OptionalInt.class.isAssignableFrom(klass.getRawType())) {
            return new Pair<>(true, (T) OptionalInt.empty());
        } else if (OptionalLong.class.isAssignableFrom(klass.getRawType())) {
            return new Pair<>(true, (T) OptionalLong.empty());
        } else if (OptionalDouble.class.isAssignableFrom(klass.getRawType())) {
            return new Pair<>(true, (T) OptionalDouble.empty());
        } else {
            return new Pair<>(false, null);
        }
    }
}
