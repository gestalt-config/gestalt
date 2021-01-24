package org.config.gestalt;

import org.config.gestalt.decoder.DecoderService;
import org.config.gestalt.entity.ConfigNodeContainer;
import org.config.gestalt.entity.GestaltConfig;
import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.exceptions.ConfigurationException;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.loader.ConfigLoader;
import org.config.gestalt.loader.ConfigLoaderService;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.ConfigNodeService;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.reload.ConfigReloadListener;
import org.config.gestalt.reload.CoreReloadStrategy;
import org.config.gestalt.source.ConfigSource;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.ErrorsUtil;
import org.config.gestalt.utils.ValidateOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.config.gestalt.entity.ValidationLevel.ERROR;
import static org.config.gestalt.entity.ValidationLevel.WARN;

/**
 * Main API to get build and get configurations.
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

        if (results.hasErrors(WARN)) {
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
                    ", for class: " + klass.getName(), tokens.getErrors());
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
                    ", for class: " + klass.getName() + " returning default value", tokens.getErrors());
                logger.warn(errorMsg);

                return defaultVal;

            } else if (results.hasErrors()) {
                String errorMsg = ErrorsUtil.buildErrorMessage("Errors getting config path: " + path +
                    ", for class: " + klass.getName(), tokens.getErrors());
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

        return error.level() != ERROR;
    }

}
