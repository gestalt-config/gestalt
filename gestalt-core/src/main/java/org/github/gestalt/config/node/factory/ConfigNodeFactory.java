package org.github.gestalt.config.node.factory;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;
import java.util.Map;

/**
 * Factory to build Config Node from a set of parameters.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface ConfigNodeFactory {

    default void applyConfig(ConfigNodeFactoryConfig config) {

    }

    /**
     * Returns true if it supports a specific config Node type.
     *
     * @param type type of the config Node
     * @return true if it supports a specific config type.
     */
    Boolean supportsType(String type);

    /**
     * Takes in a map of parameters to then use a builder to generate a Config Node.
     *
     * @param parameters parameters used to define a config Node, such as file location, or url
     * @return Config Node
     */
    GResultOf<List<ConfigNode>> build(Map<String, String> parameters);
}
