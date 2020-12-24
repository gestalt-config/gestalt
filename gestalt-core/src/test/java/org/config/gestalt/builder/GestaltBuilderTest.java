package org.config.gestalt.builder;

import org.config.gestalt.Gestalt;
import org.config.gestalt.decoder.*;
import org.config.gestalt.entity.GestaltConfig;
import org.config.gestalt.exceptions.ConfigurationException;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.PathLexer;
import org.config.gestalt.loader.ConfigLoader;
import org.config.gestalt.loader.ConfigLoaderRegistry;
import org.config.gestalt.loader.MapConfigLoader;
import org.config.gestalt.node.ConfigNodeManager;
import org.config.gestalt.source.ConfigSource;
import org.config.gestalt.source.MapConfigSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

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

        List<Decoder> decoders = new ArrayList<>(Arrays.asList(new StringDecoder(), new DoubleDecoder()));

        builder = builder.setDecoderService(new DecoderRegistry(Arrays.asList(new StringDecoder(), new DoubleDecoder())))
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
            .setConfigNodeService(new ConfigNodeManager());

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

        List<Decoder> decoders = new ArrayList<>(Arrays.asList(new StringDecoder(), new DoubleDecoder()));

        List<ConfigLoader> configLoaders = new ArrayList<>(Collections.singletonList(new MapConfigLoader()));

        Gestalt gestalt =  builder
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
}
