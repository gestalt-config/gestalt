package org.config.gestalt.builder;

import org.config.gestalt.Gestalt;
import org.config.gestalt.GestaltCache;
import org.config.gestalt.GestaltCore;
import org.config.gestalt.decoder.*;
import org.config.gestalt.entity.GestaltConfig;
import org.config.gestalt.exceptions.ConfigurationException;
import org.config.gestalt.lexer.PathLexer;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.loader.*;
import org.config.gestalt.node.ConfigNodeManager;
import org.config.gestalt.node.ConfigNodeService;
import org.config.gestalt.reload.ConfigReloadStrategy;
import org.config.gestalt.reload.CoreReloadListener;
import org.config.gestalt.reload.CoreReloadStrategy;
import org.config.gestalt.source.ConfigSource;
import org.config.gestalt.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builder to setup and create the Gestalt config class.
 *
 * <p>The minimum requirements for building a config is to provide a source.
 * Gestalt gestalt = new GestaltBuilder()
 * .addSource(new FileConfigSource(defaultFile))
 * .build();
 *
 * <p>The builder will automatically add config loaders and decoders.
 * You can customise and replace functionality as needed using the appropriate builder methods.
 *
 * <p>If there are any decoders set, it will not add the default decoders. So you will need to add the defaults manually if needed.
 * If there are any config loaders set, it will not add the default config loaders. So you will need to add the defaults manually if needed.
 *
 * @author Colin Redmond
 */
public class GestaltBuilder {
    private static final Logger logger = LoggerFactory.getLogger(GestaltBuilder.class.getName());
    private final List<ConfigReloadStrategy> reloadStrategies = new ArrayList<>();
    private final List<CoreReloadListener> coreCoreReloadListeners = new ArrayList<>();
    private ConfigLoaderService configLoaderService = new ConfigLoaderRegistry();
    private DecoderService decoderService;
    private SentenceLexer sentenceLexer = new PathLexer();
    private GestaltConfig gestaltConfig = new GestaltConfig();
    private ConfigNodeService configNodeService = new ConfigNodeManager();
    private List<ConfigSource> sources = new ArrayList<>();
    private List<Decoder<?>> decoders = new ArrayList<>();
    private List<ConfigLoader> configLoaders = new ArrayList<>();

    private boolean useCacheDecorator = true;

    private Boolean treatWarningsAsErrors = null;
    private Boolean treatMissingArrayIndexAsError = null;
    private Boolean treatMissingValuesAsErrors = null;
    private Boolean envVarsTreatErrorsAsWarnings = null;

    private String dateDecoderFormat = null;
    private String localDateTimeFormat = null;
    private String localDateFormat = null;

    /**
     * Adds all default decoders to the builder. The default decoders include all decoders in this project.
     *
     * @return GestaltBuilder builder
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public GestaltBuilder addDefaultDecoders() {
        List<Decoder<?>> decoders = Arrays.asList(new ArrayDecoder(), new BigDecimalDecoder(), new BigIntegerDecoder(),
            new BooleanDecoder(), new ByteDecoder(), new CharDecoder(), new DateDecoder(gestaltConfig.getDateDecoderFormat()),
            new DoubleDecoder(), new DurationDecoder(), new EnumDecoder(), new FileDecoder(), new FloatDecoder(), new InstantDecoder(),
            new IntegerDecoder(), new ListDecoder(), new LocalDateDecoder(gestaltConfig.getLocalDateFormat()),
            new LocalDateTimeDecoder(gestaltConfig.getLocalDateTimeFormat()), new LongDecoder(), new MapDecoder(), new ObjectDecoder(),
            new PathDecoder(), new PatternDecoder(), new SetDecoder(), new ShortDecoder(), new StringDecoder(), new UUIDDecoder());
        this.decoders.addAll(decoders);
        return this;
    }

    /**
     * Add default config loaders for Map Config, Property and Environment Variables.
     *
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addDefaultConfigLoaders() {
        configLoaders.addAll(Arrays.asList(new MapConfigLoader(), new PropertyLoader(),
            new EnvironmentVarsLoader(gestaltConfig.isEnvVarsTreatErrorsAsWarnings())));
        return this;
    }

    /**
     * Sets the list of sources to load. Replaces any sources already set.
     *
     * @param sources list of sources to load.
     * @return GestaltBuilder builder
     * @throws ConfigurationException exception if there are no sources
     */
    public GestaltBuilder setSources(List<ConfigSource> sources) throws ConfigurationException {
        if (sources == null || sources.isEmpty()) {
            throw new ConfigurationException("No sources provided while setting sources");
        }
        this.sources = sources;

        return this;
    }

