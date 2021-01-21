package org.config.gestalt.loader;

import org.config.gestalt.exceptions.ConfigurationException;

import java.util.List;

/**
 * Maintains all config loaders.
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
     * get the current config loaders
     *
     * @return current config loaders
     */
    List<ConfigLoader> getConfigLoaders();

    /**
     * find the config loader matching the format.
     *
     * @param format format we want to load.
     * @return the config loader matching the format.
     * @throws ConfigurationException any exception
     */
    ConfigLoader getLoader(String format) throws ConfigurationException;
}
