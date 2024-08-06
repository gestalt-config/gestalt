package org.github.gestalt.config.builder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.GestaltCache;
import org.github.gestalt.config.GestaltCore;
import org.github.gestalt.config.decoder.Decoder;
import org.github.gestalt.config.decoder.DecoderRegistry;
import org.github.gestalt.config.decoder.DecoderService;
import org.github.gestalt.config.decoder.ProxyDecoderMode;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderRegistry;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.observations.ObservationManager;
import org.github.gestalt.config.observations.ObservationRecorder;
import org.github.gestalt.config.observations.ObservationService;
import org.github.gestalt.config.path.mapper.PathMapper;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorManager;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorService;
import org.github.gestalt.config.processor.result.*;
import org.github.gestalt.config.processor.result.validation.ConfigValidator;
import org.github.gestalt.config.processor.result.validation.ValidationResultProcessor;
import org.github.gestalt.config.reload.ConfigReloadStrategy;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.CoreReloadListenersContainer;
import org.github.gestalt.config.secret.rules.*;
import org.github.gestalt.config.security.encrypted.EncryptedSecretModule;
import org.github.gestalt.config.security.temporary.TemporarySecretModule;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.node.factory.ConfigNodeFactory;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryConfig;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryManager;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryService;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.CollectionUtils;
import org.github.gestalt.config.utils.Pair;