    /**
     * List of sources to add to the builder.
     *
     * @param sources list of sources to add.
     * @return GestaltBuilder builder
     * @throws ConfigurationException no sources provided
     */
    public GestaltBuilder addSources(List<ConfigSource> sources) throws ConfigurationException {
        if (sources == null || sources.isEmpty()) {
            throw new ConfigurationException("No sources provided while adding sources");
        }
        this.sources.addAll(sources);

        return this;
    }

    /**
     * Add a single source to the builder.
     *
     * @param source add a single sources
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addSource(ConfigSource source) {
        Objects.requireNonNull(source, "Source should not be null");
        this.sources.add(source);
        return this;
    }

    /**
     * Add a config reload strategy to the builder.
     *
     * @param configReloadStrategy add a config reload strategy.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addReloadStrategy(ConfigReloadStrategy configReloadStrategy) {
        Objects.requireNonNull(configReloadStrategy, "reloadStrategy should not be null");
        this.reloadStrategies.add(configReloadStrategy);
        return this;
    }

    /**
     * Add a list of config reload strategies to the builder.
     *
     * @param reloadStrategies list of config reload strategies.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addReloadStrategies(List<ConfigReloadStrategy> reloadStrategies) {
        Objects.requireNonNull(reloadStrategies, "reloadStrategies should not be null");
        this.reloadStrategies.addAll(reloadStrategies);
        return this;
    }

    /**
     * Add a Core reload listener to the builder.
     *
     * @param coreReloadListener a Core reload listener
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addCoreReloadListener(CoreReloadListener coreReloadListener) {
        Objects.requireNonNull(coreReloadListener, "coreReloadListener should not be null");
        this.coreCoreReloadListeners.add(coreReloadListener);
        return this;
    }

    /**
     * Add a list of Core reload listener.
     *
     * @param coreCoreReloadListeners a list of Core reload listener
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addCoreReloadListener(List<CoreReloadListener> coreCoreReloadListeners) {
        Objects.requireNonNull(reloadStrategies, "reloadStrategies should not be null");
        this.coreCoreReloadListeners.addAll(coreCoreReloadListeners);
        return this;
    }

    /**
     * Add a config loader service to the builder.
     *
     * @param configLoaderService a config loader service
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setConfigLoaderService(ConfigLoaderService configLoaderService) {
        Objects.requireNonNull(configLoaderService, "ConfigLoaderRegistry should not be null");
        this.configLoaderService = configLoaderService;
        return this;
    }

    /**
     * Sets a list of config loader to the builder. Replaces any currently set.
     * If there are any config loaders set, it will not add the defaults. So you will need to add the defaults manually if needed.
     *
     * @param configLoaders a list of config loader
     * @return GestaltBuilder builder
     * @throws ConfigurationException if there are no config loaders.
     */
    public GestaltBuilder setConfigLoaders(List<ConfigLoader> configLoaders) throws ConfigurationException {
        if (configLoaders == null || configLoaders.isEmpty()) {
            throw new ConfigurationException("No config loader provided while setting config loaders");
        }
        this.configLoaders = configLoaders;
        return this;
    }

