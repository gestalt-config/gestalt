package org.github.gestalt.config.processor.config.include;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryService;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("VariableDeclarationUsageDistance")
class ConfigNodeIncludeProcessorTest {
    private ConfigNodeFactoryService configNodeFactoryService;

    private ConfigNodeProcessorConfig ppConfig;

    @BeforeEach
    public void setup() {
        GestaltConfig config = new GestaltConfig();
        ConfigNodeService configNodeService = Mockito.mock(ConfigNodeService.class);
        SentenceLexer lexer = new PathLexer();
        SecretConcealer secretConcealer = Mockito.mock();
        configNodeFactoryService = Mockito.mock();

        ppConfig =
            new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
                configNodeFactoryService);
    }

    @Test
    void processOkImportUnder() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include", new LeafNode("source=node"));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));


        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertFalse(processedNodes.hasErrors());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertEquals("c", mapResults.getKey("c").get().getValue().get());
    }

    @Test
    void processOkImportUnderDefined() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include:-2", new LeafNode("source=node"));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertFalse(processedNodes.hasErrors());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertEquals("c", mapResults.getKey("c").get().getValue().get());
    }

    @Test
    void processOkImportOver() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include:1", new LeafNode("source=node"));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertFalse(processedNodes.hasErrors());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b changed", mapResults.getKey("b").get().getValue().get());
        Assertions.assertEquals("c", mapResults.getKey("c").get().getValue().get());
    }

    @Test
    void processSingleNode() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("$include:1", new LeafNode("source=node"));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        importNodeMap.put("b", new LeafNode("b"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertFalse(processedNodes.hasErrors());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertEquals("c", mapResults.getKey("c").get().getValue().get());
    }

    @Test
    void processOkNoImport() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertFalse(processedNodes.hasErrors());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(2, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
    }

    @Test
    void processOkLeaf() {
        ConfigNode originalRoot = new LeafNode("test");

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertFalse(processedNodes.hasErrors());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(LeafNode.class, results);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("test", results.getValue().get());
    }

    @Test
    void processNotSetup() {
        ConfigNode originalRoot = new LeafNode("test");

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertFalse(processedNodes.hasErrors());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(LeafNode.class, results);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("test", results.getValue().get());
    }

    @Test
    void processOkImportNullKey() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include", new LeafNode("source=node"));
        originalNodeMap.put(null, new LeafNode("source=node"));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertTrue(processedNodes.hasErrors());

        Assertions.assertEquals(1, processedNodes.getErrors().size());
        Assertions.assertEquals(ValidationLevel.WARN, processedNodes.getErrors().get(0).level());
        Assertions.assertEquals("Empty node name provided for path: test",
            processedNodes.getErrors().get(0).description());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertEquals("c", mapResults.getKey("c").get().getValue().get());
    }

    @Test
    void processErrorImportBadNodeType() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include", new MapNode(Map.of("source", new LeafNode("node"))));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertTrue(processedNodes.hasErrors());

        Assertions.assertEquals(1, processedNodes.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, processedNodes.getErrors().get(0).level());
        Assertions.assertEquals("A Config node import is the wrong node type on path test expected a leaf received: MapNode",
            processedNodes.getErrors().get(0).description());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertEquals("node", mapResults.getKey("$include").get().getKey("source").get().getValue().get());
    }

    @Test
    void processErrorImportBadParametersLong() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include", new LeafNode("source=node=test"));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertTrue(processedNodes.hasErrors());

        Assertions.assertEquals(1, processedNodes.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, processedNodes.getErrors().get(0).level());
        Assertions.assertEquals("A Config node import parameter on path: test with parameters: source=node=test, " +
                "has a invalid parameter source=node=test with the wrong size 3",
            processedNodes.getErrors().get(0).description());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertEquals("c", mapResults.getKey("c").get().getValue().get());
    }

    @Test
    void processErrorImportBadParametersShort() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include", new LeafNode("source"));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();


        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertTrue(processedNodes.hasErrors());

        Assertions.assertEquals(1, processedNodes.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, processedNodes.getErrors().get(0).level());
        Assertions.assertEquals("A Config node import parameter on path: test with parameters: source, " +
                "has a invalid parameter source with the wrong size 1",
            processedNodes.getErrors().get(0).description());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertEquals("c", mapResults.getKey("c").get().getValue().get());
    }

    @Test
    void processErrorEmptyImportLeaf() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include", new LeafNode(""));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertTrue(processedNodes.hasErrors());

        Assertions.assertEquals(1, processedNodes.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, processedNodes.getErrors().get(0).level());
        Assertions.assertEquals("A Config node import is empty on path test",
            processedNodes.getErrors().get(0).description());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertTrue(mapResults.getKey("$include").get().hasValue());
    }

    @Test
    void processErrorNullImportLeaf() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include", new LeafNode(null));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertTrue(processedNodes.hasErrors());

        Assertions.assertEquals(1, processedNodes.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, processedNodes.getErrors().get(0).level());
        Assertions.assertEquals("A Config node import is empty on path test",
            processedNodes.getErrors().get(0).description());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertFalse(mapResults.getKey("$include").get().hasValue());
    }

    @Test
    void processErrorImportLeafValueNull() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include", null);

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));

        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertFalse(processedNodes.hasErrors());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(3, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertTrue(mapResults.getKey("$include").isEmpty());
    }

    @Test
    void processErrorImportMaxNesting() {

        Map<String, ConfigNode> originalNodeMap = new HashMap<>();
        originalNodeMap.put("a", new LeafNode("a"));
        originalNodeMap.put("b", new LeafNode("b"));
        originalNodeMap.put("$include", new LeafNode("source=node"));

        Map<String, ConfigNode> importNodeMap = new HashMap<>();
        importNodeMap.put("b", new LeafNode("b changed"));
        importNodeMap.put("c", new LeafNode("c"));
        importNodeMap.put("$include", new LeafNode("source=node"));

        ConfigNode originalRoot = new MapNode(originalNodeMap);
        ConfigNode importRoot = new MapNode(importNodeMap);

        ConfigNodeIncludeProcessor processor = new ConfigNodeIncludeProcessor();

        Mockito.when(configNodeFactoryService.build(Mockito.any())).thenReturn(GResultOf.result(List.of(importRoot)));


        processor.applyConfig(ppConfig);

        var processedNodes = processor.process("test", originalRoot);

        Assertions.assertTrue(processedNodes.hasResults());
        Assertions.assertTrue(processedNodes.hasErrors());

        Assertions.assertEquals(1, processedNodes.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, processedNodes.getErrors().get(0).level());
        Assertions.assertEquals("Reached the maximum nested import depth of: 5, on path: test, " +
                "if this is intended increase the limit using GestaltBuilder.setNodeNestedIncludeLimit(10)",
            processedNodes.getErrors().get(0).description());

        var results = processedNodes.results();

        Assertions.assertInstanceOf(MapNode.class, results);
        var mapResults = (MapNode) results;
        Assertions.assertEquals(4, mapResults.size());
        Assertions.assertEquals("a", mapResults.getKey("a").get().getValue().get());
        Assertions.assertEquals("b", mapResults.getKey("b").get().getValue().get());
        Assertions.assertEquals("c", mapResults.getKey("c").get().getValue().get());
        Assertions.assertEquals("source=node", mapResults.getKey("$include").get().getValue().get());
    }
}

