package org.github.gestalt.config.google.node.factory;

import com.google.cloud.storage.Storage;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.google.config.GoogleModuleConfig;
import org.github.gestalt.config.google.storage.GCSConfigSourceBuilder;
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
public class GCSConfigNodeFactory implements ConfigNodeFactory {
    public static final String SOURCE_TYPE = "gcs";
    public static final String PARAMETER_OBJECT_NAME = "objectName";
    public static final String PARAMETER_BUCKET_NAME = "bucketName";
    private static final System.Logger logger = System.getLogger(GCSConfigNodeFactory.class.getName());


    private ConfigLoaderService configLoaderService;
    private Storage storage = null;

    @Override
    public void applyConfig(ConfigNodeFactoryConfig config) {
        this.configLoaderService = config.getConfigLoaderService();
        GoogleModuleConfig moduleConfig = config.getConfig().getModuleConfig(GoogleModuleConfig.class);

        if (moduleConfig == null) {
            logger.log(System.Logger.Level.DEBUG, "GoogleModuleConfig has not been registered. " +
                "if you wish to use the GCS module with a custom storage client though  " +
                "${include}=source=gcs,objectName=test,bucketName=my.properties " +
                "then you must register an GoogleModuleConfig config moduleConfig using the builder.");
        } else {
            if (moduleConfig.hasStorage()) {
                storage = moduleConfig.getStorage();
            } else {
                logger.log(System.Logger.Level.DEBUG, "GoogleModuleConfig was registered but the Storage client " +
                    "was not provided. Falling back to the defaults");
            }
        }
    }

    @Override
    public Boolean supportsType(String type) {
        return SOURCE_TYPE.equalsIgnoreCase(type);
    }

    @Override
    public GResultOf<List<ConfigNode>> build(Map<String, String> parameters) {
        var configSourceBuilder = GCSConfigSourceBuilder.builder();

        List<ValidationError> errors = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                switch (entry.getKey()) {
                    case PARAMETER_OBJECT_NAME:
                        configSourceBuilder.setObjectName(entry.getValue());
                        break;
                    case PARAMETER_BUCKET_NAME:
                        configSourceBuilder.setBucketName(entry.getValue());
                        break;
                    default:
                        errors.add(
                            new ValidationError.ConfigSourceFactoryUnknownParameter(SOURCE_TYPE, entry.getKey(), entry.getValue()));
                        break;
                }
            }

            if (storage != null) {
                configSourceBuilder.setStorage(storage);
            }

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
