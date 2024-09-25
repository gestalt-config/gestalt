package org.github.gestalt.config.google.node.factory;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.google.builder.GoogleModuleConfigBuilder;
import org.github.gestalt.config.google.config.GoogleModuleConfig;
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
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;


class GCSConfigNodeFactoryTest {
    private static final String BUCKET_NAME = "testbucket";
    private static final String UPLOAD_FILE_NAME = "src/test/resources/include.properties";

    private GCSConfigNodeFactory factory;
    private ConfigLoaderService configLoaderService;
    private ConfigLoader configLoader;
    private GestaltConfig gestaltConfig;
    private Storage storage;
    private GoogleModuleConfig gcsModuleConfig;

    private Blob blob;

    @BeforeEach
    public void setUp() {
        factory = new GCSConfigNodeFactory();
        configLoaderService = Mockito.mock();
        configLoader = Mockito.mock();

        gestaltConfig = Mockito.mock();
        storage = Mockito.mock();
        gcsModuleConfig = Mockito.mock();
        blob = Mockito.mock();
    }

    @Test
    public void testSupportsType() {
        Assertions.assertTrue(factory.supportsType("gcs"));
        Assertions.assertFalse(factory.supportsType("other"));
    }

    @Test
    public void testBuildWithValidPath() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("objectName", "test");
        params.put("bucketName", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(gcsModuleConfig);
        Mockito.when(gcsModuleConfig.hasStorage()).thenReturn(true);
        Mockito.when(gcsModuleConfig.getStorage()).thenReturn(storage);

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
        params.put("objectName", "test");
        params.put("bucketName", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(gcsModuleConfig);
        Mockito.when(gcsModuleConfig.hasStorage()).thenReturn(true);
        Mockito.when(gcsModuleConfig.getStorage()).thenReturn(storage);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryUnknownParameter.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.DEBUG, result.getErrors().get(0).level());
        Assertions.assertEquals("Unknown Config Source Factory parameter for: gcs Parameter key: unknown, value: value",
            result.getErrors().get(0).description());
    }

    @Test
    public void testBuildWithNoStorageClient() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("objectName", "test");
        params.put("bucketName", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(gcsModuleConfig);
        Mockito.when(gcsModuleConfig.hasStorage()).thenReturn(false);
        Mockito.when(gcsModuleConfig.getStorage()).thenReturn(null);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertNotNull(result.results());

        Assertions.assertEquals("data", result.results().get(0).getKey("path").get().getValue().get());
    }

    @Test
    public void testBuildWithMissingParams() throws GestaltException {
        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(gcsModuleConfig);
        Mockito.when(gcsModuleConfig.hasStorage()).thenReturn(true);
        Mockito.when(gcsModuleConfig.getStorage()).thenReturn(storage);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));

        Map<String, String> params = new HashMap<>();
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryException.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Exception while building Config Source Factory: gcs, exception: " +
            "Google Cloud Storage bucketName can not be null", result.getErrors().get(0).description());
    }

    @Test
    public void testBuildWithNoModuleConfig() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("objectName", "test");
        params.put("bucketName", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(null);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());
        Assertions.assertNotNull(result.results());

        Assertions.assertEquals("data", result.results().get(0).getKey("path").get().getValue().get());
    }

    @Test
    public void integrationTest() throws IOException, GestaltException {
        final File uploadFile = new File(UPLOAD_FILE_NAME);
        byte[] bytes = Files.readAllBytes(uploadFile.toPath());

        Mockito.when(storage.get(BlobId.of(BUCKET_NAME, UPLOAD_FILE_NAME))).thenReturn(blob);
        Mockito.when(blob.getContent()).thenReturn(bytes);

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=gcs,objectName=" + UPLOAD_FILE_NAME + ",bucketName=" + BUCKET_NAME);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(GoogleModuleConfigBuilder.builder().setStorage(storage).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }
}
