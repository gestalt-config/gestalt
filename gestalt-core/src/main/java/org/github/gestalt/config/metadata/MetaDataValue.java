package org.github.gestalt.config.metadata;

import java.util.List;
import java.util.Map;

public abstract class MetaDataValue<T> {

    protected final T value;

    public MetaDataValue(T value) {
        this.value = value;
    }

    public abstract String keyValue();

    public T getMetadata() {
        return value;
    }

    public abstract Map<String, List<MetaDataValue<?>>> rollup(Map<String, List<MetaDataValue<?>>> metadata);
}
