package org.github.gestalt.config.azure.node.factory;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.github.gestalt.config.azure.blob.BlobConfigSourceBuilder;
import org.github.gestalt.config.azure.config.AzureModuleConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.loader.ConfigLoaderUtils;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.factory.ConfigNodeFactory;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryConfig;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating an Azure Blob Config Node from parameters.
 *
 * <p>Load a config source from a File then converts it to a config node
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class BlobConfigNodeFactory implements ConfigNodeFactory {
    public static final String SOURCE_TYPE = "blob";
    public static final String PARAMETER_ENDPOINT = "endpoint";
    public static final String PARAMETER_CONTAINER = "container";
    public static final String PARAMETER_BLOB = "blob";
    private static final System.Logger logger = System.getLogger(BlobConfigNodeFactory.class.getName());
    private ConfigLoaderService configLoaderService;
    private BlobClient blobClient;
    private StorageSharedKeyCredential credential;

    @Override
    public void applyConfig(ConfigNodeFactoryConfig config) {
        this.configLoaderService = config.getConfigLoaderService();
        AzureModuleConfig moduleConfig = config.getConfig().getModuleConfig(AzureModuleConfig.class);

        if (moduleConfig == null) {
            logger.log(System.Logger.Level.WARNING, "AzureModuleConfig has not been registered. " +
                "if you wish to use the azure module with Blob Config Node substitution " +
                "${include}=source=blob,container=test,blob=my.properties " +
                "then you must register an AzureModuleConfig config moduleConfig using the builder. " +
                "Blob config node substitution/include may not work");
        } else {
            if (moduleConfig.hasBlobClient()) {
                blobClient = moduleConfig.getBlobClient();
            } else {
                logger.log(System.Logger.Level.WARNING, "AzureModuleConfig was registered but the azure blob client " +
                    "was not provided. Azure blob config node substitution/include may not work");
            }

            if (moduleConfig.hasStorageSharedKeyCredential()) {
                credential = moduleConfig.getStorageSharedKeyCredential();
            }
        }
    }

    @Override
    public Boolean supportsType(String type) {
        return SOURCE_TYPE.equalsIgnoreCase(type);
    }

    @Override
    public GResultOf<List<ConfigNode>> build(Map<String, String> parameters) {
        var configSourceBuilder = BlobConfigSourceBuilder.builder();

        List<ValidationError> errors = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                switch (entry.getKey()) {
                    case PARAMETER_ENDPOINT:
                        configSourceBuilder.setEndpoint(entry.getValue());
                        break;
                    case PARAMETER_CONTAINER:
                        configSourceBuilder.setContainerName(entry.getValue());
                        break;
                    case PARAMETER_BLOB:
                        configSourceBuilder.setBlobName(entry.getValue());
                        break;
                    default:
                        errors.add(
                            new ValidationError.ConfigSourceFactoryUnknownParameter(SOURCE_TYPE, entry.getKey(), entry.getValue()));
                        break;
                }
            }

            configSourceBuilder.setBlobClient(blobClient);
            configSourceBuilder.setCredential(credential);

            var fileConfigSource = configSourceBuilder.build().getConfigSource();

            GResultOf<List<ConfigNode>> loadedNodes = ConfigLoaderUtils.convertSourceToNodes(fileConfigSource, configLoaderService);
            errors.addAll(loadedNodes.getErrors());

            return GResultOf.resultOf(loadedNodes.results(), errors);
        } catch (Exception ex) {
            errors.add(new ValidationError.ConfigSourceFactoryException(SOURCE_TYPE, ex));
            return GResultOf.errors(errors);
        }
    }
}
