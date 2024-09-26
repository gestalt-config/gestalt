package org.github.gestalt.config.aws.node.factory;

import org.github.gestalt.config.aws.config.AWSModuleConfig;
import org.github.gestalt.config.aws.errors.AWSValidationErrors;
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
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class S3ConfigNodeFactoryTest {

    private S3ConfigNodeFactory factory;
    private final ConfigLoaderService configLoaderService = Mockito.mock();
    private final ConfigLoader configLoader = Mockito.mock();
    private final GestaltConfig gestaltConfig = Mockito.mock();
    private final AWSModuleConfig awsModuleConfig = Mockito.mock();
    private final S3Client s3Client = Mockito.mock();

    @BeforeEach
    public void setUp() {
        factory = new S3ConfigNodeFactory();
        Mockito.reset(configLoaderService, configLoader, gestaltConfig, awsModuleConfig, s3Client);
    }

    @Test
    public void testSupportsType() {
        Assertions.assertTrue(factory.supportsType("s3"));
        Assertions.assertFalse(factory.supportsType("other"));
    }

    @Test
    public void testBuildWithValidPath() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("bucket", "test");
        params.put("key", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(awsModuleConfig);
        Mockito.when(awsModuleConfig.hasS3Client()).thenReturn(true);
        Mockito.when(awsModuleConfig.getS3Client()).thenReturn(s3Client);

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
        params.put("bucket", "test");
        params.put("key", "my.properties");

        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(awsModuleConfig);
        Mockito.when(awsModuleConfig.hasS3Client()).thenReturn(true);
        Mockito.when(awsModuleConfig.getS3Client()).thenReturn(s3Client);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryUnknownParameter.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.DEBUG, result.getErrors().get(0).level());
        Assertions.assertEquals("Unknown Config Source Factory parameter for: s3 Parameter key: unknown, value: value",
            result.getErrors().get(0).description());
    }

    @Test
    public void testBuildWithNoS3Client() {
        Map<String, String> params = new HashMap<>();
        params.put("path", "/invalid/path");

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(awsModuleConfig);
        Mockito.when(awsModuleConfig.hasS3Client()).thenReturn(false);
        Mockito.when(awsModuleConfig.getS3Client()).thenReturn(null);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertInstanceOf(AWSValidationErrors.AWSS3ClientConfigNotSet.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        String error = result.getErrors().get(0).description();
        Assertions.assertTrue(error.startsWith("AWSModuleConfig has not been registered and or the " +
            "S3 Client was not set. Register by creating a AWSBuilder then registering the AWSBuilder.build() results with the " +
            "Gestalt Builder.addModuleConfig(). If you wish to use the aws module with node substitution/include {path=/invalid/path} " +
            "on the path: s3"));
    }

    @Test
    public void testBuildWithMissingParams() throws GestaltException {
        Map<String, ConfigNode> node = new HashMap<>();
        node.put("path", new LeafNode("data"));

        List<ConfigNodeContainer> configNodes = List.of(new ConfigNodeContainer(new MapNode(node), null, Tags.of()));

        Mockito.when(configLoaderService.getLoader(any())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(any())).thenReturn(GResultOf.result(configNodes));

        Mockito.when(gestaltConfig.getModuleConfig(any())).thenReturn(awsModuleConfig);
        Mockito.when(awsModuleConfig.hasS3Client()).thenReturn(true);
        Mockito.when(awsModuleConfig.getS3Client()).thenReturn(s3Client);

        factory.applyConfig(new ConfigNodeFactoryConfig(configLoaderService, null, null, gestaltConfig));

        Map<String, String> params = new HashMap<>();
        GResultOf<List<ConfigNode>> result = factory.build(params);

        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getErrors());
        Assertions.assertInstanceOf(ValidationError.ConfigSourceFactoryException.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertTrue(result.getErrors().get(0).description().startsWith("Exception while building Config Source Factory: s3, " +
            "exception: S3 bucketName can not be null"));
    }

    @Test
    public void testBuildWithNoModuleConfig() throws GestaltException {
        Map<String, String> params = new HashMap<>();
        params.put("bucket", "test");
        params.put("key", "my.properties");

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
        Assertions.assertInstanceOf(AWSValidationErrors.AWSS3ClientConfigNotSet.class, result.getErrors().get(0));

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        String error = result.getErrors().get(0).description();
        Assertions.assertTrue(error.startsWith("AWSModuleConfig has not been registered and or the S3 Client was not set. Register by " +
            "creating a AWSBuilder then registering the AWSBuilder.build() results with the Gestalt Builder.addModuleConfig(). " +
            "If you wish to use the aws module with node substitution/include {bucket=test, key=my.properties} on the path: s3"));
    }
}
