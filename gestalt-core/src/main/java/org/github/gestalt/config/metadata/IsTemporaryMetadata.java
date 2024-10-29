package org.github.gestalt.config.metadata;

import java.util.List;
import java.util.Map;

public class IsTemporaryMetadata extends MetaDataValue<Integer> {
    public static String TEMPORARY = "temporary";

    public IsTemporaryMetadata(Integer value) {
        super(value);
    }

    @Override
    public String keyValue() {
        return TEMPORARY;
    }

    // We do not want to rollup the IsSecretMetadata to the calling metadata
    @Override
    public Map<String, List<MetaDataValue<?>>> rollup(Map<String, List<MetaDataValue<?>>> metadata) {
        return metadata;
    }
}
