package org.github.gestalt.config;

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
import org.github.gestalt.config.reload.CoreReloadStrategy;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ErrorsUtil;
import org.github.gestalt.config.utils.ValidateOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Central access point to Gestalt that has API's to build and get configurations.
 *
 * @author Colin Redmond
 */
public class GestaltCore implements Gestalt, ConfigReloadListener {
    private static final Logger logger = LoggerFactory.getLogger(GestaltCore.class.getName());

    private final ConfigLoaderService configLoaderService;
    private final List<ConfigSource> sources;
    private final DecoderService decoderService;
    private final SentenceLexer sentenceLexer;
    private final GestaltConfig gestaltConfig;
    private final ConfigNodeService configNodeService;
    private final CoreReloadStrategy coreReloadStrategy;
    private final List<PostProcessor> postProcessors;

    private final List<ValidationError> loadErrors = new ArrayList<>();

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
     */
    public GestaltCore(ConfigLoaderService configLoaderService, List<ConfigSource> sources, DecoderService decoderService,
                       SentenceLexer sentenceLexer, GestaltConfig gestaltConfig, ConfigNodeService configNodeService,
                       CoreReloadStrategy reloadStrategy, List<PostProcessor> postProcessor) {
        this.configLoaderService = configLoaderService;
        this.sources = sources;
        this.decoderService = decoderService;
        this.sentenceLexer = sentenceLexer;
        this.gestaltConfig = gestaltConfig;
        this.configNodeService = configNodeService;
        this.coreReloadStrategy = reloadStrategy;
        this.postProcessors = postProcessor != null ? postProcessor : Collections.emptyList();
    }

    List<ValidationError> getLoadErrors() {
        return loadErrors;
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
                logger.warn("Failed to load node: {} did not have any results", source.name());
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

        coreReloadStrategy.reload();
    }

    void postProcessConfigs() throws GestaltException {
        ValidateOf<Boolean> results = configNodeService.postProcess(postProcessors);

        if (checkErrorsShouldFail(results)) {
            throw new GestaltException("Failed post processing config nodes with errors ",
                results.getErrors());

        } else if (results.hasErrors() && logger.isDebugEnabled()) {
            String errorMsg = ErrorsUtil.buildErrorMessage("Failed post processing config nodes with errors ",
                results.getErrors());
            logger.debug(errorMsg);
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

        if (results.hasErrors(ValidationLevel.WARN) && logger.isWarnEnabled()) {
            String errorMsg = ErrorsUtil.buildErrorMessage(results.getErrors());
            logger.warn(errorMsg);
        }

        if (!results.hasResults()) {
            throw new GestaltConfigurationException("No results found for node");
        }
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

    public <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException {
        return getConfig(path, klass, Tags.of());
    }

    @Override
    public <T> T getConfig(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        ValidateOf<List<Token>> tokens = sentenceLexer.scan(path);
        if (tokens.hasErrors()) {
            throw new GestaltException("Unable to parse path: " + path, tokens.getErrors());
        } else {
            ValidateOf<T> results = getConfigInternal(path, tokens.results(), klass, tags);

            if (checkErrorsShouldFail(results)) {
                throw new GestaltException("Failed getting config path: " + path + ", for class: " + klass.getName(),
                    results.getErrors());

            } else if (results.hasErrors() && logger.isDebugEnabled()) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting config path: " + path +
                    ", for class: " + klass.getName(), results.getErrors());
                logger.debug(errorMsg);
            }

            if (results.hasResults()) {
                return results.results();
            }
        }
        throw new GestaltException("No results for config path: " + path + ", and class: " + klass.getName());
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, Class<T> klass) {
        Objects.requireNonNull(klass);

        return getConfig(path, defaultVal, TypeCapture.of(klass));
    }

