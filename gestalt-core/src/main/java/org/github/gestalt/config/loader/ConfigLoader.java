package org.github.gestalt.config.loader;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Interface for a config loader.
 * Allows a loader to specify what types of configs it will load, property, environment variables, ect..
 * Each source has a format, and this will load the matching sources.
 *
 * <p>A config loader knows how to load a specific format, so a property loader will load a stream of properties.
 * An Environment Vars loader will load a list of Environment Variables.
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
