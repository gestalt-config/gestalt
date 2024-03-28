package org.github.gestalt.config.source;

import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;

import java.util.HashMap;
import java.util.Map;

/**
 * ConfigSourceBuilder for the Map Config Source.
 *
 * <p>Allows a user to provide a custom config source as a map.
 * would take the same form as a property file with a key that can be tokenized and a value.
 * db.port = 1234
 * db.password = password
 * dp.user = notroot
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class MapConfigSourceBuilder extends SourceBuilder<MapConfigSourceBuilder, MapConfigSource> {
    private Map<String, String> customConfig;

    /**
     * private constructor, use the builder method.
     */
    private MapConfigSourceBuilder() {
        customConfig = new HashMap<>();
    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static MapConfigSourceBuilder builder() {
        return new MapConfigSourceBuilder();
    }

    /**
     * Get the map that backs the config source.
     *
     * @return the map that backs the config source.
     */
    public Map<String, String> getCustomConfig() {
        return customConfig;
    }

    /**
     * Set the map that backs the config source.
     *
     * @param customConfig the map that backs the config source.
     * @return the builder
     */
    public MapConfigSourceBuilder setCustomConfig(Map<String, String> customConfig) {
        this.customConfig = customConfig;
        return this;
    }

    /**
     * Add a new configuration with a path and a value.
     *
     * @param path  the path for the configuration
     * @param value the value for the configuration.
     * @return the builder
     */
    public MapConfigSourceBuilder addCustomConfig(String path, String value) {
        customConfig.put(path, value);
        return this;
    }


    @Override
    public ConfigSourcePackage build() throws GestaltException {
        return buildPackage(new MapConfigSource(customConfig));
    }
}
