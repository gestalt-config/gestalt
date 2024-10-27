package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.github.gestalt.config.metadata.MetaDataValue;

import java.util.List;
import java.util.Map;

public class SecretAnnotationMetadataTransform implements AnnotationMetadataTransform {
    @Override
    public String name() {
        return "secret";
    }

    @Override
    public Map<String, List<MetaDataValue<?>>> annotationTransform(String name, String parameter) {
        return Map.of(IsSecretMetadata.IS_SECRET_METADATA, List.of(new IsSecretMetadata(true)),
            IsNoCacheMetadata.NO_CACHE_METADATA, List.of(new IsNoCacheMetadata(true)));
    }
}
