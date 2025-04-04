package org.github.gestalt.config.security.temporary;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.github.gestalt.config.metadata.IsTemporaryMetadata;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TemporarySecretConfigNodeProcessorTest {

    private final GestaltConfig gestalt = Mockito.mock();

    @BeforeEach
    public void setup() {
        Mockito.reset(gestalt);
    }

    @Test
    public void temporarySecretConfigNodeProcessor() {
        TemporarySecretModuleBuilder builder = TemporarySecretModuleBuilder.builder().addSecretWithCount("secret", 1);

        Mockito.when(gestalt.getModuleConfig(any())).thenReturn(builder.build());

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestalt, null, null, null, null);

        TemporarySecretConfigNodeProcessor temporarySecretConfigNodeProcessor = new TemporarySecretConfigNodeProcessor();
        temporarySecretConfigNodeProcessor.applyConfig(config);

        ConfigNode targetNode = new LeafNode("password");
        var result = temporarySecretConfigNodeProcessor.process("my.secret", targetNode);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(TemporaryLeafNode.class, result.results());
        Assertions.assertEquals("password", result.results().getValue().get());
        Assertions.assertTrue(result.results().getValue().isEmpty());

    }

    @Test
    public void temporarySecretConfigNodeProcessorMetadata() {
        TemporarySecretModuleBuilder builder = TemporarySecretModuleBuilder.builder().addSecretWithCount("secret", 1);

        Mockito.when(gestalt.getModuleConfig(any())).thenReturn(builder.build());

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestalt, null, null, null, null);

        TemporarySecretConfigNodeProcessor temporarySecretConfigNodeProcessor = new TemporarySecretConfigNodeProcessor();
        temporarySecretConfigNodeProcessor.applyConfig(config);

        ConfigNode targetNode = new LeafNode("password", Map.of(IsTemporaryMetadata.TEMPORARY, List.of(new IsTemporaryMetadata(2))));
        var result = temporarySecretConfigNodeProcessor.process("my.data", targetNode);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(TemporaryLeafNode.class, result.results());
        Assertions.assertEquals("password", result.results().getValue().get());
        Assertions.assertEquals("password", result.results().getValue().get());
        Assertions.assertTrue(result.results().getValue().isEmpty());

    }

    @Test
    public void nonSecret() {
        TemporarySecretModuleBuilder builder = TemporarySecretModuleBuilder.builder().addSecretWithCount("secret", 1);

        Mockito.when(gestalt.getModuleConfig(any())).thenReturn(builder.build());

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestalt, null, null, null, null);

        TemporarySecretConfigNodeProcessor temporarySecretConfigNodeProcessor = new TemporarySecretConfigNodeProcessor();
        temporarySecretConfigNodeProcessor.applyConfig(config);

        ConfigNode targetNode = new LeafNode("data");
        var result = temporarySecretConfigNodeProcessor.process("my.work", targetNode);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertEquals("data", result.results().getValue().get());
        Assertions.assertEquals("data", result.results().getValue().get());
    }

    @Test
    public void nonSecretMetadata() {
        TemporarySecretModuleBuilder builder = TemporarySecretModuleBuilder.builder().addSecretWithCount("secret", 1);

        Mockito.when(gestalt.getModuleConfig(any())).thenReturn(builder.build());

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestalt, null, null, null, null);

        TemporarySecretConfigNodeProcessor temporarySecretConfigNodeProcessor = new TemporarySecretConfigNodeProcessor();
        temporarySecretConfigNodeProcessor.applyConfig(config);

        ConfigNode targetNode = new LeafNode("data", Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true))));
        var result = temporarySecretConfigNodeProcessor.process("my.work", targetNode);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertEquals("data", result.results().getValue().get());
        Assertions.assertEquals("data", result.results().getValue().get());
    }

    @Test
    public void emptyLeaf() {
        TemporarySecretModuleBuilder builder = TemporarySecretModuleBuilder.builder().addSecretWithCount("secret", 1);

        Mockito.when(gestalt.getModuleConfig(any())).thenReturn(builder.build());

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestalt, null, null, null, null);

        TemporarySecretConfigNodeProcessor temporarySecretConfigNodeProcessor = new TemporarySecretConfigNodeProcessor();
        temporarySecretConfigNodeProcessor.applyConfig(config);

        ConfigNode targetNode = new LeafNode(null);
        var result = temporarySecretConfigNodeProcessor.process("my.secret", targetNode);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertTrue(result.results().getValue().isEmpty());
    }

    @Test
    public void nonLeafNode() {
        TemporarySecretModuleBuilder builder = TemporarySecretModuleBuilder.builder().addSecretWithCount("secret", 1);

        Mockito.when(gestalt.getModuleConfig(any())).thenReturn(builder.build());

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestalt, null, null, null, null);

        TemporarySecretConfigNodeProcessor temporarySecretConfigNodeProcessor = new TemporarySecretConfigNodeProcessor();
        temporarySecretConfigNodeProcessor.applyConfig(config);

        ConfigNode leafNode = new LeafNode("data");
        MapNode targetNode = new MapNode(Map.of("secret", leafNode));
        var result = temporarySecretConfigNodeProcessor.process("my.work", targetNode);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(MapNode.class, result.results());
    }



    @Test
    public void nonSecretSetup() {
        TemporarySecretModuleBuilder builder = TemporarySecretModuleBuilder.builder();

        Mockito.when(gestalt.getModuleConfig(any())).thenReturn(builder.build());

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestalt, null, null, null, null);

        TemporarySecretConfigNodeProcessor temporarySecretConfigNodeProcessor = new TemporarySecretConfigNodeProcessor();
        temporarySecretConfigNodeProcessor.applyConfig(config);

        ConfigNode targetNode = new LeafNode("data");
        var result = temporarySecretConfigNodeProcessor.process("my.work", targetNode);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertEquals("data", result.results().getValue().get());
        Assertions.assertEquals("data", result.results().getValue().get());
    }

    @Test
    public void nonModuleSet() {
        Mockito.when(gestalt.getModuleConfig(any())).thenReturn(null);

        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestalt, null, null, null, null);

        TemporarySecretConfigNodeProcessor temporarySecretConfigNodeProcessor = new TemporarySecretConfigNodeProcessor();
        temporarySecretConfigNodeProcessor.applyConfig(config);

        ConfigNode targetNode = new LeafNode("data");
        var result = temporarySecretConfigNodeProcessor.process("my.work", targetNode);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertEquals("data", result.results().getValue().get());
        Assertions.assertEquals("data", result.results().getValue().get());
    }
}
