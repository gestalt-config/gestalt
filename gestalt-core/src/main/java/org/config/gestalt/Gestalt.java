package org.config.gestalt;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.reflect.TypeCapture;

/**
 * Main API to get build and get configurations.
 *
 * @author Colin Redmond
 */
public interface Gestalt {

    /**
     * Loads the configurations from the source and builds a config tree.
     *
     * @throws GestaltException any errors
     */
    void loadConfigs() throws GestaltException;

    /**
     * Get a config for a path and a given class.
     *
     * @param path path to get the config for.
     * @param klass class to get the class for.
     * @param <T> type of class to get.
     * @return the configuration.
     * @throws GestaltException any errors such as if there are no configs.
     */
    <T> T getConfig(String path, Class<T> klass) throws GestaltException;

    /**
     * Get a config for a path and a given TypeCapture.
     *
     * @param path path to get the config for.
     * @param klass TypeCapture to get the class for.
     * @param <T> type of class to get.
     * @return the configuration.
     * @throws GestaltException any errors such as if there are no configs.
     */
    <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException;

    /**
     * Get a config for a path and a given class.
     * If the config is missing or invalid it will return the default value.
     *
     * @param path path to get the config for.
     * @param defaultVal the default value to return if the config is invalid.
     * @param klass class to get the class for.
     * @param <T> type of class to get.
     * @return the configuration, or the default if the configuration is not found.
     */
    <T> T getConfig(String path, T defaultVal, Class<T> klass);

    /**
     * Get a config for a path and a given class.
     * If the config is missing or invalid it will return the default value.
     *
     * @param path path to get the config for.
     * @param defaultVal the default value to return if the config is invalid.
     * @param klass TypeCapture to get the class for.
     * @param <T> type of class to get.
     * @return the configuration, or the default if the configuration is not found.
     */
    <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass);
}
