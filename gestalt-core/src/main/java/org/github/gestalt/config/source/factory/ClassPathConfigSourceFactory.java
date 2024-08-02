package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.source.ClassPathConfigSourceBuilder;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating a Classpath Config Source from parameters
 *
 * <p>Load a config source from a classpath resource using the getResourceAsStream method.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class ClassPathConfigSourceFactory implements ConfigSourceFactory {

    public static final String SOURCE_TYPE = "classPath";
    public static final String PARAMETER_RESOURCE = "resource";

    @Override
    public Boolean supportsSource(String sourceName) {
        return SOURCE_TYPE.equalsIgnoreCase(sourceName);
    }

    @Override
    public GResultOf<ConfigSource> build(Map<String, String> parameters) {

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

            return GResultOf.resultOf(classPathConfigSourceBuilder.build().getConfigSource(), errors);
        } catch (Exception ex) {
            errors.add(new ValidationError.ConfigSourceFactoryException(SOURCE_TYPE, ex));
            return GResultOf.errors(errors);
        }
    }
}
