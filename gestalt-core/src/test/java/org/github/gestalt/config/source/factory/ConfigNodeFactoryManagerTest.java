package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ConfigNodeFactoryManagerTest {

    @Mock
    private ConfigNodeFactory mockFactory;

    private ConfigSourceFactoryManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new ConfigSourceFactoryManager(List.of(mockFactory));
    }


    @Test
    void testBuildSuccessful() {
        Map<String, String> parameters = Map.of(ConfigSourceFactoryManager.SOURCE, "supportedSource", "param1", "value1");
        Map<String, ConfigNode> configNode = Map.of("path", new LeafNode("data"));

        when(mockFactory.supportsSource(eq("supportedSource"))).thenReturn(true);
        when(mockFactory.build(any())).thenReturn(GResultOf.result(List.of(new MapNode(configNode))));

        GResultOf<List<ConfigNode>> result = manager.build(parameters);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("data", result.results().get(0).getKey("path").get().getValue().get());
    }

    @Test
    void testBuildNoSourceProvided() {
        Map<String, String> parameters = Map.of("param1", "value1");

        GResultOf<List<ConfigNode>> result = manager.build(parameters);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryNoSource.class, result.getErrors().get(0));
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Source name not provided while building Config Source Factory for parameters: {param1=value1}",
            result.getErrors().get(0).description());
    }

    @Test
    void testBuildSourceNotSupported() {
        Map<String, String> parameters = Map.of(ConfigSourceFactoryManager.SOURCE, "unsupportedSource");

        when(mockFactory.supportsSource(eq("unsupportedSource"))).thenReturn(false);

        GResultOf<List<ConfigNode>> result = manager.build(parameters);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryNotFound.class, result.getErrors().get(0));
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("A Config Source Factory has not be found for source: unsupportedSource",
            result.getErrors().get(0).description());
    }
}
