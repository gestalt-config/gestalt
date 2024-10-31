package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.github.gestalt.config.metadata.IsTemporaryMetadata;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemporaryAnnotationMetadataTransform implements AnnotationMetadataTransform {
    @Override
    public String name() {
        return "temp";
    }

    @Override
    public GResultOf<Map<String, List<MetaDataValue<?>>>> annotationTransform(String name, String parameter) {
        int value = 1;
        if (parameter != null && !parameter.isEmpty() && StringUtils.isInteger(parameter)) {
            value = Integer.parseInt(parameter);
        }

        Map<String, List<MetaDataValue<?>>> metadata = new HashMap<>();

        // Add IsNoCacheMetadata entry
        metadata.put(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(true)));
        // Add IsSecretMetadata entry
        metadata.put(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true)));
        // Add IsTemporaryMetadata entry
        metadata.put(IsTemporaryMetadata.TEMPORARY, List.of(new IsTemporaryMetadata(value)));

        return GResultOf.result(metadata);
    }
}
