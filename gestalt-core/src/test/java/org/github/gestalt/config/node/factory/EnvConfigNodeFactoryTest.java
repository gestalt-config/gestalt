package org.github.gestalt.config.node.factory;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnvConfigNodeFactoryTest {

    private EnvVarsConfigNodeFactory factory;
    private final ConfigLoaderService configLoaderService = Mockito.mock();
    private final ConfigLoader configLoader = Mockito.mock();

    @BeforeEach
    public void setUp() {
        factory = new EnvVarsConfigNodeFactory();
        Mockito.reset(configLoaderService, configLoader);
    }

    @Test
    public void testSupportsType() {
        Assertions.assertTrue(factory.supportsType("env"));
        Assertions.assertFalse(factory.supportsType("other"));
    }

    @Test
    public void testBuildWithValidPath() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("failOnErrors", "true");
        params.put("prefix", "GESTALT_");
        params.put("ignoreCaseOnPrefix", "true");
        params.put("removePrefix", "false");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(Mockito.any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(Mockito.any())).thenReturn(GResultOf.result(configNodes));

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, null));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertNotNull(result.results());

        Assertions.assertEquals("data", result.results().get(0).getKey("path").get().getValue().get());
    }

    @Test
    public void testBuildWithUnknownParameter() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("unknown", "value");
        params.put("prefix", "GESTALT_");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(Mockito.any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(Mockito.any())).thenReturn(GResultOf.result(configNodes));

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, null));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryUnknownParameter.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.DEBUG, result.getErrors().get(0).level());
        Assertions.assertEquals("Unknown Config Source Factory parameter for: env Parameter key: unknown, value: value",
            result.getErrors().get(0).description());
    }
}