    /**
     * Adds a list of config loader to the builder.
     * If there are any config loaders set, it will not add the defaults. So you will need to add the defaults manually if needed.
     *
     * @param configLoaders a list of config loader
     * @return GestaltBuilder builder
     * @throws ConfigurationException if the config loaders are empty
     */
    public GestaltBuilder addConfigLoaders(List<ConfigLoader> configLoaders) throws ConfigurationException {
        if (configLoaders == null || configLoaders.isEmpty()) {
            throw new ConfigurationException("No config loader provided while adding config loaders");
        }
        this.configLoaders.addAll(configLoaders);
        return this;
    }

    /**
     * Add a config loader.
     * If there are any config loaders set, it will not add the defaults. So you will need to add the defaults manually if needed.
     *
     * @param configLoader a config loader
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addConfigLoader(ConfigLoader configLoader) {
        Objects.requireNonNull(configLoader, "ConfigLoader should not be null");
        this.configLoaders.add(configLoader);
        return this;
    }

    /**
     * Set the sentence lexer that will be passed through to the DecoderRegistry.
     *
     * @param sentenceLexer for the DecoderRegistry
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setSentenceLexer(SentenceLexer sentenceLexer) {
        Objects.requireNonNull(sentenceLexer, "SentenceLexer should not be null");
        this.sentenceLexer = sentenceLexer;
        return this;
    }

    /**
     * Set the configuration for Gestalt. Will be overridden by any settings specified in the builder
     *
     * @param gestaltConfig configuration for the Gestalt
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setGestaltConfig(GestaltConfig gestaltConfig) {
        Objects.requireNonNull(gestaltConfig, "GestaltConfig should not be null");
        this.gestaltConfig = gestaltConfig;
        return this;
    }

    /**
     * Set the config node service if you want to provide your own. Otherwise a default is provided.
     *
     * @param configNodeService a config node service
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setConfigNodeService(ConfigNodeService configNodeService) {
        Objects.requireNonNull(configNodeService, "ConfigNodeManager should not be null");
        this.configNodeService = configNodeService;
        return this;
    }

    /**
     * Set the decoder service if you want to provide your own. Otherwise a default is provided.
     * If there are any decoders set, it will not add the default decoders. So you will need to add the defaults manually if needed.
     *
     * @param decoderService decoder service
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setDecoderService(DecoderService decoderService) {
        Objects.requireNonNull(decoderService, "DecoderService should not be null");
        this.decoderService = decoderService;
        decoderService.addDecoders(decoders);
        return this;
    }

    /**
     * Set a list of decoders, replaces the existing decoders.
     * If there are any decoders set, it will not add the default decoders. So you will need to add the defaults manually if needed.
     *
     * @param decoders list of decoders
     * @return GestaltBuilder builder
     * @throws ConfigurationException no decoders provided
     */
    public GestaltBuilder setDecoders(List<Decoder<?>> decoders) throws ConfigurationException {
        if (decoders == null || decoders.isEmpty()) {
            throw new ConfigurationException("No decoders provided while setting decoders");
        }
        this.decoders = decoders;
        return this;
    }

    /**
     * Add a list of decoders.
     * If there are any decoders set, it will not add the default decoders. So you will need to add the defaults manually if needed.
     *
     * @param decoders list of decoders
     * @return GestaltBuilder builder
     * @throws ConfigurationException no decoders provided
     */
    public GestaltBuilder addDecoders(List<Decoder<?>> decoders) throws ConfigurationException {
        if (decoders == null || decoders.isEmpty()) {
            throw new ConfigurationException("No decoders provided while adding decoders");
        }
        this.decoders.addAll(decoders);
        return this;
    }

    /**
     * Add a decoder.
     * If there are any decoders set, it will not add the default decoders. So you will need to add the defaults manually if needed.
     *
     * @param decoder add a decoder
     * @return GestaltBuilder builder
     */
    @SuppressWarnings("rawtypes")
    public GestaltBuilder addDecoder(Decoder decoder) {
        Objects.requireNonNull(decoder, "Decoder should not be null");
        this.decoders.add(decoder);
        return this;
    }

