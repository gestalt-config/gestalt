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
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.post.process.transform.EnvironmentVariablesTransformer;
import org.github.gestalt.config.post.process.transform.TransformerPostProcessor;
import org.github.gestalt.config.reload.TimedConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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

        List<ConfigSource> sources = new ArrayList<>();
        sources.add(new MapConfigSource(configs));

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
                         .addSources(sources)
                         .addSource(new MapConfigSource(configs))
                         .addSource(new MapConfigSource(configs2))
                         .setSentenceLexer(new PathLexer())
                         .setConfigNodeService(configNodeManager)
                         .addCoreReloadListener(coreReloadListener)
                         .addReloadStrategy(new TimedConfigReloadStrategy(sources.get(0), Duration.ofMillis(100)))
                         .addPostProcessors(Collections.singletonList(
                             new TransformerPostProcessor(Collections.singletonList(new EnvironmentVariablesTransformer()))))
                         .setPostProcessors(Collections.singletonList(
                             new TransformerPostProcessor(Collections.singletonList(new EnvironmentVariablesTransformer()))))
                         .addPathMapper(new StandardPathMapper())
                         .addPathMapper(List.of(new DotNotationPathMapper()))
                         .setPathMappers(List.of(new StandardPathMapper()));

        Gestalt gestalt = builder.build();
        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
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

        List<ConfigSource> sources = new ArrayList<>();
        sources.add(new MapConfigSource(configs));
        sources.add(new MapConfigSource(configs2));

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
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

        List<ConfigSource> sources = new ArrayList<>();
        sources.add(new MapConfigSource(configs));
        sources.add(new MapConfigSource(configs2));

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
            .addSources(List.of(new MapConfigSource(configs)))
            .setTreatWarningsAsErrors(false)
            .setTreatMissingArrayIndexAsError(false)
            .setTreatMissingValuesAsErrors(false)
            .setTreatNullValuesInClassAsErrors(true)
            .build();

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("db", DBInfo.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                         .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfo\n" +
                             " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: ObjectToken, " +
                             "during navigating to next node\n" +
                             " - level: ERROR, message: Decoding object : DBInfo on path: db.uri, field uri results in null value\n" +
                             " - level: MISSING_VALUE, message: Unable to find node matching path: db.password, for class: ObjectToken, " +
                             "during navigating to next node\n" +
                             " - level: ERROR, message: Decoding object : DBInfo on path: db.password, " +
                             "field password results in null value");
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
        config.setTreatNullValuesInClassAsErrors(false);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder(), new ObjectDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, Collections.emptyList());

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

        List<ConfigSource> sources = new ArrayList<>();
        sources.add(new MapConfigSource(configs));
        sources.add(new MapConfigSource(configs2));

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .addDefaultDecoders()
            .addDefaultConfigLoaders()
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
    }

    @Test
    public void buildDefaultWithCacheFalse() throws GestaltException {
        ConfigNodeManager configNodeService = Mockito.mock(ConfigNodeManager.class);

        Mockito.when(configNodeService.navigateToNode(any(), any(), any())).thenReturn(ValidateOf.valid(new LeafNode("value")));

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

        List<ConfigSource> sources = new ArrayList<>();
        sources.add(new MapConfigSource(configs));
        sources.add(new MapConfigSource(configs2));

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

        Mockito.when(configNodeService.navigateToNode(any(), any(), any())).thenReturn(ValidateOf.valid(new LeafNode("value")));

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

        List<ConfigSource> sources = new ArrayList<>();
        sources.add(new MapConfigSource(configs));
        sources.add(new MapConfigSource(configs2));

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .setConfigNodeService(configNodeService)
            .addSources(sources)
            .addDefaultDecoders()
            .addDefaultConfigLoaders()
            .useCacheDecorator(true)
            .build();

        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));

        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNode(any(), any(), any());
    }

    @Test
    @Disabled
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

        List<ConfigSource> sources = new ArrayList<>();
        sources.add(new MapConfigSource(configs));
        sources.add(new MapConfigSource(configs2));

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .setSubstitutionClosingToken("*(")
            .setSubstitutionClosingToken(")")
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

        List<ConfigSource> sources = new ArrayList<>();
        sources.add(new MapConfigSource(configs));
        sources.add(new MapConfigSource(configs2));

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
            .addSource(new MapConfigSource(configs2))
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
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No sources provided while adding sources", e.getMessage());
        }

        try {
            builder.setSources(null);
            Assertions.fail("Should not hit this");
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No sources provided while setting sources", e.getMessage());
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
    public void buildNoSources() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.build();
        } catch (GestaltConfigurationException e) {
            Assertions.assertEquals("No sources provided", e.getMessage());
        }
    }

    private static class CoreReloadListener implements org.github.gestalt.config.reload.CoreReloadListener {

        @Override
        public void reload() {

        }
    }
}
