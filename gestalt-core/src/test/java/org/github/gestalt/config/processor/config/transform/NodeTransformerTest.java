package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.source.factory.ConfigSourceFactoryService;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class NodeTransformerTest {

    private GestaltConfig config;
    private ConfigNodeService configNodeService;
    private SentenceLexer lexer;
    private SecretConcealer secretConcealer;
    private ConfigSourceFactoryService configSourceFactoryService;

    @BeforeEach
    public void setup() {
        config = new GestaltConfig();
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        secretConcealer = Mockito.mock();
        configSourceFactoryService = Mockito.mock();
    }

    @Test
    void name() {
        NodeTransformer transformer = new NodeTransformer();
        Assertions.assertEquals("node", transformer.name());
    }

    @Test
    void processOk() {
        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(GResultOf.result(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(GResultOf.result(new LeafNode("new value")));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
            configSourceFactoryService));
        GResultOf<String> resultsOf = transformer.process("hello", "test", "");

        Assertions.assertTrue(resultsOf.hasResults());
        Assertions.assertFalse(resultsOf.hasErrors());

        Assertions.assertEquals("new value", resultsOf.results());
    }

    @Test
    void processNoConfig() {
        NodeTransformer transformer = new NodeTransformer();
        GResultOf<String> results = transformer.process("hello", "test", "");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("node Transform PostProcessorConfig is null, unable to transform path: hello with: test",
            results.getErrors().get(0).description());
    }

    @Test
    void processNull() {
        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(GResultOf.result(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(GResultOf.result(new LeafNode("new value")));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
            configSourceFactoryService));
        GResultOf<String> results = transformer.process("hello", null, "node:");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("Invalid string: node:, on path: hello in transformer: node",
            results.getErrors().get(0).description());
    }

    @Test
    void processErrorTokenizing() {
        NodeTransformer transformer = new NodeTransformer();
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.errors(new ValidationError.FailedToTokenizeElement("hello", "test")));

        transformer.applyConfig(new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
            configSourceFactoryService));
        GResultOf<String> results = transformer.process("hello", "test", "");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(2, results.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element hello for path: test", results.getErrors().get(0).description());
        Assertions.assertEquals("Errors generating tokens while running node transform path: hello with: test",
            results.getErrors().get(1).description());
    }

    @Test
    void processErrorTokenizingNoResults() {
        NodeTransformer transformer = new NodeTransformer();
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(GResultOf.errors(new ValidationError.EmptyPath()));

        transformer.applyConfig(new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
            configSourceFactoryService));
        GResultOf<String> results = transformer.process("hello", "test", "");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(2, results.getErrors().size());
        Assertions.assertEquals("empty path provided", results.getErrors().get(0).description());
        Assertions.assertEquals("No results generating tokens while running node transform path: hello with: test",
            results.getErrors().get(1).description());
    }

    @Test
    void processErrorNavigateToNode() {
        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(GResultOf.result(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(GResultOf.errors(new ValidationError.NoResultsFoundForNode("test", MapNode.class, "post processing")));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
            configSourceFactoryService));
        GResultOf<String> results = transformer.process("hello", "test", "");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(2, results.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: test, for class: MapNode, during post processing",
            results.getErrors().get(0).description());
        Assertions.assertEquals("Errors navigating to node while running node transform path: hello with: test",
            results.getErrors().get(1).description());
    }

    @Test
    void processErrorNavigateToNodeNoResults() {

        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(GResultOf.result(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(GResultOf.errors(new ValidationError.EmptyPath()));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
            configSourceFactoryService));
        GResultOf<String> results = transformer.process("hello", "test", "");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(2, results.getErrors().size());
        Assertions.assertEquals("empty path provided", results.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: hello, for class: test, during NodeTransformer",
            results.getErrors().get(1).description());
    }

    @Test
    void processErrorWrongNodeType() {
        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(GResultOf.result(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(GResultOf.result(new MapNode(new HashMap<>())));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
            configSourceFactoryService));
        GResultOf<String> results = transformer.process("hello", "test", "");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("Non leaf node found while running node transform path: hello with: test",
            results.getErrors().get(0).description());
    }

    @Test
    void processErrorEmptyLeafNode() {
        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(GResultOf.result(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(GResultOf.result(new LeafNode(null)));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
            configSourceFactoryService));
        GResultOf<String> results = transformer.process("hello", "test", "");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("leaf node has no value while running node transform path: hello with: test",
            results.getErrors().get(0).description());
    }
}
