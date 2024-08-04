package org.github.gestalt.config.node.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.loader.ConfigLoaderUtils;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.ClassPathConfigSourceBuilder;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating a Classpath Config Node from parameters
 *
 * <p>Load a config source from a classpath then converts it to a config node
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class ClassPathConfigNodeFactory implements ConfigNodeFactory {

    public static final String SOURCE_TYPE = "classPath";
    public static final String PARAMETER_RESOURCE = "resource";

    private ConfigLoaderService configLoaderService;

    public void applyConfig(ConfigNodeFactoryConfig config) {
        this.configLoaderService = config.getConfigLoaderService();
    }

    @Override
    public Boolean supportsType(String type) {
        return SOURCE_TYPE.equalsIgnoreCase(type);
    }

    @Override
    public GResultOf<List<ConfigNode>> build(Map<String, String> parameters) {

        var classPathConfigSourceBuilder = ClassPathConfigSourceBuilder.builder();

        List<ValidationError> errors = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (PARAMETER_RESOURCE.equalsIgnoreCase(entry.getKey())) {
                    classPathConfigSourceBuilder.setResource(entry.getValue());
                } else {
                    errors.add(
                        new ValidationError.ConfigSourceFactoryUnknownParameter(SOURCE_TYPE, entry.getKey(), entry.getValue()));
                }
            }
            var source = classPathConfigSourceBuilder.build().getConfigSource();

            GResultOf<List<ConfigNode>> loadedNodes = ConfigLoaderUtils.convertSourceToNodes(source, configLoaderService);
            errors.addAll(loadedNodes.getErrors());

            return GResultOf.resultOf(loadedNodes.results(), errors);

        } catch (Exception ex) {
            errors.add(new ValidationError.ConfigSourceFactoryException(SOURCE_TYPE, ex));
            return GResultOf.errors(errors);
        }
    }


}
