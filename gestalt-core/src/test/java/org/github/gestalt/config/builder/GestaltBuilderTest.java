package org.github.gestalt.config.builder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.decoder.*;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderRegistry;
import org.github.gestalt.config.loader.MapConfigLoader;
import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.github.gestalt.config.node.TagMergingStrategyFallback;
import org.github.gestalt.config.observations.ObservationManager;
import org.github.gestalt.config.observations.TestObservationRecorder;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeManager;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.DotNotationPathMapper;
import org.github.gestalt.config.path.mapper.PathMapper;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.processor.TestValidationProcessor;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorManager;
import org.github.gestalt.config.processor.config.RunTimeConfigNodeProcessor;
import org.github.gestalt.config.processor.config.transform.EnvironmentVariablesTransformer;
import org.github.gestalt.config.processor.config.transform.LoadtimeStringSubstitutionConfigNodeProcessor;
import org.github.gestalt.config.processor.config.transform.RunTimeStringSubstitutionConfigNodeProcessor;
import org.github.gestalt.config.processor.result.DefaultResultProcessor;
import org.github.gestalt.config.processor.result.ErrorResultProcessor;
import org.github.gestalt.config.processor.result.ResultsProcessorManager;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.TimedConfigReloadStrategy;
import org.github.gestalt.config.secret.rules.RegexSecretChecker;
import org.github.gestalt.config.secret.rules.SecretConcealerManager;
import org.github.gestalt.config.security.temporary.TemporarySecretModule;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.node.factory.ClassPathConfigNodeFactory;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryManager;
import org.github.gestalt.config.node.factory.FileConfigNodeFactory;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.processor.TestResultProcessor;
import org.github.gestalt.config.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class GestaltBuilderTest {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = GestaltBuilderTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    public void build() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<Decoder<?>> decoders = new ArrayList<>(List.of(new StringDecoder(), new DoubleDecoder()));

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer();

        CoreReloadListener coreReloadListener = new CoreReloadListener();

        GestaltBuilder builder = new GestaltBuilder();
        builder = builder.setDecoderService(new DecoderRegistry(List.of(new StringDecoder(), new DoubleDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())))
            .setDecoders(decoders)
            .addDecoder(new LongDecoder())
            .setTreatWarningsAsErrors(true)
            .setLogLevelForMissingValuesWhenDefaultOrOptional(System.Logger.Level.DEBUG)
            .setSubstitutionOpeningToken("${")
            .setSubstitutionClosingToken("}")
            .setRunTimeSubstitutionOpeningToken("${")
            .setRunTimeSubstitutionClosingToken("}")
            .setAnnotationOpeningToken("@{")
            .setAnnotationClosingToken("}")
            .setAnnotationRegex("^(?<annotation>\\w+):?(?<key>.+?)?$")
            .setAnnotationTrimWhiteSpace(true)
            .setMaxSubstitutionNestedDepth(5)
            .setSubstitutionRegex("")
            .setGestaltConfig(new GestaltConfig())
            .setConfigLoaderService(new ConfigLoaderRegistry())
            .addConfigLoader(new MapConfigLoader())
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(new TimedConfigReloadStrategy(Duration.ofMillis(100))).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build())
            .setSentenceLexer(new PathLexer())
            .setConfigNodeService(configNodeManager)
            .addCoreReloadListener(coreReloadListener)
            .addCoreReloadListener(List.of())
            .addConfigNodeProcessors(Collections.singletonList(
                new LoadtimeStringSubstitutionConfigNodeProcessor(Collections.singletonList(new EnvironmentVariablesTransformer()))))
            .setConfigNodeProcessors(Collections.singletonList(
                new LoadtimeStringSubstitutionConfigNodeProcessor(Collections.singletonList(new EnvironmentVariablesTransformer()))))
            .addRunTimeConfigNodeProcessors(Collections.singletonList(
                new RunTimeStringSubstitutionConfigNodeProcessor(Collections.singletonList(new EnvironmentVariablesTransformer()))))
            .setRunTimeConfigNodeProcessors(Collections.singletonList(
                new RunTimeStringSubstitutionConfigNodeProcessor(Collections.singletonList(new EnvironmentVariablesTransformer()))))
            .addRunTimeConfigNodeProcessor(
                new RunTimeStringSubstitutionConfigNodeProcessor(Collections.singletonList(new EnvironmentVariablesTransformer())))
            .addPathMapper(new StandardPathMapper())
            .addPathMappers(List.of(new DotNotationPathMapper()))
            .setPathMappers(List.of(new StandardPathMapper()))
            .setObservationsEnabled(true)
            .addObservationsRecorder(new TestObservationRecorder(0))
            .addObservationsRecorders(List.of(new TestObservationRecorder(1)))
            .setObservationsRecorders(List.of(new TestObservationRecorder(0), new TestObservationRecorder(1)))
            .setObservationsService(new ObservationManager(List.of()))
            .addResultProcessor(new TestResultProcessor(true))
            .addResultProcessors(List.of(new TestResultProcessor(true)))
            .setResultProcessor(List.of(new TestResultProcessor(true)))
            .setResultsProcessorService(new ResultsProcessorManager(new ArrayList<>()))
            .setConfigNodeProcessorService(new ConfigNodeProcessorManager(List.of(), List.of(), new PathLexer()))
            .setConfigSourceFactoryService(new ConfigNodeFactoryManager(List.of()))
            .setConfigSourceFactories(List.of(new FileConfigNodeFactory()))
            .addConfigSourceFactory(new ClassPathConfigNodeFactory())
            .addConfigSourceFactories(List.of(new FileConfigNodeFactory()))
            .setAddCoreResultProcessors(true)
            .setTagMergingStrategy(new TagMergingStrategyFallback())
            .addValidator(new TestValidationProcessor(true))
            .addValidators(List.of(new TestValidationProcessor(true)))
            .setValidators(List.of(new TestValidationProcessor(true)))
            .setSecurityMaskingRule(new HashSet<>())
            .addSecurityMaskingRule("secret")
            .setSecurityMask("&&&&")
            .setSecretConcealer(new SecretConcealerManager(Set.of(), it -> "****"))
            .addTemporaryNodeAccessCount("secret")
            .addTemporaryNodeAccessCount("secret", 1)
            .addTemporaryNodeAccessCount(Set.of("cert"), 2)
            .setTemporaryNodeAccessCount(List.of(new Pair<>(new RegexSecretChecker(Set.of("password")), 1)))
            .addEncryptedSecret("cert")
            .setEncryptedSecrets(new RegexSecretChecker(Set.of("cert")))
            .setTreatWarningsAsErrors(true)
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setNodeIncludeKeyword("$include")
            .setNodeNestedIncludeLimit(10)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE);

        Assertions.assertEquals(5, builder.getMaxSubstitutionNestedDepth());
        Assertions.assertEquals(true, builder.isTreatWarningsAsErrors());
        Assertions.assertEquals(true, builder.getTreatMissingValuesAsErrors());
        Assertions.assertEquals(true, builder.getTreatMissingDiscretionaryValuesAsErrors());
        Assertions.assertEquals("", builder.getSubstitutionRegex());
        Assertions.assertEquals(ProxyDecoderMode.CACHE, builder.getProxyDecoderMode());
        Assertions.assertEquals(System.Logger.Level.DEBUG, builder.getLogLevelForMissingValuesWhenDefaultOrOptional());
        Assertions.assertEquals(Tags.of(), builder.getDefaultTags());

        Gestalt gestalt = builder.build();
        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
    }

    @Test
    public void buildDefault() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
    }

    @Test
    @SuppressWarnings({"deprecation"})
    public void buildSourcePackageAndSource() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());

        //sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());
        ConfigSource configSource = new MapConfigSource(configs2);

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .addSource(configSource)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void buildSourceAndSourcePackage() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());

        //sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());
        ConfigSource configSource = new MapConfigSource(configs2);

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(configSource)
            .addSources(sources)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
    }

    @Test
    public void buildTreatErrorsAsWarnings() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[2]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .setTreatMissingArrayIndexAsError(true)
            .setTreatMissingValuesAsErrors(true)
            .setDateDecoderFormat(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
            .setLocalDateFormat(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .setLocalDateTimeFormat(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
            .build();

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("admin[1]", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: admin[1], for class: java.lang.String\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: admin[1], for class: ArrayToken, " +
                    "during navigating to next node");
        }
    }

    @Test
    public void testNullResultsForNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()))
            .setTreatWarningsAsErrors(false)
            .setTreatMissingArrayIndexAsError(false)
            .build();

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("db", DBInfo.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfo\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfo, " +
                    "during object decoding\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: db.password, for class: DBInfo, " +
                    "during object decoding");
        }
    }

    @Test
    public void testNullResultsForNodeIgnored() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(false);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(false);

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()))
            .setGestaltConfig(config)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfo dbInfo = gestalt.getConfig("db", DBInfo.class);
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(3306, dbInfo.getPort());
            Assertions.assertNull(dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void buildDefaultWithBuilder() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .setSources(sources)
            .addDefaultDecoders()
            .addDefaultConfigLoaders()
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
    }

    @Test
    public void buildDefaultWithCacheFalse() throws GestaltException {
        ConfigNodeManager configNodeService = Mockito.mock(ConfigNodeManager.class);

        Mockito.when(configNodeService.navigateToNode(any(), any(), any())).thenReturn(GResultOf.result(new LeafNode("value")));

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .setConfigNodeService(configNodeService)
            .addSources(sources)
            .addDefaultDecoders()
            .addDefaultConfigLoaders()
            .useCacheDecorator(false)
            .build();

        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));

        Mockito.verify(configNodeService, Mockito.times(2)).navigateToNode(any(), any(), any());
    }

    @Test
    public void buildDefaultWithCacheTrue() throws GestaltException {
        ConfigNodeManager configNodeService = Mockito.mock(ConfigNodeManager.class);

        Mockito.when(configNodeService.navigateToNode(any(), any(), any())).thenReturn(GResultOf.result(new LeafNode("value")));

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .setConfigNodeService(configNodeService)
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build())
            .addDefaultDecoders()
            .addDefaultConfigLoaders()
            .useCacheDecorator(true)
            .build();

        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));

        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNode(any(), any(), any());
    }

    @Test
    public void buildDifferentStringSubstitutions() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "*(db.name)");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .setSubstitutionOpeningToken("*(")
            .setSubstitutionClosingToken(")")
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class));
    }

    @Test
    public void buildDifferentAnnotationTags() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "password&(secret)");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .setAnnotationOpeningToken("&(")
            .setAnnotationClosingToken(")")
            .build();

        gestalt.loadConfigs();

        var result = gestalt.getConfigResult("db.password", TypeCapture.of(String.class), Tags.of());
        Assertions.assertEquals("password", result.results());
        Assertions.assertEquals(1, result.getMetadata().size());
        Assertions.assertEquals(true, result.getMetadata().containsKey(IsSecretMetadata.SECRET));
    }

    @Test
    public void buildDefaultTags() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).setTags(Tags.profile("test")).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.profile("test")).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .setDefaultTags(Tags.profile("test"))
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
    }

    @Test
    public void buildDuplicates() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        List<Decoder<?>> decoders = new ArrayList<>(List.of(new StringDecoder(), new DoubleDecoder()));

        List<ConfigLoader> configLoaders = new ArrayList<>(Collections.singletonList(new MapConfigLoader()));

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .setDecoders(decoders)
            .addDecoders(decoders)
            .addDecoder(new LongDecoder())
            .setTreatWarningsAsErrors(true)
            .setGestaltConfig(new GestaltConfig())
            .setConfigLoaderService(new ConfigLoaderRegistry())
            .setConfigLoaders(configLoaders)
            .addConfigLoaders(configLoaders)
            .addConfigLoader(new MapConfigLoader())
            .setSources(sources)
            .addSources(sources)
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build())
            .setSentenceLexer(new PathLexer())
            .setConfigNodeService(new ConfigNodeManager())
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
    }

    @Test
    public void buildBadSources() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.addSources(null);
            Assertions.fail("Should not hit this");
        } catch (Exception e) {
            Assertions.assertEquals("ConfigSourcePackage should not be null", e.getMessage());
        }

        try {
            builder.setSources(null);
            Assertions.fail("Should not hit this");
        } catch (Exception e) {
            Assertions.assertEquals("ConfigSourcePackage should not be null", e.getMessage());
        }
    }

    @Test
    public void buildBadDecoders() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.addDecoders(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No decoders provided while adding decoders", e.getMessage());
        }

        try {
            builder.setDecoders(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No decoders provided while setting decoders", e.getMessage());
        }
    }

    @Test
    public void buildBadPostProcessor() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.addConfigNodeProcessors(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No ConfigNodeProcessor provided while adding", e.getMessage());
        }

        try {
            builder.setConfigNodeProcessors(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No ConfigNodeProcessor provided while setting", e.getMessage());
        }
    }

    @Test
    public void buildBadPostProcessorEmpty() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.addConfigNodeProcessors(List.of());
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No ConfigNodeProcessor provided while adding", e.getMessage());
        }

        try {
            builder.setConfigNodeProcessors(List.of());
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No ConfigNodeProcessor provided while setting", e.getMessage());
        }
    }

    @Test
    public void buildBadConfigLoaders() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.addConfigLoaders(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No config loader provided while adding config loaders", e.getMessage());
        }

        try {
            builder.setConfigLoaders(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No config loader provided while setting config loaders", e.getMessage());
        }
    }

    @Test
    public void buildBadConfigLoadersEmpty() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.addConfigLoaders(List.of());
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No config loader provided while adding config loaders", e.getMessage());
        }

        try {
            builder.setConfigLoaders(List.of());
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No config loader provided while setting config loaders", e.getMessage());
        }
    }

    @Test
    public void buildBadPathMappers() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.setPathMappers(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No PathMappers provided while setting", e.getMessage());
        }

        try {
            builder.addPathMappers((List<PathMapper>) null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No PathMapper provided while adding", e.getMessage());
        }
    }

    @Test
    public void buildBadPathMappersEmpty() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.setPathMappers(List.of());
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No PathMappers provided while setting", e.getMessage());
        }

        try {
            builder.addPathMappers(List.of());
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No PathMapper provided while adding", e.getMessage());
        }
    }

    @Test
    public void buildNoSources() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.build();
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No sources provided", e.getMessage());
        }
    }

    @SuppressWarnings("removal")
    @Test
    public void codeCoverage() {
        GestaltBuilder builder = new GestaltBuilder();
        builder.setTreatNullValuesInClassAsErrors(false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void manuallyAddedDecoderConfig() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        var decoder1 = new TestDecoder();
        var decoder2 = new TestDecoder();
        var decoder3 = new TestDecoder();


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addDefaultDecoders()
            .addDecoder(decoder1)
            .addDecoders(List.of(decoder2, decoder3))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(1, decoder1.configCount);
        Assertions.assertEquals(1, decoder2.configCount);
        Assertions.assertEquals(1, decoder3.configCount);
    }

    @Test
    public void manuallyAddedConfigLoaderConfig() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        var loader1 = new TestConfigLoader();
        var loader2 = new TestConfigLoader();
        var loader3 = new TestConfigLoader();


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addDefaultConfigLoaders()
            .addConfigLoader(loader1)
            .addConfigLoaders(List.of(loader2, loader3))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(1, loader1.configCount);
        Assertions.assertEquals(1, loader2.configCount);
        Assertions.assertEquals(1, loader3.configCount);
    }

    @Test
    public void manuallyAddedPostProcessorConfig() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        var processor1 = new TestConfigNodeProcessor();
        var processor2 = new TestConfigNodeProcessor();
        var processor3 = new TestConfigNodeProcessor();


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addDefaultPostProcessors()
            .addConfigNodeProcessor(processor1)
            .addConfigNodeProcessors(List.of(processor2, processor3))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(1, processor1.configCount);
        Assertions.assertEquals(1, processor2.configCount);
        Assertions.assertEquals(1, processor3.configCount);
    }

    @Test
    public void manuallyAddedRunTimePostProcessorConfig() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        var processor1 = new TestRunTimeConfigNodeProcessor();
        var processor2 = new TestRunTimeConfigNodeProcessor();
        var processor3 = new TestRunTimeConfigNodeProcessor();


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addDefaultPostProcessors()
            .addRunTimeConfigNodeProcessor(processor1)
            .addRunTimeConfigNodeProcessors(List.of(processor2, processor3))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(1, processor1.configCount);
        Assertions.assertEquals(1, processor2.configCount);
        Assertions.assertEquals(1, processor3.configCount);
    }

    @Test
    public void manuallyAddedPathMapperConfig() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        var mapper1 = new TestPathMapper();
        var mapper2 = new TestPathMapper();

        var mapper3 = new TestPathMapper();


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addDefaultPathMappers()
            .addPathMapper(mapper1)
            .addPathMappers(List.of(mapper2, mapper3))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(1, mapper1.configCount);
        Assertions.assertEquals(1, mapper2.configCount);
        Assertions.assertEquals(1, mapper3.configCount);
    }

    @Test
    public void buildWithResultProcessorManager() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .setResultsProcessorService(new ResultsProcessorManager(List.of(new ErrorResultProcessor(), new DefaultResultProcessor())))
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
    }

    @Test
    public void buildWithNoResultProcessorManager() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .setAddCoreResultProcessors(false)
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertNull(gestalt.getConfig("db.none", "default", String.class));
    }

    @Test
    public void temporaryNode() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .addTemporaryNodeAccessCount("password", 1)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));

        Assertions.assertEquals("", gestalt.getConfigOptional("db.password", String.class).get());
    }

    @Test
    public void buildModuleAlreadyRegisteredSecrets() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .addTemporaryNodeAccessCount("cert")
            .addModuleConfig(new TemporarySecretModule(List.of(new Pair<>(new RegexSecretChecker(Set.of("password")), 10))))
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
    }

    @Test
    public void everythingTemporarySecrets() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .addTemporaryNodeAccessCount(".*", 100)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
    }

    private static final class TestDecoder implements Decoder {

        public int configCount = 0;

        @Override
        public Priority priority() {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public void applyConfig(GestaltConfig config) {
            configCount++;
        }

        @Override
        public GResultOf decode(String path, Tags tags, ConfigNode node, TypeCapture type, DecoderContext decoderContext) {
            return null;
        }

        @Override
        public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture type) {
            return false;
        }
    }

    private static final class TestConfigLoader implements ConfigLoader {

        public int configCount = 0;

        @Override
        public String name() {
            return "Loader" + new Random().nextInt();
        }

        @Override
        public void applyConfig(GestaltConfig config) {
            configCount++;
        }

        @Override
        public boolean accepts(String format) {
            return false;
        }

        @Override
        public GResultOf<List<ConfigNodeContainer>> loadSource(ConfigSourcePackage source) throws GestaltException {
            return null;
        }
    }

    private static final class TestConfigNodeProcessor implements ConfigNodeProcessor {

        public int configCount = 0;

        @Override
        public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
            return GResultOf.result(currentNode);
        }

        @Override
        public void applyConfig(ConfigNodeProcessorConfig config) {
            configCount++;
        }
    }

    private static final class TestRunTimeConfigNodeProcessor implements RunTimeConfigNodeProcessor {

        public int configCount = 0;

        @Override
        public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
            return GResultOf.result(currentNode);
        }

        @Override
        public void applyConfig(ConfigNodeProcessorConfig config) {
            configCount++;
        }
    }

    private static final class TestPathMapper implements PathMapper {
        public int configCount = 0;

        @Override
        public void applyConfig(GestaltConfig config) {
            configCount++;
        }

        @Override
        public GResultOf<List<Token>> map(String path, String sentence, SentenceLexer lexer) {
            return null;
        }
    }

    private static final class CoreReloadListener implements org.github.gestalt.config.reload.CoreReloadListener {

        @Override
        public void reload() {

        }
    }
}