import java.lang.System.Logger.Level;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Builder to setup and create the Gestalt config class.
 *
 * <p>The minimum requirements for building a config is to provide a source.
 * Gestalt gestalt = new GestaltBuilder()
 * .addSource(new FileConfigSource(defaultFile))
 * .build();
 *
 * <p>The builder will automatically add the default config loaders and decoders.
 * You can customise and replace functionality as needed using the appropriate builder methods.
 *
 * <p>If there are any decoders set, it will not add the default decoders. So you will need to add the defaults manually if needed.
 * If there are any config loaders set, it will not add the default config loaders. So you will need to add the defaults manually if needed.
 *
 * <p>The builder can be used to customize and replace any of the functionality of Gestalt.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class GestaltBuilder {
    private static final System.Logger logger = System.getLogger(GestaltBuilder.class.getName());
    private final List<ConfigReloadStrategy> reloadStrategies = new ArrayList<>();
    private final List<CoreReloadListener> coreCoreReloadListeners = new ArrayList<>();
    @SuppressWarnings("rawtypes")
    private final Map<Class, GestaltModuleConfig> modules = new HashMap<>();
    private final List<ResultProcessor> coreResultProcessors = List.of(new ErrorResultProcessor(), new DefaultResultProcessor());
    private ConfigLoaderService configLoaderService = new ConfigLoaderRegistry();
    private DecoderService decoderService;
    private SentenceLexer sentenceLexer;
    private GestaltConfig gestaltConfig = new GestaltConfig();
    private ObservationService observationService;
    private ResultsProcessorService resultsProcessorService;
    private ConfigNodeProcessorService configNodeProcessorService;
    private ConfigNodeService configNodeService;
    private ConfigNodeFactoryService configNodeFactoryService;
    private List<ConfigSourcePackage> configSourcePackages = new ArrayList<>();
    private List<Decoder<?>> decoders = new ArrayList<>();
    private List<ConfigLoader> configLoaders = new ArrayList<>();
    private List<ConfigNodeProcessor> configNodeProcessors = new ArrayList<>();
    private List<PathMapper> pathMappers = new ArrayList<>();
    private List<ObservationRecorder> observationRecorders = new ArrayList<>();
    private List<ConfigValidator> configValidators = new ArrayList<>();
    private List<ResultProcessor> resultProcessors = new ArrayList<>();
    private List<ConfigNodeFactory> configSourceFactories = new ArrayList<>();
    private ConfigNodeTagResolutionStrategy configNodeTagResolutionStrategy;
    private TagMergingStrategy tagMergingStrategy;
    private boolean useCacheDecorator = true;
    private Set<String> securityMaskingRules = new HashSet<>(
        List.of("bearer", "cookie", "credential", "id",
            "key", "keystore", "passphrase", "password",
            "private", "salt", "secret", "secure",
            "ssl", "token", "truststore"));
    private String secretMask = "*****";
    private List<Pair<SecretChecker, Integer>> secretAccessCounts = new ArrayList<>();
    private SecretChecker encryptedSecrets = new RegexSecretChecker(new HashSet<>());
    private SecretConcealer secretConcealer;
    private SecretObfuscator secretObfuscator = (it) -> secretMask;

    private Boolean treatWarningsAsErrors = null;
    private Boolean treatMissingArrayIndexAsError = null;
    private Boolean treatMissingValuesAsErrors = null;
    private Boolean treatMissingDiscretionaryValuesAsErrors = null;
    // If we should enable observations
    private Boolean observationsEnabled = null;
    // If we should enable Validation
    private boolean validationEnabled = false;

    // if we should add the core result processors.
    // The core result processors are central to Gestalt and should be added.
    // The only reason to not add them is to fully customize gestalt.
    private boolean addCoreResultProcessors = true;

    private Level logLevelForMissingValuesWhenDefaultOrOptional = null;

    private DateTimeFormatter dateDecoderFormat = null;
    private DateTimeFormatter localDateTimeFormat = null;
    private DateTimeFormatter localDateFormat = null;

    // Token that represents the opening of a string substitution.
    private String substitutionOpeningToken = null;

    // Token that represents the closing of a string substitution.
    private String substitutionClosingToken = null;

    // the maximum nested substitution depth.
    private Integer maxSubstitutionNestedDepth = null;

    // the regex used to parse string substitutions.
    // Must have a named capture group transform, key, and default, where the key is required and the transform and default are optional.
    private String substitutionRegex = null;

    // The keyword that is used to determine if a node is an include from a source
    private String nodeIncludeKeyword = null;

    // Defines how the proxy decoder works. See the enum for details.
    private ProxyDecoderMode proxyDecoderMode = null;


    // Default set of tags to apply to all calls to get a configuration where tags are not provided.
    private Tags defaultTags = Tags.of();


    /**
     * Adds all default decoders to the builder. Uses the ServiceLoader to find all registered Decoders and adds them
     *
     * @return GestaltBuilder builder
     */
    @SuppressWarnings({"rawtypes"})
    public GestaltBuilder addDefaultDecoders() {
        List<Decoder<?>> decodersSet = new ArrayList<>();
        ServiceLoader<Decoder> loader = ServiceLoader.load(Decoder.class);
        loader.forEach(decodersSet::add);
        this.decoders.addAll(decodersSet);
        return this;
    }

    /**
     * Add default config loaders to the builder. Uses the ServiceLoader to find all registered Config Loaders and adds them
     *
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addDefaultConfigLoaders() {
        List<ConfigLoader> configLoaderSet = new ArrayList<>();
        ServiceLoader<ConfigLoader> loader = ServiceLoader.load(ConfigLoader.class);
        loader.forEach(configLoaderSet::add);
        configLoaders.addAll(configLoaderSet);
        return this;
    }

    /**
     * Add default post processors to the builder. Uses the ServiceLoader to find all registered post processors and adds them
     *
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addDefaultPostProcessors() {
        List<ConfigNodeProcessor> configNodeProcessorsSet = new ArrayList<>();
        ServiceLoader<ConfigNodeProcessor> loader = ServiceLoader.load(ConfigNodeProcessor.class);
        loader.forEach(configNodeProcessorsSet::add);
        configNodeProcessors.addAll(configNodeProcessorsSet);
        return this;
    }

    /**
     * Add default observation recorders to the builder. Uses the ServiceLoader to find all registered ObservationsRecorder and adds them
     *
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addDefaultObservationsRecorder() {
        List<ObservationRecorder> observationRecordersSet = new ArrayList<>();
        ServiceLoader<ObservationRecorder> loader = ServiceLoader.load(ObservationRecorder.class);
        loader.forEach(observationRecordersSet::add);

        observationRecorders.addAll(observationRecordersSet);
        return this;
    }

    /**
     * Add default Validator to the builder. Uses the ServiceLoader to find all registered Validator and adds them
     *
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addDefaultValidators() {
        List<ConfigValidator> validatorsSet = new ArrayList<>();
        ServiceLoader<ConfigValidator> loader = ServiceLoader.load(ConfigValidator.class);
        loader.forEach(validatorsSet::add);

        configValidators.addAll(validatorsSet);
        return this;
    }


    /**
     * Add default Result Processors to the builder. Uses the ServiceLoader to find all registered ResultProcessor and adds them
     *
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addDefaultResultProcessor() {
        List<ResultProcessor> resultProcessorSet = new ArrayList<>();
        ServiceLoader<ResultProcessor> loader = ServiceLoader.load(ResultProcessor.class);
        loader.forEach(resultProcessorSet::add);

        resultProcessors.addAll(resultProcessorSet);
        return this;
    }

    /**
     * Add default post processors to the builder. Uses the ServiceLoader to find all registered post processors and adds them
     *
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addDefaultPathMappers() {
        List<PathMapper> pathMappersSet = new ArrayList<>();
        ServiceLoader<PathMapper> loader = ServiceLoader.load(PathMapper.class);
        loader.forEach(pathMappersSet::add);
        pathMappers.addAll(pathMappersSet);
        return this;
    }

    /**
     * Add default Config Source Factories to the builder. Uses the ServiceLoader to find all registered ConfigSourceFactory and adds them
     *
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addDefaultConfigSourceFactory() {
        List<ConfigNodeFactory> configSourceFactorySet = new ArrayList<>();
        ServiceLoader<ConfigNodeFactory> loader = ServiceLoader.load(ConfigNodeFactory.class);
        loader.forEach(configSourceFactorySet::add);

        configSourceFactories.addAll(configSourceFactorySet);
        return this;
    }

    /**
     * Add a single source to the builder.
     *
     * @param source add a single sources
     * @return GestaltBuilder builder
     * @deprecated prefer the use of {@link GestaltBuilder#addSource(ConfigSourcePackage) addSource} with a source builder
     */
    @Deprecated(since = "0.23.4")
    public GestaltBuilder addSource(ConfigSource source) {
        Objects.requireNonNull(source, "Source should not be null");
        this.configSourcePackages.add(new ConfigSourcePackage(source, List.of(), source.getTags()));
        return this;
    }

    /**
     * Add a single ConfigSourcePackage built with a builder, to gestalt the builder.
     *
     * @param configSourcePackage add a single Config source Package
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addSource(ConfigSourcePackage configSourcePackage) {
        Objects.requireNonNull(configSourcePackage, "ConfigSourcePackage should not be null");
        this.configSourcePackages.add(configSourcePackage);
        return this;
    }

    /**
     * Set a List of ConfigSourcePackage built with a config source builder, to gestalt the builder.
     *
     * @param configSourcePackage set a list of ConfigSourcePackage
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setSources(List<ConfigSourcePackage> configSourcePackage) {
        Objects.requireNonNull(configSourcePackage, "ConfigSourcePackage should not be null");
        this.configSourcePackages = new ArrayList<>(configSourcePackage);
        return this;
    }

    /**
     * Add a List of ConfigSourcePackage built with a config source builder, to gestalt the builder.
     *
     * @param configSourcePackage add list of Config source Package
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addSources(List<ConfigSourcePackage> configSourcePackage) {
        Objects.requireNonNull(configSourcePackage, "ConfigSourcePackage should not be null");
        this.configSourcePackages.addAll(configSourcePackage);
        return this;
    }


    /**
     * Add a config reload strategy to the builder.
     *
     * @param configReloadStrategy add a config reload strategy.
     * @return GestaltBuilder builder
     * @deprecated prefer the use of {@link GestaltBuilder#addSource(ConfigSourcePackage) addSource} with a source builder.
     *     Use the source builder to add a reload strategy
     */
    @Deprecated(since = "0.23.4", forRemoval = true)
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
     * @deprecated prefer the use of {@link GestaltBuilder#addSource(ConfigSourcePackage) addSource} with a source builder.
     *     Use the source builder to add a reload strategy
     */
    @Deprecated(since = "0.23.4", forRemoval = true)
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
     * @throws GestaltConfigurationException if there are no config loaders.
     */
    public GestaltBuilder setConfigLoaders(List<ConfigLoader> configLoaders) throws GestaltConfigurationException {
        if (configLoaders == null || configLoaders.isEmpty()) {
            throw new GestaltConfigurationException("No config loader provided while setting config loaders");
        }
        this.configLoaders = new ArrayList<>(configLoaders);
        return this;
    }

    /**
     * Adds a list of config loader to the builder.
     * If there are any config loaders set, it will not add the defaults. So you will need to add the defaults manually if needed.
     *
     * @param configLoaders a list of config loader
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException if the config loaders are empty
     */
    public GestaltBuilder addConfigLoaders(List<ConfigLoader> configLoaders) throws GestaltConfigurationException {
        if (configLoaders == null || configLoaders.isEmpty()) {
            throw new GestaltConfigurationException("No config loader provided while adding config loaders");
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
     * Sets the list of ConfigNodeProcessor. Replaces any ConfigNodeProcessor already set.
     *
     * @param configNodeProcessors list of ConfigNodeProcessor to run.
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException exception if there are no ConfigNodeProcessor
     */
    public GestaltBuilder setConfigNodeProcessors(List<ConfigNodeProcessor> configNodeProcessors) throws GestaltConfigurationException {
        if (configNodeProcessors == null || configNodeProcessors.isEmpty()) {
            throw new GestaltConfigurationException("No ConfigNodeProcessor provided while setting");
        }
        this.configNodeProcessors = new ArrayList<>(configNodeProcessors);

        return this;
    }

    /**
     * List of ConfigNodeProcessor to add to the builder.
     *
     * @param configNodeProcessors list of ConfigNodeProcessor to add.
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException no ConfigNodeProcessor provided
     */
    public GestaltBuilder addConfigNodeProcessors(List<ConfigNodeProcessor> configNodeProcessors) throws GestaltConfigurationException {
        if (configNodeProcessors == null || configNodeProcessors.isEmpty()) {
            throw new GestaltConfigurationException("No ConfigNodeProcessor provided while adding");
        }
        this.configNodeProcessors.addAll(configNodeProcessors);

        return this;
    }

    /**
     * Add a single ConfigNodeProcessor to the builder.
     *
     * @param configNodeProcessor add a single ConfigNodeProcessor
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addConfigNodeProcessor(ConfigNodeProcessor configNodeProcessor) {
        Objects.requireNonNull(configNodeProcessor, "ConfigNodeProcessor should not be null");
        this.configNodeProcessors.add(configNodeProcessor);
        return this;
    }

    /**
     * Sets the list of PathMappers. Replaces any PathMappers already set.
     *
     * @param pathMappers list of pathMappers to run.
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException exception if there are no pathMappers
     */
    public GestaltBuilder setPathMappers(List<PathMapper> pathMappers) throws GestaltConfigurationException {
        if (pathMappers == null || pathMappers.isEmpty()) {
            throw new GestaltConfigurationException("No PathMappers provided while setting");
        }
        this.pathMappers = pathMappers;

        return this;
    }

    /**
     * List of PostProcessor to add to the builder.
     *
     * @param pathMappers list of PathMapper to add.
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException no PathMapper provided
     * @deprecated renamed to addPathMappers to be consistent with naming.
     *     Please use {@link GestaltBuilder#addPathMappers(List)}
     */
    @Deprecated(since = "0.25.3", forRemoval = true)
    public GestaltBuilder addPathMapper(List<PathMapper> pathMappers) throws GestaltConfigurationException {
        addPathMappers(pathMappers);

        return this;
    }

    /**
     * Add a single PathMapper to the builder.
     *
     * @param pathMapper add a single PathMapper
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addPathMapper(PathMapper pathMapper) {
        Objects.requireNonNull(pathMapper, "PathMapper should not be null");
        this.pathMappers.add(pathMapper);
        return this;
    }

    /**
     * List of PostProcessor to add to the builder.
     *
     * @param pathMappers list of PathMapper to add.
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException no PathMapper provided
     */
    public GestaltBuilder addPathMappers(List<PathMapper> pathMappers) throws GestaltConfigurationException {
        if (pathMappers == null || pathMappers.isEmpty()) {
            throw new GestaltConfigurationException("No PathMapper provided while adding");
        }
        this.pathMappers.addAll(pathMappers);

        return this;
    }

    /**
     * Sets the list of ObservationRecorder. Replaces any ObservabilityRecorder already set.
     *
     * @param observationRecorders list of observationRecorders to record observations to.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setObservationsRecorders(List<ObservationRecorder> observationRecorders) {
        Objects.requireNonNull(observationRecorders, "No ObservationRecorder provided while setting");
        this.observationRecorders = new ArrayList<>(observationRecorders);

        return this;
    }

    /**
     * List of ObservationRecorder to add to the builder.
     *
     * @param observationRecordersSet list of ObservationRecorder to add.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addObservationsRecorders(List<ObservationRecorder> observationRecordersSet) {
        Objects.requireNonNull(observationRecordersSet, "ObservationRecorder should not be null");

        observationRecorders.addAll(observationRecordersSet);

        return this;
    }

    /**
     * Add a single ObservationRecorder to the builder.
     *
     * @param observationRecorder add a single ObservationRecorder
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addObservationsRecorder(ObservationRecorder observationRecorder) {
        Objects.requireNonNull(observationRecorder, "ObservationRecorder should not be null");
        this.observationRecorders.add(observationRecorder);
        return this;
    }

    /**
     * Sets the list of Validator. Replaces any Validator already set.
     *
     * @param configValidators list of Validator to validate objects.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setValidators(List<ConfigValidator> configValidators) {
        Objects.requireNonNull(configValidators, "No Validators provided while setting");
        this.configValidators = new ArrayList<>(configValidators);

        return this;
    }

    /**
     * List of Validator to add to the builder.
     *
     * @param validatorsSet list of Validator to add.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addValidators(List<ConfigValidator> validatorsSet) {
        Objects.requireNonNull(validatorsSet, "Validator should not be null");

        configValidators.addAll(validatorsSet);

        return this;
    }

    /**
     * Add a single Validator to the builder.
     *
     * @param configValidator add a single Validator
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addValidator(ConfigValidator configValidator) {
        Objects.requireNonNull(configValidator, "Validator should not be null");
        this.configValidators.add(configValidator);
        return this;
    }

    /**
     * Sets the list of ResultProcessor. Replaces any ResultProcessor already set.
     *
     * @param resultProcessors list of resultProcessors.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setResultProcessor(List<ResultProcessor> resultProcessors) {
        Objects.requireNonNull(resultProcessors, "ResultProcessor should not be null");
        this.resultProcessors = new ArrayList<>(resultProcessors);

        return this;
    }

    /**
     * List of ResultProcessor to add to the builder.
     *
     * @param resultProcessorSet list of ResultProcessor to add.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addResultProcessors(List<ResultProcessor> resultProcessorSet) {
        Objects.requireNonNull(resultProcessorSet, "ResultProcessor should not be null");

        resultProcessors.addAll(resultProcessorSet);

        return this;
    }

    /**
     * Add a single ResultProcessor to the builder.
     *
     * @param resultProcessor add a single ResultProcessor
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addResultProcessor(ResultProcessor resultProcessor) {
        Objects.requireNonNull(resultProcessor, "ResultProcessor should not be null");
        this.resultProcessors.add(resultProcessor);
        return this;
    }

    /**
     * Sets the ConfigSourceFactoryService if you want to provide your own. Otherwise, a default is provided.
     *
     * @param configNodeFactoryService the ConfigSourceFactoryService
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setConfigSourceFactoryService(ConfigNodeFactoryService configNodeFactoryService) {
        Objects.requireNonNull(configNodeFactoryService, "ConfigSourceFactoryService should not be null");
        this.configNodeFactoryService = configNodeFactoryService;
        return this;
    }

    /**
     * Sets the list of ConfigSourceFactory. Replaces any ConfigSourceFactory already set.
     *
     * @param configSourceFactories list of ConfigSourceFactory.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setConfigSourceFactories(List<ConfigNodeFactory> configSourceFactories) {
        Objects.requireNonNull(configSourceFactories, "ConfigSourceFactory should not be null");
        this.configSourceFactories = new ArrayList<>(configSourceFactories);

        return this;
    }

    /**
     * List of ConfigSourceFactory to add to the builder.
     *
     * @param configSourceFactorySet list of ConfigSourceFactory to add.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addConfigSourceFactories(List<ConfigNodeFactory> configSourceFactorySet) {
        Objects.requireNonNull(configSourceFactorySet, "ConfigSourceFactory should not be null");

        configSourceFactories.addAll(configSourceFactorySet);

        return this;
    }

    /**
     * Add a single ConfigSourceFactory to the builder.
     *
     * @param configSourceFactory add a single ConfigSourceFactory
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addConfigSourceFactory(ConfigNodeFactory configSourceFactory) {
        Objects.requireNonNull(configSourceFactory, "ConfigSourceFactory should not be null");
        this.configSourceFactories.add(configSourceFactory);
        return this;
    }

    /**
     * Set the mask to use when replacing a leaf value matching a security rule.
     *
     * @param mask mask to use when replacing a leaf value matching a security rule
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setSecurityMask(String mask) {
        secretMask = mask;
        return this;
    }

    /**
     * Add a regex that we find when printing the nodes, to mask any secrets.
     * If the regex is found it will replace the node value with the mask.
     *
     * @param regex regex to find.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addSecurityMaskingRule(String regex) {
        securityMaskingRules.add(regex);
        return this;
    }

    /**
     * Sets all the regex that we find when printing the nodes, to mask any secrets.
     * If the regex is found it will replace the node value with the mask.
     *
     * @param regexs regex's to find.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setSecurityMaskingRule(Set<String> regexs) {
        securityMaskingRules = regexs;
        return this;
    }

    /**
     * Set a single temporary node access rule. If a path matches the regex, it will be limited to the 1 access.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param regex If a path matches the regex
     * @return the builder
     */
    public GestaltBuilder addTemporaryNodeAccessCount(String regex) {
        secretAccessCounts.add(new Pair<>(new RegexSecretChecker(Set.of(regex)), 1));
        return this;
    }

    /**
     * Set a single temporary node access rule. If a path matches the regex, it will be limited to the number of access counts.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param regex       If a path matches the regex
     * @param accessCount After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * @return the builder
     */
    public GestaltBuilder addTemporaryNodeAccessCount(String regex, int accessCount) {
        secretAccessCounts.add(new Pair<>(new RegexSecretChecker(Set.of(regex)), accessCount));
        return this;
    }

    /**
     * Set a set of temporary node access rule. If a path matches the regexs, it will be limited to the number of access counts.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param regexs      If a path matches the regex
     * @param accessCount After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * @return the builder
     */
    public GestaltBuilder addTemporaryNodeAccessCount(Set<String> regexs, int accessCount) {
        secretAccessCounts.add(new Pair<>(new RegexSecretChecker(regexs), accessCount));
        return this;
    }

    /**
     * Set a set of temporary node access rule. If a path matches the regexs, it will be limited to the number of access counts.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param secretAccessCounts list of secret SecretChecker with the number of times the temporary node should be accessible
     * @return the builder
     */
    public GestaltBuilder setTemporaryNodeAccessCount(List<Pair<SecretChecker, Integer>> secretAccessCounts) {
        Objects.requireNonNull(secretAccessCounts, "secretAccessCounts should not be null");
        this.secretAccessCounts = secretAccessCounts;
        return this;
    }

    /**
     * Set what secrets you want to be encrypted. If you already added any secrets to be encrypted this will overwrite them.
     *
     * @param encryptedSecrets what secrets to encrypt
     * @return the builder
     */
    public GestaltBuilder setEncryptedSecrets(SecretChecker encryptedSecrets
    ) {
        Objects.requireNonNull(encryptedSecrets, "encryptedSecrets should not be null");
        this.encryptedSecrets = encryptedSecrets;
        return this;
    }

    /**
     * Add a new secret to be encrypted.
     *
     * @param regex the regex for the secret to be encrypted.
     * @return the builder
     */
    public GestaltBuilder addEncryptedSecret(String regex) {
        Objects.requireNonNull(regex, "regex should not be null");
        encryptedSecrets.addSecret(regex);
        return this;
    }

    /**
     * ***USE WITH CAUTION.***
     * Set the sentence lexer that will be used throughout Gestalt.
     * It is used in several locations, to convert the path requested to tokens, so we can navigate the config tree using the tokens.
     * It is used to normalize sentences when parsing configs. (except Environment Variables)
     * It is also used in the path mappers to try different ways to navigate the config tree during decoding.
     *
     * <p>If you set a new lexer that is for example case-sensitive, when you decode an object
     * public record Person(String name, Integer id) if in the properties it is admin.Name = sarah
     * it will not be able to decode as the name field is lower case and the property is upper case.
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
        Objects.requireNonNull(configNodeService, "ConfigNodeService should not be null");
        this.configNodeService = configNodeService;
        return this;
    }

    /**
     * Sets the ObservationService if you want to provide your own. Otherwise, a default is provided.
     *
     * <p>If there are any ObservationRecorder, it will not add the default observations recorders.
     * So you will need to add the defaults manually if needed.
     *
     * @param observationService Observation Service
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setObservationsService(ObservationService observationService) {
        Objects.requireNonNull(observationService, "observationService should not be null");
        this.observationService = observationService;
        observationService.addObservationRecorders(observationRecorders);
        return this;
    }

    /**
     * Set your own custom Secret Concealer. If you set your own custom Secrete Concealer,
     * you need to set your own rules and SecretObfuscation. The builder will not add the rules to a user supplied SecretConcealer
     *
     * @param secretConcealer your own custom Secret Concealer.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setSecretConcealer(SecretConcealer secretConcealer) {
        this.secretConcealer = secretConcealer;
        return this;
    }

    /**
     * Set your own custom Secret Obfuscator. Used to determine how to mask secrets.
     *
     * @param secretObfuscator your own custom Secret Obfuscator.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setSecretObfuscation(SecretObfuscator secretObfuscator) {
        this.secretObfuscator = secretObfuscator;
        return this;
    }


    /**
     * Sets the ResultsProcessorService if you want to provide your own. Otherwise, a default is provided.
     *
     * @param resultsProcessorService the resultsProcessorService
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setResultsProcessorService(ResultsProcessorService resultsProcessorService) {
        Objects.requireNonNull(resultsProcessorService, "ResultsProcessorService should not be null");
        this.resultsProcessorService = resultsProcessorService;
        resultsProcessorService.addResultProcessors(resultProcessors);
        return this;
    }

    /**
     * Sets the ConfigNodeProcessorService if you want to provide your own. Otherwise, a default is provided.
     *
     * @param configNodeProcessorService the ConfigNodeProcessorService
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setConfigNodeProcessorService(ConfigNodeProcessorService configNodeProcessorService) {
        Objects.requireNonNull(configNodeProcessorService, "ConfigNodeProcessorService should not be null");
        this.configNodeProcessorService = configNodeProcessorService;
        configNodeProcessorService.addConfigNodeProcessors(configNodeProcessors);
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
     * @throws GestaltConfigurationException no decoders provided
     */
    public GestaltBuilder setDecoders(List<Decoder<?>> decoders) throws GestaltConfigurationException {
        if (decoders == null || decoders.isEmpty()) {
            throw new GestaltConfigurationException("No decoders provided while setting decoders");
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
     * @throws GestaltConfigurationException no decoders provided
     */
    public GestaltBuilder addDecoders(List<Decoder<?>> decoders) throws GestaltConfigurationException {
        if (decoders == null || decoders.isEmpty()) {
            throw new GestaltConfigurationException("No decoders provided while adding decoders");
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

    public GestaltBuilder addModuleConfig(GestaltModuleConfig extension) {
        modules.put(extension.getClass(), extension);
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
     * Get Treat warnings as errors.
     *
     * @return warningsAsErrors
     */
    public Boolean isTreatWarningsAsErrors() {
        return treatWarningsAsErrors;
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
     * @return treatMissingValuesAsErrors the settings for treating missing object values as errors.
     */
    public Boolean getTreatMissingValuesAsErrors() {
        return treatMissingValuesAsErrors;
    }


    /**
     * treat missing field values in an object, proxy, record or data object as errors.
     *
     * <p>If this is true, any time a value that is not discretionary is missing, there will be an error.
     * If this is false, a missing value will be returned as null or the default initialization. Null for objects and 0 for primitives.
     *
     * @param treatMissingValuesAsErrors the settings for treating missing values as errors.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setTreatMissingValuesAsErrors(Boolean treatMissingValuesAsErrors) {
        this.treatMissingValuesAsErrors = treatMissingValuesAsErrors;
        return this;
    }

    /**
     * Get treat missing discretionary values (optional, fields with defaults, fields with default annotations)
     * in an object, proxy, record or data object as errors.
     *
     * <p>If this is false you will be able to get the configuration with default values or an empty Optional.
     * If this is true, if a field is missing and would have had a default it will fail and throw an exception.
     *
     * @return treatMissingDiscretionaryValuesAsErrors the settings for treating missing discretionary values as errors.
     */
    public Boolean getTreatMissingDiscretionaryValuesAsErrors() {
        return treatMissingDiscretionaryValuesAsErrors;
    }

    /**
     * Sets treat missing discretionary values (optional, fields with defaults, fields with default annotations) as an error.
     * If this is false you will be able to get the configuration with default values or an empty Optional.
     * If this is true, if a field is missing and would have had a default it will fail and throw an exception.
     *
     * @param treatMissingDiscretionaryValuesAsErrors the settings for treating missing discretionary values as errors.
     * @return GestaltBuilder the builder
     */
    public GestaltBuilder setTreatMissingDiscretionaryValuesAsErrors(boolean treatMissingDiscretionaryValuesAsErrors) {
        this.treatMissingDiscretionaryValuesAsErrors = treatMissingDiscretionaryValuesAsErrors;
        return this;
    }

    /**
     * Treat null values in classes after decoding as errors.
     *
     * @param treatNullValuesInClassAsErrors treat null values in classes after decoding as errors
     * @return GestaltBuilder builder
     * @deprecated This value is no longer used, Please use {@link #setTreatMissingDiscretionaryValuesAsErrors(boolean)}
     *     and {@link #setTreatMissingValuesAsErrors(Boolean)}
     */
    @Deprecated(since = "0.25.0", forRemoval = true)
    public GestaltBuilder setTreatNullValuesInClassAsErrors(Boolean treatNullValuesInClassAsErrors) {
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
     * If we are to enable observations.
     *
     * @param observationsEnabled If we are to enable observations
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setObservationsEnabled(Boolean observationsEnabled) {
        this.observationsEnabled = observationsEnabled;
        return this;
    }

    /**
     * If we are to enable validation.
     *
     * @param validationEnabled If we are to enable validation
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
        return this;
    }

    /**
     * If we should add the core result processors.
     * The core result processors are central to Gestalt and should be added.
     * The only reason to not add them is to fully customize gestalt.
     * If they are not added you will get strange and possible incorrect behaviour.
     *
     * @param addCoreResultProcessors add a core result processor.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setAddCoreResultProcessors(boolean addCoreResultProcessors) {
        this.addCoreResultProcessors = addCoreResultProcessors;
        return this;
    }

    /**
     * Set the ConfigNodeTagResolutionStrategy to allow users to override how we select the roots to find nodes to merge based on the tags.
     * The default if not set is EqualTagsWithDefaultTagResolutionStrategy
     *
     * @param configNodeTagResolutionStrategy Allows users to override how we select the roots to find nodes to merge based on the tags.
     * @return the builder
     */
    public GestaltBuilder setConfigNodeTagResolutionStrategy(ConfigNodeTagResolutionStrategy configNodeTagResolutionStrategy) {
        this.configNodeTagResolutionStrategy = configNodeTagResolutionStrategy;
        return this;
    }

    /**
     * Set the TagMergingStrategy that controls how we merge the tags provided with the request and the default tags,
     * then returns the results.
     *
     * @param tagMergingStrategy the Merges the tags provided with the request and the default tags, then returns the results.
     * @return the builder
     */
    public GestaltBuilder setTagMergingStrategy(TagMergingStrategy tagMergingStrategy) {
        this.tagMergingStrategy = tagMergingStrategy;
        return this;
    }

    /**
     * Set a date decoder format. Used to decode date times.
     *
     * @param dateDecoderFormat a date decoder format
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setDateDecoderFormat(DateTimeFormatter dateDecoderFormat) {
        this.dateDecoderFormat = dateDecoderFormat;
        return this;
    }

    /**
     * Provide the log level when we log a message when a config is missing, but we provided a default, or it is Optional.
     *
     * @return Log level
     */
    public Level getLogLevelForMissingValuesWhenDefaultOrOptional() {
        return logLevelForMissingValuesWhenDefaultOrOptional;
    }

    /**
     * Provide the log level when we log a message when a config is missing, but we provided a default, or it is Optional.
     *
     * @param logLevelForMissingValuesWhenDefaultOrOptional log level
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setLogLevelForMissingValuesWhenDefaultOrOptional(Level logLevelForMissingValuesWhenDefaultOrOptional) {
        this.logLevelForMissingValuesWhenDefaultOrOptional = logLevelForMissingValuesWhenDefaultOrOptional;
        return this;
    }

    /**
     * Set a local date time format. Used to decode local date times.
     *
     * @param localDateTimeFormat a date decoder format
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setLocalDateTimeFormat(DateTimeFormatter localDateTimeFormat) {
        this.localDateTimeFormat = localDateTimeFormat;
        return this;
    }

    /**
     * Set a local date format. Used to decode local date.
     *
     * @param localDateFormat a local date decoder format
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setLocalDateFormat(DateTimeFormatter localDateFormat) {
        this.localDateFormat = localDateFormat;
        return this;
    }

    /**
     * Set a Token that represents the opening of a string substitution.
     *
     * @param substitutionOpeningToken Token that represents the opening of a string substitution.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setSubstitutionOpeningToken(String substitutionOpeningToken) {
        this.substitutionOpeningToken = substitutionOpeningToken;
        return this;
    }

    /**
     * Token that represents the closing of a string substitution.
     *
     * @param substitutionClosingToken a token that represents the closing of a string substitution.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setSubstitutionClosingToken(String substitutionClosingToken) {
        this.substitutionClosingToken = substitutionClosingToken;
        return this;
    }

    /**
     * Get the maximum string substitution nested depth.
     * If you have nested or recursive substitutions that go deeper than this it will fail.
     *
     * @return the maximum string substitution nested depth.
     */
    public Integer getMaxSubstitutionNestedDepth() {
        return maxSubstitutionNestedDepth;
    }

    /**
     * Set the maximum string substitution nested depth.
     * If you have nested or recursive substitutions that go deeper than this it will fail.
     *
     * @param maxSubstitutionNestedDepth the maximum string substitution nested depth.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setMaxSubstitutionNestedDepth(Integer maxSubstitutionNestedDepth) {
        this.maxSubstitutionNestedDepth = maxSubstitutionNestedDepth;
        return this;
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
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setSubstitutionRegex(String substitutionRegex) {
        this.substitutionRegex = substitutionRegex;
        return this;
    }

    /**
     * Set the keyword that is used to determine if a node is an include from a source.
     *
     * @param nodeIncludeKeyword the keyword that is used to determine if a node is an include from a source.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setNodeIncludeKeyword(String nodeIncludeKeyword) {
        this.nodeIncludeKeyword = nodeIncludeKeyword;
        return this;
    }

    /**
     * Get the mode the for proxy decoder.
     *
     * @return the mode the for proxy decoder
     */
    public ProxyDecoderMode getProxyDecoderMode() {
        return proxyDecoderMode;
    }

    /**
     * Set the mode the for proxy decoder.
     *
     * @param proxyDecoderMode the mode the for proxy decoder
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setProxyDecoderMode(ProxyDecoderMode proxyDecoderMode) {
        this.proxyDecoderMode = proxyDecoderMode;
        return this;
    }


    /**
     * Get default tags to apply to all calls to get a configuration when tags are not provided.
     *
     * @return default tags
     */
    public Tags getDefaultTags() {
        return defaultTags;
    }

    /**
     * Set default tags to apply to all calls to get a configuration when tags are not provided.
     *
     * @param defaultTags Set of default tags to apply to all calls to get a configuration when tags are not provided.
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setDefaultTags(Tags defaultTags) {
        this.defaultTags = defaultTags;
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
            logger.log(WARNING, "Found duplicate decoders {0}", duplicates);
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
            logger.log(WARNING, "Found duplicate config loaders {0}", duplicates);
        }

        return configLoaders.stream().filter(CollectionUtils.distinctBy(ConfigLoader::name)).collect(Collectors.toList());
    }

    /**
     * Build Gestalt.
     *
     * @return Gestalt
     * @throws GestaltConfigurationException multiple validations can throw exceptions
     */
    public Gestalt build() throws GestaltConfigurationException {
        if (configSourcePackages.isEmpty()) {
            throw new GestaltConfigurationException("No sources provided");
        }

        if (secretConcealer == null) {
            secretConcealer = new SecretConcealerManager(securityMaskingRules, secretObfuscator);
        }

        gestaltConfig = rebuildConfig();
        gestaltConfig.registerModuleConfig(modules);

        if (sentenceLexer == null) {
            sentenceLexer = new PathLexer();
        }

        if (configNodeTagResolutionStrategy == null) {
            configNodeTagResolutionStrategy = new EqualTagsWithDefaultTagResolutionStrategy();
        }

        if (configNodeProcessorService == null) {
            // initialize the ConfigNodeProcessorManager dont provide configNodeProcessors yet, they will be added below.
            configNodeProcessorService = new ConfigNodeProcessorManager(List.of(), sentenceLexer);
        }

        if (configNodeService == null) {
            configNodeService = new ConfigNodeManager(configNodeTagResolutionStrategy, configNodeProcessorService, sentenceLexer);
        }

        if (tagMergingStrategy == null) {
            tagMergingStrategy = new TagMergingStrategyFallback();
        }

        configurePathMappers();
        configureDecoders();
        configureObservations();
        configureValidation();
        configureCoreResultProcessors();
        configureResultProcessors();
        configureConfigLoaders();
        configureConfigSourceFactory();

        configureTemporaryNodesModule();
        configureEncryptedSecretsNodesModule();
        configureConfigNodeProcessor();

        // create a new GestaltCoreReloadStrategy to listen for Gestalt Core Reloads.
        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        final GestaltCore gestaltCore = new GestaltCore(configLoaderService, configSourcePackages, decoderService, sentenceLexer,
            gestaltConfig, configNodeService, coreReloadListenersContainer, secretConcealer, observationService,
            resultsProcessorService, defaultTags, tagMergingStrategy);

        // register gestaltCore with all the source reload strategies.
        reloadStrategies.forEach(it -> it.registerListener(gestaltCore));

        // register gestaltCore with all the source reload strategies.
        configSourcePackages.stream()
            .flatMap(it -> it.getConfigReloadStrategies().stream())
            .forEach(it -> it.registerListener(gestaltCore));
        // Add all listeners for the core update.
        coreCoreReloadListeners.forEach(coreReloadListenersContainer::registerListener);

        if (useCacheDecorator) {
            // do not cache the temporary nodes
            var nonCacheableSecrets = secretAccessCounts.stream().map(Pair::getFirst).collect(Collectors.toList());
            // do not cache the encrypted nodes.
            nonCacheableSecrets.add(encryptedSecrets);

            GestaltCache gestaltCache = new GestaltCache(gestaltCore, defaultTags, observationService, gestaltConfig,
                tagMergingStrategy, nonCacheableSecrets);

            // Register the cache with the gestaltCoreReloadStrategy so when the core reloads
            // we can clear the cache.
            coreReloadListenersContainer.registerListener(gestaltCache);
            return gestaltCache;
        } else {
            return gestaltCore;
        }
    }

    private void configureConfigNodeProcessor() {
        if (configNodeProcessors.isEmpty()) {
            logger.log(TRACE, "No Config Node Processors provided, using defaults");
            addDefaultPostProcessors();
        }

        configNodeProcessors = configNodeProcessors.stream().filter(Objects::nonNull).collect(Collectors.toList());

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, configNodeService, sentenceLexer,
            secretConcealer, configNodeFactoryService);

        configNodeProcessors.forEach(it -> it.applyConfig(config));

        configNodeProcessorService.addConfigNodeProcessors(configNodeProcessors);
    }

    private void configureConfigLoaders() {
        // Setup the config loaders.
        if (configLoaders.isEmpty()) {
            logger.log(TRACE, "No decoders provided, using defaults");
            addDefaultConfigLoaders();
        }
        // get all the config loaders from the configLoaderRegistry, combine them with the ones in the builder,
        // and update the configLoaderRegistry
        configLoaders.addAll(configLoaderService.getConfigLoaders());
        List<ConfigLoader> dedupedConfigs = dedupeConfigLoaders();

        configLoaders = configLoaders.stream().filter(Objects::nonNull).collect(Collectors.toList());
        configLoaders.forEach(it -> it.applyConfig(gestaltConfig));

        configLoaderService.setLoaders(dedupedConfigs);
    }

    // should be called after configureObservations, so we have an observationManager to pass into the ValidationResultProcessor.
    private void configureValidation() {
        if (validationEnabled) {
            // setup the default validators, if there are none add the default ones.
            if (configValidators.isEmpty()) {
                logger.log(TRACE, "No validators recorders provided, using defaults");
                addDefaultValidators();
            }
            configValidators = configValidators.stream().filter(Objects::nonNull).collect(Collectors.toList());
            configValidators.forEach(it -> it.applyConfig(gestaltConfig));

            var validationResultProcessor = new ValidationResultProcessor(configValidators, observationService);
            validationResultProcessor.applyConfig(gestaltConfig);

            // if the ResultsProcessorManager does not exist, create it.
            // Otherwise, get all the recorders from the ResultsProcessorManager, combine them with the ones in the builder,
            if (resultsProcessorService == null) {
                resultsProcessorService = new ResultsProcessorManager(List.of(validationResultProcessor));
            } else {
                resultsProcessorService.addResultProcessors(List.of(validationResultProcessor));
            }
        }
    }

    private void configureCoreResultProcessors() {
        // setup the core resultProcessors, if there are none or they are disabled print out a warning.
        if (coreResultProcessors.isEmpty() || !addCoreResultProcessors) {
            logger.log(WARNING, "No Core ResultProcessors added, Gestalt may not behave as expected");
            return;
        }

        coreResultProcessors.forEach(it -> it.applyConfig(gestaltConfig));

        // if the ResultsProcessorManager does not exist, create it.
        // Otherwise, get all the recorders from the ResultsProcessorManager, combine them with the ones in the builder,
        if (resultsProcessorService == null) {
            resultsProcessorService = new ResultsProcessorManager(coreResultProcessors);
        } else {
            resultsProcessorService.addResultProcessors(coreResultProcessors);
        }
    }

    private void configureResultProcessors() {
        // setup the default resultProcessors, if there are none add the default ones.
        if (resultProcessors.isEmpty()) {
            logger.log(TRACE, "No resultProcessors provided, using defaults");
            addDefaultResultProcessor();
        }
        resultProcessors = resultProcessors.stream().filter(Objects::nonNull).collect(Collectors.toList());
        resultProcessors.forEach(it -> it.applyConfig(gestaltConfig));

        // if the ResultsProcessorManager does not exist, create it.
        // Otherwise, get all the recorders from the ResultsProcessorManager, combine them with the ones in the builder,
        if (resultsProcessorService == null) {
            resultsProcessorService = new ResultsProcessorManager(resultProcessors);
        } else {
            resultsProcessorService.addResultProcessors(resultProcessors);
        }
    }

    private void configureConfigSourceFactory() {
        // setup the default configSourceFactories, if there are none add the default ones.
        if (configSourceFactories.isEmpty()) {
            logger.log(TRACE, "No configSourceFactories provided, using defaults");
            addDefaultConfigSourceFactory();
        }
        ConfigNodeFactoryConfig configNodeFactoryConfig =
            new ConfigNodeFactoryConfig(configLoaderService, configNodeService, sentenceLexer);

        configSourceFactories = configSourceFactories.stream().filter(Objects::nonNull).collect(Collectors.toList());
        configSourceFactories.forEach(it -> it.applyConfig(configNodeFactoryConfig));

        // if the ConfigSourceFactoryManager does not exist, create it.
        // Otherwise, get all the factories from the ConfigSourceFactoryManager, combine them with the ones in the builder,
        if (configNodeFactoryService == null) {
            configNodeFactoryService = new ConfigNodeFactoryManager(configSourceFactories);
        } else {
            configNodeFactoryService.addConfigSourceFactories(configSourceFactories);
        }
    }

    private void configureObservations() {
        // setup the default observationRecorders, if there are none add the default ones.
        if (observationRecorders.isEmpty()) {
            logger.log(TRACE, "No observation recorders provided, using defaults");
            addDefaultObservationsRecorder();
        }
        observationRecorders = observationRecorders.stream().filter(Objects::nonNull).collect(Collectors.toList());
        observationRecorders.forEach(it -> it.applyConfig(gestaltConfig));

        // if the ObservationManager does not exist, create it.
        // Otherwise, get all the recorders from the ObservationManager, combine them with the ones in the builder,
        if (observationService == null) {
            observationService = new ObservationManager(observationRecorders);
        } else {
            observationService.addObservationRecorders(observationRecorders);
        }
    }

    private void configurePathMappers() {
        // setup the default path mappers, if there are none, add the default ones.
        if (pathMappers.isEmpty()) {
            logger.log(TRACE, "No path mapper provided, using defaults");
            addDefaultPathMappers();
        }
        pathMappers = pathMappers.stream().filter(Objects::nonNull).collect(Collectors.toList());
        pathMappers.forEach(it -> it.applyConfig(gestaltConfig));
    }

    private void configureDecoders() throws GestaltConfigurationException {
        // setup the decoders, if there are none, add the default ones.
        if (decoders.isEmpty()) {
            logger.log(TRACE, "No decoders provided, using defaults");
            addDefaultDecoders();
        }
        decoders = decoders.stream().filter(Objects::nonNull).collect(Collectors.toList());
        decoders.forEach(it -> it.applyConfig(gestaltConfig));

        // if the decoderService does not exist, create it.
        // Otherwise get all the decoders from the decoderService, combine them with the ones in the builder,
        // and update the decoderService
        if (decoderService == null) {
            decoderService = new DecoderRegistry(decoders, configNodeService, sentenceLexer, pathMappers);
        } else {
            decoders.addAll(decoderService.getDecoders());
            List<Decoder<?>> dedupedDecoders = dedupeDecoders();
            decoderService.setDecoders(dedupedDecoders);
        }
    }

    private void configureTemporaryNodesModule() {
        if (secretAccessCounts != null && !secretAccessCounts.isEmpty()) {
            if (gestaltConfig.getModuleConfig(TemporarySecretModule.class) == null) {
                gestaltConfig.registerModuleConfig(new TemporarySecretModule(secretAccessCounts));
            } else {
                TemporarySecretModule module = gestaltConfig.getModuleConfig(TemporarySecretModule.class);
                module.addSecretCounts(secretAccessCounts);
            }
        }
    }

    private void configureEncryptedSecretsNodesModule() {
        if (gestaltConfig.getModuleConfig(EncryptedSecretModule.class) == null) {
            gestaltConfig.registerModuleConfig(new EncryptedSecretModule(encryptedSecrets));
        }
    }

    private GestaltConfig rebuildConfig() {
        GestaltConfig newConfig = new GestaltConfig();

        newConfig.setTreatWarningsAsErrors(Objects.requireNonNullElseGet(treatWarningsAsErrors,
            () -> gestaltConfig.isTreatWarningsAsErrors()));

        newConfig.setTreatMissingArrayIndexAsError(Objects.requireNonNullElseGet(treatMissingArrayIndexAsError,
            () -> gestaltConfig.isTreatMissingArrayIndexAsError()));

        newConfig.setTreatMissingValuesAsErrors(Objects.requireNonNullElseGet(treatMissingValuesAsErrors,
            () -> gestaltConfig.isTreatMissingValuesAsErrors()));

        newConfig.setTreatMissingDiscretionaryValuesAsErrors(Objects.requireNonNullElseGet(treatMissingDiscretionaryValuesAsErrors,
            () -> gestaltConfig.isTreatMissingDiscretionaryValuesAsErrors()));

        newConfig.setLogLevelForMissingValuesWhenDefaultOrOptional(
            Objects.requireNonNullElseGet(logLevelForMissingValuesWhenDefaultOrOptional,
                () -> gestaltConfig.getLogLevelForMissingValuesWhenDefaultOrOptional()));

        newConfig.setDateDecoderFormat(Objects.requireNonNullElseGet(dateDecoderFormat,
            () -> gestaltConfig.getDateDecoderFormat()));

        newConfig.setLocalDateTimeFormat(Objects.requireNonNullElseGet(localDateTimeFormat,
            () -> gestaltConfig.getLocalDateTimeFormat()));

        newConfig.setLocalDateFormat(Objects.requireNonNullElseGet(localDateFormat,
            () -> gestaltConfig.getLocalDateFormat()));

        newConfig.setSubstitutionOpeningToken(Objects.requireNonNullElseGet(substitutionOpeningToken,
            () -> gestaltConfig.getSubstitutionOpeningToken()));

        newConfig.setSubstitutionClosingToken(Objects.requireNonNullElseGet(substitutionClosingToken,
            () -> gestaltConfig.getSubstitutionClosingToken()));

        newConfig.setMaxSubstitutionNestedDepth(Objects.requireNonNullElseGet(maxSubstitutionNestedDepth,
            () -> gestaltConfig.getMaxSubstitutionNestedDepth()));

        newConfig.setSubstitutionRegex(Objects.requireNonNullElseGet(substitutionRegex,
            () -> gestaltConfig.getSubstitutionRegex()));

        newConfig.setProxyDecoderMode(Objects.requireNonNullElseGet(proxyDecoderMode,
            () -> gestaltConfig.getProxyDecoderMode()));

        newConfig.setObservationsEnabled(Objects.requireNonNullElseGet(observationsEnabled,
            () -> gestaltConfig.isObservationsEnabled()));

        newConfig.setSentenceLexer(Objects.requireNonNullElseGet(sentenceLexer,
            () -> gestaltConfig.getSentenceLexer()));

        newConfig.setNodeIncludeKeyword(Objects.requireNonNullElseGet(nodeIncludeKeyword,
            () -> gestaltConfig.getNodeIncludeKeyword()));

        return newConfig;
    }
}
