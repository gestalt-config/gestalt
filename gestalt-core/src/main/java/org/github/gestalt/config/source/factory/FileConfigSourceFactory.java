package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.FileConfigSourceBuilder;
import org.github.gestalt.config.utils.GResultOf;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating a File Config Source from parameters
 *
 * <p>Load a config source from a classpath resource using the getResourceAsStream method.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class FileConfigSourceFactory implements ConfigSourceFactory {

    public static final String SOURCE_TYPE = "file";
    public static final String PARAMETER_PATH = "path";
    public static final String PARAMETER_FILE = "file";

    @Override
    public Boolean supportsSource(String sourceName) {
        return SOURCE_TYPE.equalsIgnoreCase(sourceName);
    }

    @Override
    public GResultOf<ConfigSource> build(Map<String, String> parameters) {

        var fileConfigSourceBuilder = FileConfigSourceBuilder.builder();

        List<ValidationError> errors = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                switch (entry.getKey()) {
                    case PARAMETER_PATH:
                        fileConfigSourceBuilder.setPath(Path.of(entry.getValue()));
                        break;
                    case PARAMETER_FILE:
                        fileConfigSourceBuilder.setFile(new File(entry.getValue()));
                        break;
                    default:
                        errors.add(
                            new ValidationError.ConfigSourceFactoryUnknownParameter(SOURCE_TYPE, entry.getKey(), entry.getValue()));
                        break;
                }
            }

            return GResultOf.resultOf(fileConfigSourceBuilder.build().getConfigSource(), errors);
        } catch (Exception ex) {
            errors.add(new ValidationError.ConfigSourceFactoryException(SOURCE_TYPE, ex));
            return GResultOf.errors(errors);
        }
    }
}