    public <T> T getConfig(String path, T defaultVal, Class<T> klass, Tags tags) {
        Objects.requireNonNull(klass);

        return getConfig(path, defaultVal, TypeCapture.of(klass), tags);
    }

    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass) {
        return getConfig(path, defaultVal, klass, Tags.of());
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(defaultVal);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        ValidateOf<List<Token>> tokens = sentenceLexer.scan(path);
        if (tokens.hasErrors()) {
            if (logger.isWarnEnabled()) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Unable to parse path: " + path, tokens.getErrors());
                logger.warn(errorMsg);
            }

            return defaultVal;
        } else {
            ValidateOf<T> results = getConfigInternal(path, tokens.results(), klass, tags);

            if (checkErrorsShouldFail(results)) {
                if (logger.isWarnEnabled()) {
                    String errorMsg = ErrorsUtil.buildErrorMessage("Failed getting config with default path: " + path +
                        ", for class: " + klass.getName() + " returning default value", results.getErrors());
                    logger.warn(errorMsg);
                }

                return defaultVal;

            } else if (results.hasErrors() && logger.isInfoEnabled()) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting config with default path: " + path +
                    ", for class: " + klass.getName(), results.getErrors());
                logger.info(errorMsg);
            }

            if (results.hasResults()) {
                return results.results();
            }
        }

        if (logger.isInfoEnabled()) {
            String errorMsg = ErrorsUtil.buildErrorMessage("No results for config with default path: " + path +
                ", and class: " + klass.getName(), tokens.getErrors());
            logger.info(errorMsg);
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

    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass) {
        Objects.requireNonNull(klass);

        return getConfigOptional(path, klass, Tags.of());
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        ValidateOf<List<Token>> tokens = sentenceLexer.scan(path);
        if (tokens.hasErrors()) {
            if (logger.isWarnEnabled()) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Unable to parse path: " + path + " returning empty optional",
                    tokens.getErrors());
                logger.warn(errorMsg);
            }

            return Optional.empty();
        } else {
            ValidateOf<T> results = getConfigInternal(path, tokens.results(), klass, tags);

            if (checkErrorsShouldFail(results)) {
                if (logger.isWarnEnabled()) {
                    String errorMsg = ErrorsUtil.buildErrorMessage("Failed getting Optional config path: " + path +
                        ", for class: " + klass.getName() + " returning empty Optional", results.getErrors());
                    logger.warn(errorMsg);
                }

                return Optional.empty();

            } else if (results.hasErrors() && logger.isInfoEnabled()) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting Optional config path: " + path +
                    ", for class: " + klass.getName(), results.getErrors());
                logger.info(errorMsg);
            }

            if (results.hasResults()) {
                return Optional.of(results.results());
            }
        }

        if (logger.isInfoEnabled()) {
            String errorMsg = ErrorsUtil.buildErrorMessage("No results for Optional config path: " + path + ", and class: " +
                klass.getName() + " returning empty Optional", tokens.getErrors());
            logger.info(errorMsg);
        }

        return Optional.empty();
    }

    private <T> ValidateOf<T> getConfigInternal(String path, List<Token> tokens, TypeCapture<T> klass, Tags tags) {
        ValidateOf<ConfigNode> node = configNodeService.navigateToNode(path, tokens, tags);
        if (node.hasErrors()) {
            return ValidateOf.inValid(node.getErrors());
        } else if (node.hasResults()) {
            ConfigNode currentNode = node.results();

            return decoderService.decodeNode(path, currentNode, klass);
        } else {
            return ValidateOf.inValid(new ValidationError.NoResultsFoundForNode(path, klass.getName(), "get config"));
        }
    }

    private <T> boolean checkErrorsShouldFail(ValidateOf<T> results) {
        if (results.hasErrors()) {
            return results.getErrors().stream().noneMatch(this::ignoreError) || !results.hasResults();
        } else {
            return false;
        }
    }

    private boolean ignoreError(ValidationError error) {
        if (gestaltConfig.isTreatWarningsAsErrors()) {
            return false;
        } else if (error instanceof ValidationError.ArrayMissingIndex && !gestaltConfig.isTreatMissingArrayIndexAsError()) {
            return true;
        } else if ((error instanceof ValidationError.DecodingLeafMissingValue ||
            error instanceof ValidationError.NoResultsFoundForPath ||
            error instanceof ValidationError.NoResultsFoundForNode) &&
            !gestaltConfig.isTreatMissingValuesAsErrors()) {
            return true;
        }

        return error.level() != ValidationLevel.ERROR;
    }

}
