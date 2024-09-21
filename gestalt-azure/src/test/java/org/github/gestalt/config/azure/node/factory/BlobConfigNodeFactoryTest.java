package org.github.gestalt.config.azure.node.factory;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.github.gestalt.config.azure.config.AzureModuleConfig;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryConfig;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

class BlobConfigNodeFactoryTest {

    private BlobConfigNodeFactory factory;
    private ConfigLoaderService configLoaderService;
    private ConfigLoader configLoader;
    private GestaltConfig gestaltConfig;
    private AzureModuleConfig azureModuleConfig;
    private BlobClient blobClient;
    private StorageSharedKeyCredential storageSharedKeyCredential;

    @BeforeEach
    public void setUp() {
        factory = new BlobConfigNodeFactory();
        configLoaderService = Mockito.mock();
        configLoader = Mockito.mock();

        gestaltConfig = Mockito.mock();
        azureModuleConfig = Mockito.mock();
        blobClient = Mockito.mock();
        storageSharedKeyCredential = Mockito.mock();
    }

    @Test
    public void testSupportsType() {
        Assertions.assertTrue(factory.supportsType("blob"));
        Assertions.assertFalse(factory.supportsType("other"));
    }

    @Test
    public void testBuildWithValidPath() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("endpoint", "https://1234.blob.core.windows.net/");
        params.put("blob", "test");
        params.put("container", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(azureModuleConfig);
        Mockito.when(azureModuleConfig.hasBlobClient()).thenReturn(true);
        Mockito.when(azureModuleConfig.getBlobClient()).thenReturn(blobClient);

        Mockito.when(blobClient.getBlobName()).thenReturn("my.properties");

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertNotNull(result.results());

        Assertions.assertEquals("data", result.results().get(0).getKey("path").get().getValue().get());
    }

    @Test
    public void testBuildWithCredentials() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("endpoint", "https://1234.blob.core.windows.net/");
        params.put("blob", "test");
        params.put("container", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(azureModuleConfig);
        Mockito.when(azureModuleConfig.hasBlobClient()).thenReturn(true);
        Mockito.when(azureModuleConfig.getBlobClient()).thenReturn(blobClient);
        Mockito.when(azureModuleConfig.hasStorageSharedKeyCredential()).thenReturn(true);
        Mockito.when(azureModuleConfig.getStorageSharedKeyCredential()).thenReturn(storageSharedKeyCredential);

        Mockito.when(blobClient.getBlobName()).thenReturn("my.properties");

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertNotNull(result.results());

        Assertions.assertEquals("data", result.results().get(0).getKey("path").get().getValue().get());
    }

    @Test
    public void testBuildWithNoClient() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("endpoint", "https://1234.blob.core.windows.net/");
        params.put("blob", "test");
        params.put("container", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(azureModuleConfig);
        Mockito.when(azureModuleConfig.hasBlobClient()).thenReturn(false);
        Mockito.when(azureModuleConfig.getBlobClient()).thenReturn(null);
        Mockito.when(azureModuleConfig.hasStorageSharedKeyCredential()).thenReturn(true);
        Mockito.when(azureModuleConfig.getStorageSharedKeyCredential()).thenReturn(storageSharedKeyCredential);

        Mockito.when(blobClient.getBlobName()).thenReturn("my.properties");

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
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
        params.put("endpoint", "https://1234.blob.core.windows.net/");
        params.put("blob", "test");
        params.put("container", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(azureModuleConfig);
        Mockito.when(azureModuleConfig.hasBlobClient()).thenReturn(true);
        Mockito.when(azureModuleConfig.getBlobClient()).thenReturn(blobClient);
        Mockito.when(blobClient.getBlobName()).thenReturn("my.properties");

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryUnknownParameter.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.DEBUG, result.getErrors().get(0).level());
        Assertions.assertEquals("Unknown Config Source Factory parameter for: blob Parameter key: unknown, value: value",
            result.getErrors().get(0).description());
    }

    @Test
    public void testBuildWithNoBlobClientMissingParams() {
        Map<String, String> params = new HashMap<>();
        params.put("blob", "test");
        params.put("container", "my.properties");

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(azureModuleConfig);
        Mockito.when(azureModuleConfig.hasBlobClient()).thenReturn(false);
        Mockito.when(azureModuleConfig.getBlobClient()).thenReturn(null);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryException.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        String error = result.getErrors().get(0).description();
        Assertions.assertEquals("Exception while building Config Source Factory: blob, exception: Must provided either a " +
            "BlobClient or a valid endpoint", error);
    }

    @Test
    public void testBuildWithMissingParams() throws GestaltException {
        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(azureModuleConfig);
        Mockito.when(azureModuleConfig.hasBlobClient()).thenReturn(false);
        Mockito.when(azureModuleConfig.getBlobClient()).thenReturn(null);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));

        Map<String, String> params = new HashMap<>();
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryException.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        String error = result.getErrors().get(0).description();
        Assertions.assertEquals(error, "Exception while building Config Source Factory: blob, exception: " +
            "Must provided either a BlobClient or a valid endpoint");
    }

    @Test
    public void testBuildWithNoModuleConfig() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("endpoint", "https://1234.blob.core.windows.net/");
        params.put("blob", "test");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(null);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryException.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        String error = result.getErrors().get(0).description();
        Assertions.assertEquals("Exception while building Config Source Factory: blob, exception: Must provided either a " +
            "BlobClient or a valid containerName", error);
    }
}
