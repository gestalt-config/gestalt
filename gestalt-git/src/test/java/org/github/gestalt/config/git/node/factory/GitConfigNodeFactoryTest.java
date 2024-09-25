package org.github.gestalt.config.git.node.factory;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.git.config.GitModuleConfig;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryConfig;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GitConfigNodeFactoryTest {

    public static final String RESOURCES_DEFAULT_PROPERTIES = "gestalt-git/src/test/resources/default.properties";
    public static final String GIT_URL = "https://github.com/gestalt-config/gestalt.git";
    private GitConfigNodeFactory factory;
    private ConfigLoaderService configLoaderService;
    private ConfigLoader configLoader;
    private GitModuleConfig gitModuleConfig;
    private CredentialsProvider credentialsProvider;
    private SshSessionFactory sshSessionFactory;
    private GestaltConfig gestaltConfig;

    private Path configDirectory;

    @BeforeEach
    void setUp() throws IOException {
        configDirectory = Files.createTempDirectory("gitConfigTest");
        configDirectory.toFile().deleteOnExit();

        factory = new GitConfigNodeFactory();
        configLoaderService = mock();
        configLoader = mock();
        gitModuleConfig = mock();
        credentialsProvider = mock();
        sshSessionFactory = mock();
        gestaltConfig = mock();
    }

    @Test
    void testSupportsType() {
        assertTrue(factory.supportsType("git"));
        assertFalse(factory.supportsType("other"));
    }

    @Test
    void testApplyConfigWithModuleConfig() {
        when(gitModuleConfig.hasSshSessionFactory()).thenReturn(true);
        when(gitModuleConfig.getSshSessionFactory()).thenReturn(sshSessionFactory);
        when(gitModuleConfig.hasCredentialsProvider()).thenReturn(true);
        when(gitModuleConfig.getCredentials()).thenReturn(credentialsProvider);
        when(gestaltConfig.getModuleConfig(any())).thenReturn(gitModuleConfig);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));

        assertNotNull(factory);
    }

    @Test
    void testBuildWithValidParams() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("repoURI", GIT_URL);
        params.put("branch", "main");
        params.put("configFilePath", RESOURCES_DEFAULT_PROPERTIES);
        params.put("localRepoDirectory", configDirectory.toFile().getAbsolutePath());

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("config", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));
        when(gestaltConfig.getModuleConfig(any())).thenReturn(gitModuleConfig);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));

        GResultOf<List<ConfigNode>> result = factory.build(params);

        assertTrue(result.hasResults());
        assertFalse(result.hasErrors());
        assertNotNull(result.results());
        assertEquals("data", result.results().get(0).getKey("config").get().getValue().get());
    }

    @Test
    void testBuildWithMissingRepoURI() {
        Map<String, String> params = new HashMap<>();
        params.put("branch", "main");
        params.put("configFilePath", RESOURCES_DEFAULT_PROPERTIES);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));

        GResultOf<List<ConfigNode>> result = factory.build(params);

        assertFalse(result.hasResults());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertInstanceOf(ValidationError.ConfigSourceFactoryException.class, result.getErrors().get(0));
    }

    @Test
    void testBuildWithUnknownParameter() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("unknown", "test");
        params.put("repoURI", GIT_URL);
        params.put("branch", "main");
        params.put("configFilePath", RESOURCES_DEFAULT_PROPERTIES);
        params.put("localRepoDirectory", configDirectory.toFile().getAbsolutePath());

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("config", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));
        when(gestaltConfig.getModuleConfig(any())).thenReturn(gitModuleConfig);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));

        GResultOf<List<ConfigNode>> result = factory.build(params);

        assertTrue(result.hasResults());
        assertTrue(result.hasErrors());
        assertNotNull(result.results());
        assertEquals("data", result.results().get(0).getKey("config").get().getValue().get());
        assertEquals(1, result.getErrors().size());
        assertInstanceOf(ValidationError.ConfigSourceFactoryUnknownParameter.class, result.getErrors().get(0));

        ValidationError.ConfigSourceFactoryUnknownParameter error =
            (ValidationError.ConfigSourceFactoryUnknownParameter) result.getErrors().get(0);
        assertEquals(ValidationLevel.DEBUG, error.level());
        assertEquals("Unknown Config Source Factory parameter for: git Parameter key: unknown, value: test", error.description());
    }

    @Test
    void testBuildWithNoModuleConfig() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("repoURI", GIT_URL);
        params.put("branch", "main");
        params.put("configFilePath", RESOURCES_DEFAULT_PROPERTIES);
        params.put("localRepoDirectory", configDirectory.toFile().getAbsolutePath());

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("config", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));
        when(gestaltConfig.getModuleConfig(any())).thenReturn(null);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));

        GResultOf<List<ConfigNode>> result = factory.build(params);

        assertTrue(result.hasResults());
        assertFalse(result.hasErrors());
        assertNotNull(result.results());
        assertEquals("data", result.results().get(0).getKey("config").get().getValue().get());
    }

    @Test
    void testBuildWithInvalidPath() {
        Map<String, String> params = new HashMap<>();
        params.put("repoURI", GIT_URL);
        params.put("branch", "main");
        params.put("configFilePath", "invalid_path");

        when(gestaltConfig.getModuleConfig(any())).thenReturn(gitModuleConfig);


        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));

        GResultOf<List<ConfigNode>> result = factory.build(params);

        assertFalse(result.hasResults());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertInstanceOf(ValidationError.ConfigSourceFactoryException.class, result.getErrors().get(0));
    }

    @Test
    public void integrationTest() throws IOException, GestaltException {

        Path configDirectory = Files.createTempDirectory("gitConfigTest");
        configDirectory.toFile().deleteOnExit();

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=git,repoURI=https://github.com/gestalt-config/gestalt.git," +
            "configFilePath=gestalt-git/src/test/resources/include.properties," +
            "localRepoDirectory=" + configDirectory.toAbsolutePath());

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }
}
