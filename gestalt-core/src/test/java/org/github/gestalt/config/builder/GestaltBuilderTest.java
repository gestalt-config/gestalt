package org.github.gestalt.config.builder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.GestaltCore;
import org.github.gestalt.config.decoder.*;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderRegistry;
import org.github.gestalt.config.loader.MapConfigLoader;
import org.github.gestalt.config.node.ConfigNodeManager;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.DotNotationPathMapper;
import org.github.gestalt.config.path.mapper.PathMapper;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.post.process.transform.EnvironmentVariablesTransformer;
import org.github.gestalt.config.post.process.transform.TransformerPostProcessor;
import org.github.gestalt.config.reload.TimedConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class GestaltBuilderTest {

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
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
            .addPostProcessors(Collections.singletonList(
                new TransformerPostProcessor(Collections.singletonList(new EnvironmentVariablesTransformer()))))
            .setPostProcessors(Collections.singletonList(
                new TransformerPostProcessor(Collections.singletonList(new EnvironmentVariablesTransformer()))))
            .addPathMapper(new StandardPathMapper())
            .addPathMapper(List.of(new DotNotationPathMapper()))
            .setPathMappers(List.of(new StandardPathMapper()));

        Assertions.assertEquals(5, builder.getMaxSubstitutionNestedDepth());
        Assertions.assertEquals(true, builder.isTreatWarningsAsErrors());
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(false);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(false);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder(), new ObjectDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

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
            builder.addPostProcessors(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No PostProcessor provided while adding", e.getMessage());
        }

        try {
            builder.setPostProcessors(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No PostProcessors provided while setting", e.getMessage());
        }
    }

    @Test
    public void buildBadPostProcessorEmpty() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.addPostProcessors(List.of());
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No PostProcessor provided while adding", e.getMessage());
        }

        try {
            builder.setPostProcessors(List.of());
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No PostProcessors provided while setting", e.getMessage());
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
            builder.addPathMapper((List<PathMapper>) null);
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
            builder.addPathMapper(List.of());
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

    private static class CoreReloadListener implements org.github.gestalt.config.reload.CoreReloadListener {

        @Override
        public void reload() {

        }
    }
}

