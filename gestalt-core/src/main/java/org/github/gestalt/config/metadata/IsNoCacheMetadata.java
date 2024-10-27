package org.github.gestalt.config.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IsNoCacheMetadata extends MetaDataValue<Boolean> {
    public static String NO_CACHE_METADATA = "noCacheMetadata";

    public IsNoCacheMetadata(Boolean value) {
        super(value);
    }

    @Override
    public String keyValue() {
        return NO_CACHE_METADATA;
    }

    @Override
    public Map<String, List<MetaDataValue<?>>> rollup(Map<String, List<MetaDataValue<?>>> metadata) {

        // if the metadata doesnt already have the noCacheMetadata or the current noCacheMetadata is false
        // then we can update the metadata, by copying and updating with a new value.
        // We want to rollup the true up, but override a false.
        if (value && (!metadata.containsKey(NO_CACHE_METADATA) || metadata.get(NO_CACHE_METADATA).stream()
            .noneMatch(it -> (boolean) it.getMetadata()))) {
            Map<String, List<MetaDataValue<?>>> newMetadata = new HashMap<>(metadata);
            newMetadata.put(NO_CACHE_METADATA, List.of(new IsNoCacheMetadata(true)));
            return newMetadata;
        }

        // otherwise return the original metadata.
        return metadata;
    }
}
