package org.github.gestalt.config.aws.node.factory;

import org.github.gestalt.config.aws.config.AWSModuleConfig;
import org.github.gestalt.config.aws.errors.AWSValidationErrors;
import org.github.gestalt.config.aws.s3.S3ConfigSourceBuilder;
import org.github.gestalt.config.aws.transformer.AWSSecretTransformer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.loader.ConfigLoaderUtils;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.factory.ConfigNodeFactory;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryConfig;
import org.github.gestalt.config.utils.GResultOf;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating a S3 Config Node from parameters.
 *
 * <p>Load a config source from a File then converts it to a config node
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class S3ConfigNodeFactory implements ConfigNodeFactory {
    public static final String SOURCE_TYPE = "s3";
    public static final String PARAMETER_BUCKET = "bucket";
    public static final String PARAMETER_KEY = "key";
    private static final System.Logger logger = System.getLogger(AWSSecretTransformer.class.getName());
    private ConfigLoaderService configLoaderService;
    private S3Client s3Client;

    @Override
    public void applyConfig(ConfigNodeFactoryConfig config) {
        this.configLoaderService = config.getConfigLoaderService();
        AWSModuleConfig moduleConfig = config.getConfig().getModuleConfig(AWSModuleConfig.class);

        if (moduleConfig == null) {
            logger.log(System.Logger.Level.WARNING, "AWSModuleConfig has not been registered. " +
                "if you wish to use the aws module with S3 Config Node substitution ${include}=source=s3,bucket=test,key=my.properties " +
                "then you must register an AWSModuleConfig config moduleConfig using the builder. " +
                "S3 config node substitution/include will not work");
        } else {
            if (moduleConfig.hasS3Client()) {
                s3Client = moduleConfig.getS3Client();
            } else {
                logger.log(System.Logger.Level.ERROR, "AWSModuleConfig was registered but the S3 client " +
                    "was not provided. S3 config node substitution/include will not work");
            }
        }
    }

    @Override
    public Boolean supportsType(String type) {
        return SOURCE_TYPE.equalsIgnoreCase(type);
    }

    @Override
    public GResultOf<List<ConfigNode>> build(Map<String, String> parameters) {

        if (s3Client == null) {
            return GResultOf.errors(new AWSValidationErrors.AWSS3ClientConfigNotSet(SOURCE_TYPE, parameters.toString()));
        }

        var configSourceBuilder = S3ConfigSourceBuilder.builder();

        List<ValidationError> errors = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                switch (entry.getKey()) {
                    case PARAMETER_BUCKET:
                        configSourceBuilder.setBucketName(entry.getValue());
                        break;
                    case PARAMETER_KEY:
                        configSourceBuilder.setKeyName(entry.getValue());
                        break;
                    default:
                        errors.add(
                            new ValidationError.ConfigSourceFactoryUnknownParameter(SOURCE_TYPE, entry.getKey(), entry.getValue()));
                        break;
                }
            }

            configSourceBuilder.setS3(s3Client);

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
