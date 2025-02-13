package org.github.gestalt.config.node.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.loader.ConfigLoaderUtils;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.KubernetesSecretConfigSourceBuilder;
import org.github.gestalt.config.utils.GResultOf;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating a Kubernetes Secret Config Node from parameters.
 *
 * <p>Load a config source from a Kubernetes Secret then converts it to a config node
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class KubernetesSecretConfigNodeFactory implements ConfigNodeFactory {

    public static final String SOURCE_TYPE = "k8Secret";
    public static final String PARAMETER_PATH = "path";
    public static final String PARAMETER_FILE = "file";

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

        var k8ConfigSourceBuilder = KubernetesSecretConfigSourceBuilder.builder();

        List<ValidationError> errors = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                switch (entry.getKey()) {
                    case PARAMETER_PATH:
                        k8ConfigSourceBuilder.setPath(Path.of(entry.getValue()));
                        break;
                    case PARAMETER_FILE:
                        k8ConfigSourceBuilder.setFile(new File(entry.getValue()));
                        break;
                    default:
                        errors.add(
                            new ValidationError.ConfigSourceFactoryUnknownParameter(SOURCE_TYPE, entry.getKey(), entry.getValue()));
                        break;
                }
            }

            var fileConfigSource = k8ConfigSourceBuilder.build().getConfigSource();

            GResultOf<List<ConfigNode>> loadedNodes = ConfigLoaderUtils.convertSourceToNodes(fileConfigSource, configLoaderService);
            errors.addAll(loadedNodes.getErrors());

            return GResultOf.resultOf(loadedNodes.results(), errors);
        } catch (Exception ex) {
            errors.add(new ValidationError.ConfigSourceFactoryException(SOURCE_TYPE, ex));
            return GResultOf.errors(errors);
        }
    }
}
