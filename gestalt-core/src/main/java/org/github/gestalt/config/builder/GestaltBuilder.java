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
import org.github.gestalt.config.observations.ObservationManager;
import org.github.gestalt.config.observations.ObservationRecorder;
import org.github.gestalt.config.node.ConfigNodeManager;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.path.mapper.PathMapper;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.github.gestalt.config.reload.ConfigReloadStrategy;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.CoreReloadListenersContainer;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.CollectionUtils;
import org.github.gestalt.config.validation.ConfigValidator;
import org.github.gestalt.config.validation.ValidationManager;

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
    private ConfigLoaderService configLoaderService = new ConfigLoaderRegistry();
    private DecoderService decoderService;
    private SentenceLexer sentenceLexer = new PathLexer();
    private GestaltConfig gestaltConfig = new GestaltConfig();
    private ObservationManager observationManager;
    private ValidationManager validationManager;
    private ConfigNodeService configNodeService = new ConfigNodeManager(sentenceLexer);
    private List<ConfigSourcePackage> configSourcePackages = new ArrayList<>();
    private List<Decoder<?>> decoders = new ArrayList<>();
    private List<ConfigLoader> configLoaders = new ArrayList<>();
    private List<PostProcessor> postProcessors = new ArrayList<>();
    private List<PathMapper> pathMappers = new ArrayList<>();
    private List<ObservationRecorder> observationRecorders = new ArrayList<>();
    private List<ConfigValidator> configValidators = new ArrayList<>();
    private boolean useCacheDecorator = true;
    private Set<String> securityRules = new HashSet<>(
        List.of("bearer", "cookie", "credential", "id",
            "key", "keystore", "passphrase", "password",
            "private", "salt", "secret", "secure",
            "ssl", "token", "truststore"));
    private String secretMask = "*****";
    private SecretConcealer secretConcealer;

    private Boolean treatWarningsAsErrors = null;
    private Boolean treatMissingArrayIndexAsError = null;
    private Boolean treatMissingValuesAsErrors = null;
    private Boolean treatMissingDiscretionaryValuesAsErrors = null;
    // If we should enable observations
    private Boolean observationsEnabled = null;
    // If we should enable Validation
    private Boolean validationEnabled = null;

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
        List<PostProcessor> postProcessorsSet = new ArrayList<>();
        ServiceLoader<PostProcessor> loader = ServiceLoader.load(PostProcessor.class);
        loader.forEach(postProcessorsSet::add);
        postProcessors.addAll(postProcessorsSet);
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
        this.configSourcePackages = configSourcePackage;
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
        this.configLoaders = configLoaders;
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
     * Sets the list of PostProcessors. Replaces any PostProcessors already set.
     *
     * @param postProcessors list of postProcessors to run.
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException exception if there are no postProcessors
     */
    public GestaltBuilder setPostProcessors(List<PostProcessor> postProcessors) throws GestaltConfigurationException {
        if (postProcessors == null || postProcessors.isEmpty()) {
            throw new GestaltConfigurationException("No PostProcessors provided while setting");
        }
        this.postProcessors = postProcessors;

        return this;
    }

    /**
     * List of PostProcessor to add to the builder.
     *
     * @param postProcessors list of PostProcessor to add.
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException no PostProcessor provided
     */
    public GestaltBuilder addPostProcessors(List<PostProcessor> postProcessors) throws GestaltConfigurationException {
        if (postProcessors == null || postProcessors.isEmpty()) {
            throw new GestaltConfigurationException("No PostProcessor provided while adding");
        }
        this.postProcessors.addAll(postProcessors);

        return this;
    }

    /**
     * Add a single PostProcessor to the builder.
     *
     * @param postProcessor add a single PostProcessor
     * @return GestaltBuilder builder
     */
    public GestaltBuilder addPostProcessor(PostProcessor postProcessor) {
        Objects.requireNonNull(postProcessor, "PostProcessor should not be null");
        this.postProcessors.add(postProcessor);
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
     * @throws GestaltConfigurationException exception if there are no ObservationRecorder
     */
    public GestaltBuilder setObservationsRecorders(List<ObservationRecorder> observationRecorders) throws GestaltConfigurationException {
        if (observationRecorders == null || observationRecorders.isEmpty()) {
            throw new GestaltConfigurationException("No ObservationRecorder provided while setting");
        }
        this.observationRecorders = observationRecorders;

        return this;
    }

    /**
     * List of ObservationRecorder to add to the builder.
     *
     * @param observationRecordersSet list of ObservationRecorder to add.
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException no ObservationRecorder provided
     */
    public GestaltBuilder addObservationsRecorders(List<ObservationRecorder> observationRecordersSet) throws GestaltConfigurationException {
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
     * @throws GestaltConfigurationException exception if there are no Validator
     */
    public GestaltBuilder setValidators(List<ConfigValidator> configValidators) throws GestaltConfigurationException {
        if (configValidators == null || configValidators.isEmpty()) {
            throw new GestaltConfigurationException("No Validators provided while setting");
        }
        this.configValidators = configValidators;

        return this;
    }

    /**
     * List of Validator to add to the builder.
     *
     * @param validatorsSet list of Validator to add.
     * @return GestaltBuilder builder
     * @throws GestaltConfigurationException no Validator provided
     */
    public GestaltBuilder addValidators(List<ConfigValidator> validatorsSet) throws GestaltConfigurationException {
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
        securityRules.add(regex);
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
        securityRules = regexs;
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
        Objects.requireNonNull(configNodeService, "ConfigNodeManager should not be null");
        this.configNodeService = configNodeService;
        return this;
    }

    /**
     * Sets the ObservationRecorder if you want to provide your own. Otherwise, a default is provided.
     *
     * <p>If there are any ObservationRecorder, it will not add the default observations recorders.
     * So you will need to add the defaults manually if needed.
     *
     * @param observationManager Observation Manager
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setObservationsManager(ObservationManager observationManager) {
        Objects.requireNonNull(observationManager, "ObservationManager should not be null");
        this.observationManager = observationManager;
        observationManager.addObservationRecorders(observationRecorders);
        return this;
    }

    /**
     * Sets the ValidationManager if you want to provide your own. Otherwise, a default is provided.
     *
     * <p>If there are any Validators, it will not add the default Validators.
     * So you will need to add the defaults manually if needed.
     *
     * @param validationManager Validation Manager
     * @return GestaltBuilder builder
     */
    public GestaltBuilder setValidationManager(ValidationManager validationManager) {
        Objects.requireNonNull(validationManager, "ValidationManager should not be null");
        this.validationManager = validationManager;
        validationManager.addValidators(configValidators);
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
    public GestaltBuilder setValidationEnabled(Boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
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

        secretConcealer = new SecretConcealer(securityRules, secretMask);

        gestaltConfig = rebuildConfig();
        gestaltConfig.registerModuleConfig(modules);

        configurePathMappers();
        configureDecoders();
        configureObservations();
        configureValidators();
        configureConfigLoaders();
        configurePostProcessors();

        configNodeService.setLexer(sentenceLexer);

        // create a new GestaltCoreReloadStrategy to listen for Gestalt Core Reloads.
        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        final GestaltCore gestaltCore = new GestaltCore(configLoaderService, configSourcePackages, decoderService, sentenceLexer,
            gestaltConfig, configNodeService, coreReloadListenersContainer, postProcessors, secretConcealer, observationManager,
            validationManager, defaultTags);

        // register gestaltCore with all the source reload strategies.
        reloadStrategies.forEach(it -> it.registerListener(gestaltCore));

        // register gestaltCore with all the source reload strategies.
        configSourcePackages.stream()
            .flatMap(it -> it.getConfigReloadStrategies().stream())
            .forEach(it -> it.registerListener(gestaltCore));
        // Add all listeners for the core update.
        coreCoreReloadListeners.forEach(coreReloadListenersContainer::registerListener);

        if (useCacheDecorator) {
            GestaltCache gestaltCache = new GestaltCache(gestaltCore, defaultTags, observationManager, gestaltConfig);

            // Register the cache with the gestaltCoreReloadStrategy so when the core reloads
            // we can clear the cache.
            coreReloadListenersContainer.registerListener(gestaltCache);
            return gestaltCache;
        } else {
            return gestaltCore;
        }
    }

    private void configurePostProcessors() {
        if (postProcessors.isEmpty()) {
            logger.log(TRACE, "No post processors provided, using defaults");
            addDefaultPostProcessors();
        }
        postProcessors = postProcessors.stream().filter(Objects::nonNull).collect(Collectors.toList());

        PostProcessorConfig config = new PostProcessorConfig(gestaltConfig, configNodeService, sentenceLexer, secretConcealer);
        postProcessors.forEach(it -> it.applyConfig(config));
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

    private void configureValidators() {
        // setup the default validators, if there are none add the default ones.
        if (configValidators.isEmpty()) {
            logger.log(TRACE, "No validators recorders provided, using defaults");
            addDefaultValidators();
        }
        configValidators = configValidators.stream().filter(Objects::nonNull).collect(Collectors.toList());
        configValidators.forEach(it -> it.applyConfig(gestaltConfig));

        // if the validationManager does not exist, create it.
        // Otherwise, get all the recorders from the validationManager, combine them with the ones in the builder,
        if (validationManager == null) {
            validationManager = new ValidationManager(configValidators);
        } else {
            validationManager.addValidators(configValidators);
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
        if (observationManager == null) {
            observationManager = new ObservationManager(observationRecorders);
        } else {
            observationManager.addObservationRecorders(observationRecorders);
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

        newConfig.setValidationEnabled(Objects.requireNonNullElseGet(validationEnabled,
            () -> gestaltConfig.isValidationEnabled()));

        newConfig.setSentenceLexer(Objects.requireNonNullElseGet(sentenceLexer,
            () -> gestaltConfig.getSentenceLexer()));

        return newConfig;
    }
}
