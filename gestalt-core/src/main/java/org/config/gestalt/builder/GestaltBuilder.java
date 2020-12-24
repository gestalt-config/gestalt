package org.config.gestalt.builder;

import org.config.gestalt.Gestalt;
import org.config.gestalt.decoder.*;
import org.config.gestalt.entity.GestaltConfig;
import org.config.gestalt.exceptions.ConfigurationException;
import org.config.gestalt.lexer.PathLexer;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.loader.*;
import org.config.gestalt.node.ConfigNodeService;
import org.config.gestalt.source.ConfigSource;
import org.config.gestalt.GestaltCore;
import org.config.gestalt.node.ConfigNodeManager;
import org.config.gestalt.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class GestaltBuilder {
    private static final Logger logger = LoggerFactory.getLogger(GestaltBuilder.class.getName());

    private ConfigLoaderService configLoaderService = new ConfigLoaderRegistry();
    private DecoderService decoderService;
    private SentenceLexer sentenceLexer = new PathLexer();
    private GestaltConfig gestaltConfig = new GestaltConfig();
    private ConfigNodeService configNodeService = new ConfigNodeManager();

    private List<ConfigSource> sources = new ArrayList<>();
    private List<Decoder> decoders = new ArrayList<>();
    private List<ConfigLoader> configLoaders = new ArrayList<>();

    public GestaltBuilder addDefaultDecoders() {
        decoders.addAll(Arrays.asList(new ArrayDecoder(), new BooleanDecoder(),
            new ByteDecoder(), new CharDecoder(), new DoubleDecoder(), new EnumDecoder(), new FloatDecoder(), new IntegerDecoder(),
            new ListDecoder(), new LongDecoder(), new ObjectDecoder(), new SetDecoder(), new ShortDecoder(), new StringDecoder()));
        return this;
    }

    public GestaltBuilder addDefaultConfigLoaders() {
        configLoaders.addAll(Arrays.asList(new MapConfigLoader(), new PropertyLoader(), new EnvironmentVarsLoader()));
        return this;
    }

    public GestaltBuilder setSources(List<ConfigSource> sources) throws ConfigurationException {
        if (sources == null || sources.isEmpty()) {
            throw new ConfigurationException("No sources provided while setting sources");
        }
        this.sources = sources;

        return this;
    }

    public GestaltBuilder addSources(List<ConfigSource> sources) throws ConfigurationException {
        if (sources == null || sources.isEmpty()) {
            throw new ConfigurationException("No sources provided while adding sources");
        }
        this.sources.addAll(sources);

        return this;
    }

    public GestaltBuilder addSource(ConfigSource source) {
        Objects.requireNonNull(source, "Source should not be null");
        this.sources.add(source);
        return this;
    }

    public GestaltBuilder setConfigLoaderService(ConfigLoaderService configLoaderService) {
        Objects.requireNonNull(configLoaderService, "ConfigLoaderRegistry should not be null");
        this.configLoaderService = configLoaderService;
        return this;
    }

    public GestaltBuilder setConfigLoaders(List<ConfigLoader> configLoaders) throws ConfigurationException {
        if (configLoaders == null || configLoaders.isEmpty()) {
            throw new ConfigurationException("No config loader provided while setting config loaders");
        }
        this.configLoaders = configLoaders;
        return this;
    }


    public GestaltBuilder addConfigLoaders(List<ConfigLoader> configLoaders) throws ConfigurationException {
        if (configLoaders == null || configLoaders.isEmpty()) {
            throw new ConfigurationException("No config loader provided while adding config loaders");
        }
        this.configLoaders.addAll(configLoaders);
        return this;
    }

    public GestaltBuilder addConfigLoader(ConfigLoader configLoader) {
        Objects.requireNonNull(configLoader, "ConfigLoader should not be null");
        this.configLoaders.add(configLoader);
        return this;
    }

    public GestaltBuilder setSentenceLexer(SentenceLexer sentenceLexer) {
        Objects.requireNonNull(sentenceLexer, "SentenceLexer should not be null");
        this.sentenceLexer = sentenceLexer;
        return this;
    }

    public GestaltBuilder setGestaltConfig(GestaltConfig gestaltConfig) {
        Objects.requireNonNull(gestaltConfig, "GestaltConfig should not be null");
        this.gestaltConfig = gestaltConfig;
        return this;
    }

    public GestaltBuilder setConfigNodeService(ConfigNodeManager configNodeService) {
        Objects.requireNonNull(configNodeService, "ConfigNodeManager should not be null");
        this.configNodeService = configNodeService;
        return this;
    }

    public GestaltBuilder setDecoderService(DecoderService decoderService) {
        Objects.requireNonNull(decoderService, "DecoderService should not be null");
        this.decoderService = decoderService;
        decoderService.addDecoders(decoders);
        return this;
    }

    public GestaltBuilder setDecoders(List<Decoder> decoders) throws ConfigurationException {
        if (decoders == null || decoders.isEmpty()) {
            throw new ConfigurationException("No decoders provided while setting decoders");
        }
        this.decoders = decoders;
        return this;
    }

    public GestaltBuilder addDecoders(List<Decoder> decoders) throws ConfigurationException {
        if (decoders == null || decoders.isEmpty()) {
            throw new ConfigurationException("No decoders provided while adding decoders");
        }
        this.decoders.addAll(decoders);
        return this;
    }

    public GestaltBuilder addDecoder(Decoder decoder) {
        Objects.requireNonNull(decoder, "Decoder should not be null");
        this.decoders.add(decoder);
        return this;
    }


    public GestaltBuilder setTreatWarningsAsErrors(boolean warningsAsErrors) {
        gestaltConfig.setTreatWarningsAsErrors(warningsAsErrors);
        return this;
    }

    protected List<Decoder> dedupeDecoders() {
        Map<String, List<Decoder>> decoderMap = decoders
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

    public Gestalt build() throws ConfigurationException {
        if (sources.isEmpty()) {
            throw new ConfigurationException("No sources provided");
        }

        // setup the decoders, if there are none, add the default ones.
        if (decoders.isEmpty()) {
            logger.debug("No decoders provided, using defaults");
            addDefaultDecoders();
        }

        // if the decoderService does not exist, create it.
        // Otherwise get all the decoders from the decoderService, combine them with the ones in the builder,
        // and update the decoderService
        if (decoderService == null) {
            decoderService = new DecoderRegistry(decoders);
        } else {
            decoders.addAll(decoderService.getDecoders());
            List<Decoder> dedupedDecoders = dedupeDecoders();
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


        return new GestaltCore(configLoaderService, sources, decoderService, sentenceLexer, gestaltConfig, configNodeService);
    }
}
