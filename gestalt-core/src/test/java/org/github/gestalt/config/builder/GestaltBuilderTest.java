package org.github.gestalt.config.builder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.decoder.*;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.ConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderRegistry;
import org.github.gestalt.config.loader.MapConfigLoader;
import org.github.gestalt.config.node.ConfigNodeManager;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reload.TimedConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class GestaltBuilderTest {

    @Test
    public void build() throws GestaltException {
        GestaltBuilder builder = new GestaltBuilder();

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

        List<Decoder<?>> decoders = new ArrayList<>(Arrays.asList(new StringDecoder(), new DoubleDecoder()));

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer();

        CoreReloadListener coreReloadListener = new CoreReloadListener();

        builder = builder.setDecoderService(new DecoderRegistry(Arrays.asList(new StringDecoder(), new DoubleDecoder()),
            configNodeManager, lexer))
            .setDecoders(decoders)
            .addDecoder(new LongDecoder())
            .setTreatWarningsAsErrors(true)
            .setGestaltConfig(new GestaltConfig())
            .setConfigLoaderService(new ConfigLoaderRegistry())
            .addConfigLoader(new MapConfigLoader())
            .addSources(sources)
            .addSource(new MapConfigSource(configs))
            .addSource(new MapConfigSource(configs2))
            .setSentenceLexer(new PathLexer())
            .setConfigNodeService(configNodeManager)
            .addCoreReloadListener(coreReloadListener)
            .addReloadStrategy(new TimedConfigReloadStrategy(sources.get(0), Duration.ofMillis(100)));

        Gestalt gestalt = builder.build();
        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
    }

    @Test
    public void buildDefault() throws GestaltException {
        GestaltBuilder builder = new GestaltBuilder();

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

        Gestalt gestalt = builder
            .addSources(sources)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
    }

    @Test
    public void buildTreatErrorsAsWarnings() throws GestaltException {
        GestaltBuilder builder = new GestaltBuilder();

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

        Gestalt gestalt = builder
            .addSources(sources)
            .setEnvVarsTreatErrorsAsWarnings(true)
            .setTreatMissingArrayIndexAsError(true)
            .setTreatMissingValuesAsErrors(true)
            .setDateDecoderFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .setLocalDateFormat("yyyy-MM-dd")
            .setLocalDateTimeFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .build();

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("admin[1]", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: admin[1], for class: java.lang.String\n" +
                    " - level: ERROR, message: Unable to find array node for path: admin[1], at token: ArrayToken");
        }
    }

    @Test
    public void buildDefaultWithBuilder() throws GestaltException {
        GestaltBuilder builder = new GestaltBuilder();

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
        GestaltBuilder builder = new GestaltBuilder();

        ConfigNodeManager configNodeService = Mockito.mock(ConfigNodeManager.class);

        Mockito.when(configNodeService.navigateToNode(any(), any())).thenReturn(ValidateOf.valid(new LeafNode("value")));

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

        Gestalt gestalt = builder
            .setConfigNodeService(configNodeService)
            .addSources(sources)
            .addDefaultDecoders()
            .addDefaultConfigLoaders()
            .useCacheDecorator(false)
            .build();

        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));

        Mockito.verify(configNodeService, Mockito.times(2)).navigateToNode(any(), any());
    }

    @Test
    public void buildDefaultWithCacheTrue() throws GestaltException {
        GestaltBuilder builder = new GestaltBuilder();

        ConfigNodeManager configNodeService = Mockito.mock(ConfigNodeManager.class);

        Mockito.when(configNodeService.navigateToNode(any(), any())).thenReturn(ValidateOf.valid(new LeafNode("value")));

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

        Gestalt gestalt = builder
            .setConfigNodeService(configNodeService)
            .addSources(sources)
            .addDefaultDecoders()
            .addDefaultConfigLoaders()
            .useCacheDecorator(true)
            .build();

        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("value", gestalt.getConfig("db.password", String.class));

        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNode(any(), any());
    }

    @Test
    public void buildDuplicates() throws GestaltException {
        GestaltBuilder builder = new GestaltBuilder();

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

        List<Decoder<?>> decoders = new ArrayList<>(Arrays.asList(new StringDecoder(), new DoubleDecoder()));

        List<ConfigLoader> configLoaders = new ArrayList<>(Collections.singletonList(new MapConfigLoader()));

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
        } catch (ConfigurationException e) {
            Assertions.assertEquals("No sources provided while adding sources", e.getMessage());
        }

        try {
            builder.setSources(null);
            Assertions.fail("Should not hit this");
        } catch (ConfigurationException e) {
            Assertions.assertEquals("No sources provided while setting sources", e.getMessage());
        }
    }

    @Test
    public void buildBadDecoders() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.addDecoders(null);
            Assertions.fail("Should not hit this");
        } catch (ConfigurationException e) {
            Assertions.assertEquals("No decoders provided while adding decoders", e.getMessage());
        }

        try {
            builder.setDecoders(null);
            Assertions.fail("Should not hit this");
        } catch (ConfigurationException e) {
            Assertions.assertEquals("No decoders provided while setting decoders", e.getMessage());
        }
    }

    @Test
    public void buildBadConfigLoaders() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.addConfigLoaders(null);
            Assertions.fail("Should not hit this");
        } catch (ConfigurationException e) {
            Assertions.assertEquals("No config loader provided while adding config loaders", e.getMessage());
        }

        try {
            builder.setConfigLoaders(null);
            Assertions.fail("Should not hit this");
        } catch (ConfigurationException e) {
            Assertions.assertEquals("No config loader provided while setting config loaders", e.getMessage());
        }
    }

    @Test
    public void buildNoSources() {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            builder.build();
        } catch (ConfigurationException e) {
            Assertions.assertEquals("No sources provided", e.getMessage());
        }
    }

    private static class CoreReloadListener implements org.github.gestalt.config.reload.CoreReloadListener {

        @Override
        public void reload() {

        }
    }
}
