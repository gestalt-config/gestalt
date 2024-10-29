package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.metadata.IsEncryptedMetadata;
import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.secret.rules.RegexSecretChecker;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptedSecretConfigNodeProcessorTest {

    private EncryptedSecretConfigNodeProcessor processor;
    private ConfigNodeProcessorConfig configMock;
    private GestaltConfig gestaltConfigMock;
    private EncryptedSecretModule encryptedSecretModule;

    @BeforeEach
    void setUp() {
        processor = new EncryptedSecretConfigNodeProcessor();
        configMock = mock(ConfigNodeProcessorConfig.class);
        gestaltConfigMock = mock(GestaltConfig.class);
        encryptedSecretModule = new EncryptedSecretModule(new RegexSecretChecker("secret"));
    }

    @Test
    void testApplyConfigWithModule() {
        when(configMock.getConfig()).thenReturn(gestaltConfigMock);
        when(gestaltConfigMock.getModuleConfig(EncryptedSecretModule.class)).thenReturn(encryptedSecretModule);

        processor.applyConfig(configMock);
        var result = processor.process("secret", new LeafNode("test"));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(EncryptedLeafNode.class, result.results());
        Assertions.assertEquals("test", result.results().getValue().get());
    }

    @Test
    void testApplyConfigWithMetadata() {
        when(configMock.getConfig()).thenReturn(gestaltConfigMock);
        when(gestaltConfigMock.getModuleConfig(EncryptedSecretModule.class)).thenReturn(encryptedSecretModule);

        processor.applyConfig(configMock);
        var result = processor.process("my.data",
            new LeafNode("test", Map.of(IsEncryptedMetadata.ENCRYPTED, List.of(new IsEncryptedMetadata(true)))));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(EncryptedLeafNode.class, result.results());
        Assertions.assertEquals("test", result.results().getValue().get());
    }

    @Test
    void testApplyConfigWithoutModule() {
        when(configMock.getConfig()).thenReturn(gestaltConfigMock);
        when(gestaltConfigMock.getModuleConfig(EncryptedSecretModule.class)).thenReturn(null);
        processor.applyConfig(configMock);

        var result = processor.process("secret", new LeafNode("test"));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertEquals("test", result.results().getValue().get());
    }

    @Test
    void testProcessNonLeafNode() {
        ConfigNode nonLeafNode = new MapNode(new HashMap<>());

        GResultOf<ConfigNode> result = processor.process("path", nonLeafNode);
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals(nonLeafNode, result.results());
    }

    @Test
    void testProcessLeafNodeWithoutSecret() {
        when(configMock.getConfig()).thenReturn(gestaltConfigMock);
        when(gestaltConfigMock.getModuleConfig(EncryptedSecretModule.class)).thenReturn(encryptedSecretModule);

        processor.applyConfig(configMock);
        var result = processor.process("my.data", new LeafNode("test"));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertEquals("test", result.results().getValue().get());
    }

    @Test
    void testProcessLeafNodeWithoutSecretOrMetadata() {
        when(configMock.getConfig()).thenReturn(gestaltConfigMock);
        when(gestaltConfigMock.getModuleConfig(EncryptedSecretModule.class)).thenReturn(encryptedSecretModule);

        processor.applyConfig(configMock);
        var result = processor.process("my.data",
            new LeafNode("test", Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true)))));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertEquals("test", result.results().getValue().get());
    }

    @Test
    void testProcessLeafNodeWithoutSecretOrMetadataFalse() {
        when(configMock.getConfig()).thenReturn(gestaltConfigMock);
        when(gestaltConfigMock.getModuleConfig(EncryptedSecretModule.class)).thenReturn(encryptedSecretModule);

        processor.applyConfig(configMock);
        var result = processor.process("my.data",
            new LeafNode("test", Map.of(IsEncryptedMetadata.ENCRYPTED, List.of(new IsEncryptedMetadata(false)))));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertEquals("test", result.results().getValue().get());
    }

    @Test
    void testEmptyNode() {
        when(configMock.getConfig()).thenReturn(gestaltConfigMock);
        when(gestaltConfigMock.getModuleConfig(EncryptedSecretModule.class)).thenReturn(encryptedSecretModule);

        processor.applyConfig(configMock);
        var result = processor.process("secret", new LeafNode(""));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertEquals("", result.results().getValue().get());
    }

    @Test
    void testnullNode() {
        when(configMock.getConfig()).thenReturn(gestaltConfigMock);
        when(gestaltConfigMock.getModuleConfig(EncryptedSecretModule.class)).thenReturn(encryptedSecretModule);

        processor.applyConfig(configMock);
        var result = processor.process("secret", new LeafNode(null));

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertInstanceOf(LeafNode.class, result.results());
        Assertions.assertTrue(result.results().getValue().isEmpty());
    }
}
