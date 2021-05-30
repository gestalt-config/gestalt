package org.github.gestalt.config.entity;

import java.util.Objects;

/**
 * Value for a config. Used to store the leaf value while parsing the configs.
 *
 * @author Colin Redmond
 */
public class ConfigValue {
    private final String value;

    /**
     * Constructor to hold a config value.
     *
     * @param value to hld hor the config
     */
    public ConfigValue(String value) {
        this.value = Objects.requireNonNull(value, "value can not be null");
    }

    /**
     * Get the config value.
     *
     * @return config value
     */
    public String getValue() {
        return value;
    }
}
