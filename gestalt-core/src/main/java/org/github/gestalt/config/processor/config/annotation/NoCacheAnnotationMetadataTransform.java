package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.MetaDataValue;

import java.util.List;
import java.util.Map;

public class NoCacheAnnotationMetadataTransform implements AnnotationMetadataTransform {
    @Override
    public String name() {
        return "nocache";
    }

    @Override
    public Map<String, List<MetaDataValue<?>>> annotationTransform(String name, String parameter) {
        boolean value = true;
        if (parameter != null && !parameter.isEmpty()) {
            value = Boolean.parseBoolean(parameter);
        }

        return Map.of(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(value)));
    }
}
