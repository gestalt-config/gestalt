package org.github.gestalt.config.node.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.loader.ConfigLoaderUtils;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.EnvironmentConfigSourceBuilder;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating a File Config Node from parameters.
 *
 * <p>Load a config source from a File then converts it to a config node
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class EnvVarsConfigNodeFactory implements ConfigNodeFactory {

    public static final String SOURCE_TYPE = "env";
    public static final String PARAMETER_FAIL_ON_ERRORS = "failOnErrors";
    public static final String PARAMETER_PREFIX = "prefix";
    public static final String PARAMETER_IGNORE_CASE_ON_PREFIX = "ignoreCaseOnPrefix";
    public static final String PARAMETER_REMOVE_PREFIX = "removePrefix";

    private ConfigLoaderService configLoaderService;

    @Override
    public void applyConfig(ConfigNodeFactoryConfig config) {
        this.configLoaderService = config.getConfigLoaderService();
    }

    @Override
    public Boolean supportsType(String type) {
        return SOURCE_TYPE.equalsIgnoreCase(type);
    }

    @Override
    public GResultOf<List<ConfigNode>> build(Map<String, String> parameters) {

        var envConfigSourceBuilder = EnvironmentConfigSourceBuilder.builder();

        List<ValidationError> errors = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                switch (entry.getKey()) {
                    case PARAMETER_FAIL_ON_ERRORS:
                        envConfigSourceBuilder.setFailOnErrors(Boolean.parseBoolean(entry.getValue()));
                        break;
                    case PARAMETER_PREFIX:
                        envConfigSourceBuilder.setPrefix(entry.getValue());
                        break;
                    case PARAMETER_IGNORE_CASE_ON_PREFIX:
                        envConfigSourceBuilder.setIgnoreCaseOnPrefix(Boolean.parseBoolean(entry.getValue()));
                        break;
                    case PARAMETER_REMOVE_PREFIX:
                        envConfigSourceBuilder.setRemovePrefix(Boolean.parseBoolean(entry.getValue()));
                        break;
                    default:
                        errors.add(
                            new ValidationError.ConfigSourceFactoryUnknownParameter(SOURCE_TYPE, entry.getKey(), entry.getValue()));
                        break;
                }
            }

            var envConfigSource = envConfigSourceBuilder.build().getConfigSource();

            GResultOf<List<ConfigNode>> loadedNodes = ConfigLoaderUtils.convertSourceToNodes(envConfigSource, configLoaderService);
            errors.addAll(loadedNodes.getErrors());

            return GResultOf.resultOf(loadedNodes.results(), errors);
        } catch (Exception ex) {
            errors.add(new ValidationError.ConfigSourceFactoryException(SOURCE_TYPE, ex));
            return GResultOf.errors(errors);
        }
    }
}