    /**
     * Treat warnings as errors.
     *
     * @param warningsAsErrors treat warnings as errors.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setTreatWarningsAsErrors(boolean warningsAsErrors) {
        treatWarningsAsErrors = warningsAsErrors;
        return this;
    }

    /**
     * Treat missing array indexes as errors.
     *
     * @param treatMissingArrayIndexAsError treat missing array indexes as errors.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setTreatMissingArrayIndexAsError(Boolean treatMissingArrayIndexAsError) {
        this.treatMissingArrayIndexAsError = treatMissingArrayIndexAsError;
        return this;
    }

    /**
     * treat missing object values as errors.
     *
     * @param treatMissingValuesAsErrors treat missing object values as errors
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setTreatMissingValuesAsErrors(Boolean treatMissingValuesAsErrors) {
        this.treatMissingValuesAsErrors = treatMissingValuesAsErrors;
        return this;
    }

    /**
     * treat Environment Variables errors as Warnings. Since we can not control Env Vars as closely,
     * we may need to ignore errors while building the node config tree.
     *
     * @param envVarsTreatErrorsAsWarnings treat Environment Variables errors as Warnings.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setEnvVarsTreatErrorsAsWarnings(boolean envVarsTreatErrorsAsWarnings) {
        this.envVarsTreatErrorsAsWarnings = envVarsTreatErrorsAsWarnings;
        return this;
    }

    /**
     * Add a cache layer to gestalt.
     *
     * @param useCacheDecorator use a cache decorator.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder useCacheDecorator(boolean useCacheDecorator) {
        this.useCacheDecorator = useCacheDecorator;
        return this;
    }

    /**
     * Set a date decoder format. Used to decode date times.
     *
     * @param dateDecoderFormat a date decoder format
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setDateDecoderFormat(String dateDecoderFormat) {
        this.dateDecoderFormat = dateDecoderFormat;
        return this;
    }

    /**
     * Set a local date time format. Used to decode local date times.
     *
     * @param localDateTimeFormat a date decoder format
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setLocalDateTimeFormat(String localDateTimeFormat) {
        this.localDateTimeFormat = localDateTimeFormat;
        return this;
    }

    /**
     * Set a local date format. Used to decode local date.
     *
     * @param localDateFormat a local date decoder format
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setLocalDateFormat(String localDateFormat) {
        this.localDateFormat = localDateFormat;
        return this;
    }

    /**
     * dedupe decoders and return the deduped list.
     *
     * @return deduped list of decoders.
     */
    protected List<Decoder<?>> dedupeDecoders() {
        Map<String, List<Decoder<?>>> decoderMap = decoders
            .stream()
            .collect(Collectors.groupingBy(Decoder::name))
            .entrySet()
            .stream()
            .filter(it -> it.getValue().size() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!decoderMap.isEmpty()) {
            String duplicates = String.join(", ", decoderMap.keySet());
            logger.warn("Found duplicate decoders {}", duplicates);
        }

        return decoders.stream().filter(CollectionUtils.distinctBy(Decoder::name)).collect(Collectors.toList());
    }

    /**
     * Dedupe the list of config loaders and return the deduped list.
     *
     * @return a list of deduped config loaders.
     */
    protected List<ConfigLoader> dedupeConfigLoaders() {
        Map<String, List<ConfigLoader>> configMap = configLoaders
            .stream()
            .collect(Collectors.groupingBy(ConfigLoader::name))
            .entrySet()
            .stream()
            .filter(it -> it.getValue().size() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!configMap.isEmpty()) {
            String duplicates = String.join(", ", configMap.keySet());
            logger.warn("Found duplicate config loaders {}", duplicates);
        }

        return configLoaders.stream().filter(CollectionUtils.distinctBy(ConfigLoader::name)).collect(Collectors.toList());
    }

