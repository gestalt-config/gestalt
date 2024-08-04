package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigNodeImportFactoryTest {

    private ConfigNodeImportFactory factory;
    private ConfigNodeService mockConfigNodeService;
    private SentenceLexer mockLexer;

    @BeforeEach
    void setUp() {
        factory = new ConfigNodeImportFactory();
        mockConfigNodeService = mock(ConfigNodeService.class);
        mockLexer = mock(SentenceLexer.class);

        ConfigNodeFactoryConfig config = mock(ConfigNodeFactoryConfig.class);
        when(config.getConfigNodeService()).thenReturn(mockConfigNodeService);
        when(config.getLexer()).thenReturn(mockLexer);

        factory.applyConfig(config);
    }

    @Test
    void testSupportsSource() {
        assertTrue(factory.supportsSource("node"));
        assertFalse(factory.supportsSource("other"));
    }

    @Test
    void testBuildWithValidPath() {
        Map<String, String> parameters = Map.of("path", "valid.path");
        Map<String, ConfigNode> config = Map.of("path", new LeafNode("data"));
        MapNode mapNode = new MapNode(config);
        List<Token> tokens = List.of(new ObjectToken("valid"), new ObjectToken("path"));

        when(mockLexer.scan("valid.path")).thenReturn(GResultOf.result(tokens));
        when(mockConfigNodeService.navigateToNode("valid.path", tokens, Tags.of())).thenReturn(GResultOf.result(mapNode));

        GResultOf<List<ConfigNode>> result = factory.build(parameters);

        assertTrue(result.hasResults());
        assertFalse(result.hasErrors());

        assertEquals("data", result.results().get(0).getKey("path").get().getValue().get());
    }

    @Test
    void testBuildWithUnknownParameter() {
        Map<String, String> parameters = Map.of("unknown", "value");

        GResultOf<List<ConfigNode>> result = factory.build(parameters);

        assertFalse(result.hasResults());
        assertTrue(result.hasErrors());

        ValidationError error = result.getErrors().get(0);
        assertTrue(error instanceof ValidationError.ConfigSourceFactoryUnknownParameter);
        assertEquals(ValidationLevel.DEBUG, error.level());
        assertEquals("Unknown Config Source Factory parameter for: node Parameter key: unknown, value: value", error.description());
    }

    @Test
    void testBuildWithNullPath() {
        Map<String, String> parameters = Map.of();

        GResultOf<List<ConfigNode>> result = factory.build(parameters);

        assertFalse(result.hasResults());
        assertTrue(result.hasErrors());

        ValidationError error = result.getErrors().get(0);
        assertTrue(error instanceof ValidationError.ConfigNodeImportNodeEmpty);
    }

    @Test
    void testBuildWithException() {
        Map<String, String> parameters = Map.of("path", "valid.path");

        when(mockLexer.scan("valid.path")).thenThrow(new RuntimeException("Test exception"));

        GResultOf<List<ConfigNode>> result = factory.build(parameters);

        assertFalse(result.hasResults());
        assertTrue(result.hasErrors());

        ValidationError error = result.getErrors().get(0);
        assertTrue(error instanceof ValidationError.ConfigSourceFactoryException);
    }
}
