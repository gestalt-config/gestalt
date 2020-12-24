package org.config.gestalt.entity;

import java.util.Objects;

public class ConfigValue {
    private final String value;

    public ConfigValue(String value) {
        this.value = Objects.requireNonNull(value, "value can not be null");
    }

    public String getValue() {
        return value;
    }
}
