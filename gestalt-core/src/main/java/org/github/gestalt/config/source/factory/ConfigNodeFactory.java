package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;
import java.util.Map;

/**
 * Factory to build Config Source from a set of parameters.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface ConfigNodeFactory {

    default void applyConfig(ConfigNodeFactoryConfig config) {

    }

    /**
     * Returns true if it supports a specific config source sourceName.
     *
     * @param sourceName sourceName of the config source
     * @return true if it supports a specific config source sourceName.
     */
    Boolean supportsSource(String sourceName);

    /**
     * Takes in a map of parameters to then use a builder to generate a Config Source.
     *
     * @param parameters parameters used to define a configSource, such as file location, or url
     * @return Config Source
     */
    GResultOf<List<ConfigNode>> build(Map<String, String> parameters);
}