    /**
     * Build Gestalt.
     *
     * @return Gestalt
     * @throws ConfigurationException multiple validations can throw exceptions
     */
    public Gestalt build() throws ConfigurationException {
        if (sources.isEmpty()) {
            throw new ConfigurationException("No sources provided");
        }

        gestaltConfig = rebuildConfig();

        // setup the decoders, if there are none, add the default ones.
        if (decoders.isEmpty()) {
            logger.debug("No decoders provided, using defaults");
            addDefaultDecoders();
        }

        // if the decoderService does not exist, create it.
        // Otherwise get all the decoders from the decoderService, combine them with the ones in the builder,
        // and update the decoderService
        if (decoderService == null) {
            decoderService = new DecoderRegistry(decoders, configNodeService, sentenceLexer);
        } else {
            decoders.addAll(decoderService.getDecoders());
            List<Decoder<?>> dedupedDecoders = dedupeDecoders();
            decoderService.setDecoders(dedupedDecoders);
        }

        // Setup the config loaders.
        if (configLoaders.isEmpty()) {
            logger.debug("No decoders provided, using defaults");
            addDefaultConfigLoaders();
        }

        // get all the config loaders from the configLoaderRegistry, combine them with the ones in the builder,
        // and update the configLoaderRegistry
        configLoaders.addAll(configLoaderService.getConfigLoaders());
        List<ConfigLoader> dedupedConfigs = dedupeConfigLoaders();
        configLoaderService.setLoaders(dedupedConfigs);

        // create a new GestaltCoreReloadStrategy to listen for Gestalt Core Reloads.
        CoreReloadStrategy coreReloadStrategy = new CoreReloadStrategy();
        final GestaltCore gestaltCore = new GestaltCore(configLoaderService, sources, decoderService, sentenceLexer, gestaltConfig,
            configNodeService, coreReloadStrategy);

        // register gestaltCore with all the source reload strategies.
        reloadStrategies.forEach(it -> it.registerListener(gestaltCore));
        // Add all listeners for the core update.
        coreCoreReloadListeners.forEach(coreReloadStrategy::registerListener);

        if (useCacheDecorator) {
            GestaltCache gestaltCache = new GestaltCache(gestaltCore);

            // Register the cache with the gestaltCoreReloadStrategy so when the core reloads
            // we can clear the cache.
            coreReloadStrategy.registerListener(gestaltCache);
            return gestaltCache;
        } else {
            return gestaltCore;
        }

    }

    private GestaltConfig rebuildConfig() {
        GestaltConfig newConfig = new GestaltConfig();

        if (treatWarningsAsErrors != null) {
            newConfig.setTreatWarningsAsErrors(treatWarningsAsErrors);
        } else {
            newConfig.setTreatWarningsAsErrors(gestaltConfig.isTreatWarningsAsErrors());
        }

        if (treatMissingArrayIndexAsError != null) {
            newConfig.setTreatMissingArrayIndexAsError(treatMissingArrayIndexAsError);
        } else {
            newConfig.setTreatMissingArrayIndexAsError(gestaltConfig.isTreatMissingArrayIndexAsError());
        }

        if (treatMissingValuesAsErrors != null) {
            newConfig.setTreatMissingValuesAsErrors(treatMissingValuesAsErrors);
        } else {
            newConfig.setTreatMissingValuesAsErrors(gestaltConfig.isTreatMissingValuesAsErrors());
        }

        if (envVarsTreatErrorsAsWarnings != null) {
            newConfig.setEnvVarsTreatErrorsAsWarnings(envVarsTreatErrorsAsWarnings);
        } else {
            newConfig.setEnvVarsTreatErrorsAsWarnings(gestaltConfig.isEnvVarsTreatErrorsAsWarnings());
        }

        if (dateDecoderFormat != null) {
            newConfig.setDateDecoderFormat(dateDecoderFormat);
        } else {
            newConfig.setDateDecoderFormat(gestaltConfig.getDateDecoderFormat());
        }

        if (localDateTimeFormat != null) {
            newConfig.setLocalDateTimeFormat(localDateTimeFormat);
        } else {
            newConfig.setLocalDateTimeFormat(gestaltConfig.getLocalDateTimeFormat());
        }

        if (localDateFormat != null) {
            newConfig.setLocalDateFormat(localDateFormat);
        } else {
            newConfig.setLocalDateFormat(gestaltConfig.getLocalDateFormat());
        }

        return newConfig;
    }
}
