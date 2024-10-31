package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.metadata.IsEncryptedMetadata;
import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.utils.GResultOf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncryptionAnnotationMetadataTransform implements AnnotationMetadataTransform {
    @Override
    public String name() {
        return "encrypt";
    }

    @Override
    public GResultOf<Map<String, List<MetaDataValue<?>>>> annotationTransform(String name, String parameter) {
        boolean value = true;
        if (parameter != null && !parameter.isEmpty()) {
            value = Boolean.parseBoolean(parameter);
        }

        Map<String, List<MetaDataValue<?>>> metadata = new HashMap<>();

        // Add IsNoCacheMetadata entry
        metadata.put(IsNoCacheMetadata.NO_CACHE, List.of(new IsNoCacheMetadata(value)));
        // Add IsSecretMetadata entry
        metadata.put(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(value)));
        // Add IsTemporaryMetadata entry
        metadata.put(IsEncryptedMetadata.ENCRYPTED, List.of(new IsEncryptedMetadata(value)));

        return GResultOf.result(metadata);
    }
}
