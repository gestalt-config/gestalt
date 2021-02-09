package org.github.gestalt.config;

import org.github.gestalt.config.decoder.DecoderService;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.ConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.ConfigReloadListener;
import org.github.gestalt.config.reload.CoreReloadStrategy;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ErrorsUtil;
import org.github.gestalt.config.utils.ValidateOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private final List<ValidationError> loadErrors = new ArrayList<>();

    public GestaltCore(ConfigLoaderService configLoaderService, List<ConfigSource> sources, DecoderService decoderService,
                       SentenceLexer sentenceLexer, GestaltConfig gestaltConfig, ConfigNodeService configNodeService,
                       CoreReloadStrategy reloadStrategy) {
        this.configLoaderService = configLoaderService;
        this.sources = sources;
        this.decoderService = decoderService;
        this.sentenceLexer = sentenceLexer;
        this.gestaltConfig = gestaltConfig;
        this.configNodeService = configNodeService;
        this.coreReloadStrategy = reloadStrategy;
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
            ValidateOf<ConfigNode> newNode = configLoader.loadSource(source);

            validateLoadResultsForErrors(newNode);
            if (newNode.hasResults()) {
                ValidateOf<ConfigNode> mergedNode = configNodeService.addNode(new ConfigNodeContainer(newNode.results(), source.id()));
                validateLoadResultsForErrors(mergedNode);
                loadErrors.addAll(mergedNode.getErrors());
            } else {
                logger.warn("Failed to load node: {} did not have any results", source.name());
            }
        }
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
        ValidateOf<ConfigNode> reloadNode = configLoader.loadSource(reloadSource);
        validateLoadResultsForErrors(reloadNode);

        if (!reloadNode.hasResults()) {
            throw new GestaltException("no results found reloading source " + reloadSource.name());
        }

        ValidateOf<ConfigNode> mergedNode = configNodeService.reloadNode(new ConfigNodeContainer(reloadNode.results(), reloadSource.id()));
        validateLoadResultsForErrors(mergedNode);

        if (!mergedNode.hasResults()) {
            throw new GestaltException("no results found merging source " + reloadSource.name());
        }

        coreReloadStrategy.reload();
    }

    private void validateLoadResultsForErrors(ValidateOf<ConfigNode> results) throws ConfigurationException {
        if ((gestaltConfig.isTreatWarningsAsErrors() && results.hasErrors()) || results.hasErrors(ValidationLevel.ERROR)) {  // NOPMD
            throw new ConfigurationException("Failed to load configs", results.getErrors());
        }

        if (results.hasErrors(ValidationLevel.WARN)) {
            String errorMsg = ErrorsUtil.buildErrorMessage(results.getErrors());
            logger.warn(errorMsg);
        }
    }

    @Override
    public <T> T getConfig(String path, Class<T> klass) throws GestaltException {
        return getConfig(path, TypeCapture.of(klass));
    }

    @Override
    public <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException {
        ValidateOf<List<Token>> tokens = sentenceLexer.scan(path);
        if (tokens.hasErrors()) {
            throw new GestaltException("Unable to parse path: " + path, tokens.getErrors());
        } else {
            ValidateOf<T> results = getConfigInternal(path, tokens.results(), klass);

            if (checkErrorsShouldFail(results)) {
                throw new GestaltException("Failed getting config path: " + path + ", for class: " + klass.getName(),
                    results.getErrors());

            } else if (results.hasErrors()) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting config path: " + path +
                    ", for class: " + klass.getName(), results.getErrors());
                logger.warn(errorMsg);
            }

            if (results.hasResults()) {
                return results.results();
            }
        }
        throw new GestaltException("No results for config path: " + path + ", and class: " + klass.getName());
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, Class<T> klass) {
        return getConfig(path, defaultVal, TypeCapture.of(klass));
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass) {
        ValidateOf<List<Token>> tokens = sentenceLexer.scan(path);
        if (tokens.hasErrors()) {
            String errorMsg = ErrorsUtil.buildErrorMessage("Unable to parse path: " + path, tokens.getErrors());
            logger.warn(errorMsg);

            return defaultVal;
        } else {
            ValidateOf<T> results = getConfigInternal(path, tokens.results(), klass);

            if (checkErrorsShouldFail(results)) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Failed getting config path: " + path +
                    ", for class: " + klass.getName() + " returning default value", results.getErrors());
                logger.warn(errorMsg);

                return defaultVal;

            } else if (results.hasErrors()) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting config path: " + path +
                    ", for class: " + klass.getName(), results.getErrors());
                logger.warn(errorMsg);
            }

            if (results.hasResults()) {
                return results.results();
            }
        }

        String errorMsg = ErrorsUtil.buildErrorMessage("No results for config path: " + path + ", and class: " + klass.getName(),
            tokens.getErrors());
        logger.warn(errorMsg);

        return defaultVal;
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass) {
        return getConfigOptional(path, TypeCapture.of(klass));
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass) {
        ValidateOf<List<Token>> tokens = sentenceLexer.scan(path);
        if (tokens.hasErrors()) {
            String errorMsg = ErrorsUtil.buildErrorMessage("Unable to parse path: " + path + " returning empty optional",
                tokens.getErrors());
            logger.warn(errorMsg);

            return Optional.empty();
        } else {
            ValidateOf<T> results = getConfigInternal(path, tokens.results(), klass);

            if (checkErrorsShouldFail(results)) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Failed getting config path: " + path +
                    ", for class: " + klass.getName() + " returning empty Optional", results.getErrors());
                logger.warn(errorMsg);

                return Optional.empty();

            } else if (results.hasErrors()) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting config path: " + path +
                    ", for class: " + klass.getName(), results.getErrors());
                logger.warn(errorMsg);
            }

            if (results.hasResults()) {
                return Optional.of(results.results());
            }
        }

        String errorMsg = ErrorsUtil.buildErrorMessage("No results for config path: " + path + ", and class: " +
                klass.getName() + " returning empty Optional",
            tokens.getErrors());
        logger.warn(errorMsg);

        return Optional.empty();
    }

    private <T> ValidateOf<T> getConfigInternal(String path, List<Token> tokens, TypeCapture<T> klass) {
        ValidateOf<ConfigNode> node = configNodeService.navigateToNode(path, tokens);
        if (node.hasErrors()) {
            return ValidateOf.inValid(node.getErrors());
        } else if (node.hasResults()) {
            ConfigNode currentNode = node.results();

            return decoderService.decodeNode(path, currentNode, klass);
        } else {
            return ValidateOf.inValid(new ValidationError.NoResultsFoundForNode(path, klass.getName()));
        }
    }

    private <T> boolean checkErrorsShouldFail(ValidateOf<T> results) {
        if (results.hasErrors()) {
            return results.getErrors().stream().noneMatch(this::ignoreError);
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
            error instanceof ValidationError.NoResultsFoundForNode ||
            error instanceof ValidationError.UnableToFindObjectNodeForPath) &&
            !gestaltConfig.isTreatMissingValuesAsErrors()) {
            return true;
        }

        return error.level() != ValidationLevel.ERROR;
    }

}
