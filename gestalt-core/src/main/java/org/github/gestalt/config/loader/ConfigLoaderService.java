package org.github.gestalt.config.loader;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;

import java.util.List;

/**
 * Maintains all config loaders. Provides methods for registering and searching for config loaders
 *
 * @author Colin Redmond
 */
public interface ConfigLoaderService {

    /**
     * Add a list of config loaders.
     *
     * @param configLoaders config loaders to add.
     */
    void addLoaders(List<ConfigLoader> configLoaders);

    /**
     * add a config loader.
     *
     * @param configLoader add a config loader
     */
    void addLoader(ConfigLoader configLoader);

    /**
     * Set a list of config loaders. Will replace any current config loaders.
     *
     * @param configLoaders list of config loaders
     */
    void setLoaders(List<ConfigLoader> configLoaders);

    /**
     * get the current config loaders.
     *
     * @return current config loaders
     */
    List<ConfigLoader> getConfigLoaders();

    /**
     * find the first config loader matching the format.
     *
     * @param format format we want to load.
     * @return the config loader matching the format.
     * @throws GestaltConfigurationException if there are no matching config loaders for the format.
     */
    ConfigLoader getLoader(String format) throws GestaltConfigurationException;
}
