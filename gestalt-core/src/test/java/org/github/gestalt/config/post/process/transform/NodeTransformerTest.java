package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;
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

    @BeforeEach
    public void setup() {
        config = new GestaltConfig();
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
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
        Mockito.when(lexer.scan("test")).thenReturn(ValidateOf.valid(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(ValidateOf.valid(new LeafNode("new value")));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new PostProcessorConfig(config, configNodeService, lexer));
        ValidateOf<String> validateOfResults = transformer.process("hello", "test", "");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());

        Assertions.assertEquals("new value", validateOfResults.results());
    }

    @Test
    void processNoConfig() {
        NodeTransformer transformer = new NodeTransformer();
        ValidateOf<String> validateOfResults = transformer.process("hello", "test", "");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("node Transform PostProcessorConfig is null, unable to transform path: hello with: test",
            validateOfResults.getErrors().get(0).description());
    }

    @Test
    void processNull() {
        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(ValidateOf.valid(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
               .thenReturn(ValidateOf.valid(new LeafNode("new value")));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new PostProcessorConfig(config, configNodeService, lexer));
        ValidateOf<String> validateOfResults = transformer.process("hello", null, "node:");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid string: node:, on path: hello in transformer: node",
            validateOfResults.getErrors().get(0).description());
    }

    @Test
    void processErrorTokenizing() {
        NodeTransformer transformer = new NodeTransformer();
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test"))
            .thenReturn(ValidateOf.inValid(new ValidationError.FailedToTokenizeElement("hello", "test")));

        transformer.applyConfig(new PostProcessorConfig(config, configNodeService, lexer));
        ValidateOf<String> validateOfResults = transformer.process("hello", "test", "");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(2, validateOfResults.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element hello for path: test", validateOfResults.getErrors().get(0).description());
        Assertions.assertEquals("Errors generating tokens while running node transform path: hello with: test",
            validateOfResults.getErrors().get(1).description());
    }

    @Test
    void processErrorTokenizingNoResults() {
        NodeTransformer transformer = new NodeTransformer();
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(ValidateOf.inValid(new ValidationError.EmptyPath()));

        transformer.applyConfig(new PostProcessorConfig(config, configNodeService, lexer));
        ValidateOf<String> validateOfResults = transformer.process("hello", "test", "");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(2, validateOfResults.getErrors().size());
        Assertions.assertEquals("empty path provided", validateOfResults.getErrors().get(0).description());
        Assertions.assertEquals("No results generating tokens while running node transform path: hello with: test",
            validateOfResults.getErrors().get(1).description());
    }

    @Test
    void processErrorNavigateToNode() {
        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(ValidateOf.valid(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(ValidateOf.inValid(new ValidationError.NoResultsFoundForNode("test", MapNode.class, "post processing")));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new PostProcessorConfig(config, configNodeService, lexer));
        ValidateOf<String> validateOfResults = transformer.process("hello", "test", "");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(2, validateOfResults.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: test, for class: MapNode, during post processing",
            validateOfResults.getErrors().get(0).description());
        Assertions.assertEquals("Errors navigating to node while running node transform path: hello with: test",
            validateOfResults.getErrors().get(1).description());
    }

    @Test
    void processErrorNavigateToNodeNoResults() {

        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(ValidateOf.valid(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(ValidateOf.inValid(new ValidationError.EmptyPath()));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new PostProcessorConfig(config, configNodeService, lexer));
        ValidateOf<String> validateOfResults = transformer.process("hello", "test", "");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(2, validateOfResults.getErrors().size());
        Assertions.assertEquals("empty path provided", validateOfResults.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: hello, for class: test, during NodeTransformer",
            validateOfResults.getErrors().get(1).description());
    }

    @Test
    void processErrorWrongNodeType() {
        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(ValidateOf.valid(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(ValidateOf.valid(new MapNode(new HashMap<>())));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new PostProcessorConfig(config, configNodeService, lexer));
        ValidateOf<String> validateOfResults = transformer.process("hello", "test", "");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Non leaf node found while running node transform path: hello with: test",
            validateOfResults.getErrors().get(0).description());
    }

    @Test
    void processErrorEmptyLeafNode() {
        List<Token> tokens = Collections.singletonList(new ObjectToken("test"));
        Mockito.when(lexer.normalizeSentence("test")).thenReturn("test");
        Mockito.when(lexer.scan("test")).thenReturn(ValidateOf.valid(tokens));
        Mockito.when(configNodeService.navigateToNode("hello", tokens, Tags.of()))
            .thenReturn(ValidateOf.valid(new LeafNode(null)));

        NodeTransformer transformer = new NodeTransformer();
        transformer.applyConfig(new PostProcessorConfig(config, configNodeService, lexer));
        ValidateOf<String> validateOfResults = transformer.process("hello", "test", "");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("leaf node has no value while running node transform path: hello with: test",
            validateOfResults.getErrors().get(0).description());
    }
}
