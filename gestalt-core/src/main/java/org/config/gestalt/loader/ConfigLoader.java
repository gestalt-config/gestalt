package org.config.gestalt.loader;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.source.ConfigSource;
import org.config.gestalt.utils.ValidateOf;

/**
 * Interface for a config loader.
 * Allows a config to specify what types of configs it will load, property, environment variables, ect..
 *
 * @author Colin Redmond
 */
public interface ConfigLoader {
    /**
     * Name of the config loader.
     *
     * @return name
     */
    String name();

    /**
     * True if the config loader accepts the format.
     *
     * @param format config format.
     * @return True if the config loader accepts the format.
     */
    boolean accepts(String format);

    /**
     * Load a ConfigSource then build the validated config node.
     *
     * @param source source we want to load with this config loader.
     * @return the validated config node.
     * @throws GestaltException any exceptions
     */
    ValidateOf<ConfigNode> loadSource(ConfigSource source) throws GestaltException;
}
