package org.github.gestalt.config;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;

import java.util.Optional;

/**
 * Central access point to Gestalt that has API's to build and get configurations.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public interface Gestalt {

    /**
     * Loads the configurations from the source and builds a config tree.
     * For each sources it will find the config loader that matches the source format.
     * It will use the config loader to build a config node tree.
     * Then merge the configs in order. With the newer configs overwriting the older configs.
     *
     * @throws GestaltException any errors
     */
    void loadConfigs() throws GestaltException;

    /**
     * Get a config for a path and a given class.
     *
     * @param path path to get the config for. The path is not case sensitive.
     * @param klass class to get the class for.
     * @param <T> type of class to get.
     * @return the configuration.
     * @throws GestaltException any errors such as if there are no configs.
     */
    <T> T getConfig(String path, Class<T> klass) throws GestaltException;

    /**
     * Get a config for a path and a given class.
     *
     * @param path path to get the config for. The path is not case sensitive.
     * @param klass class to get the class for.
     * @param tags the tags to match while searching for configs
     * @param <T> type of class to get.
     * @return the configuration.
     * @throws GestaltException any errors such as if there are no configs.
     */
    <T> T getConfig(String path, Class<T> klass, Tags tags) throws GestaltException;

    /**
     * Get a config for a path and a given TypeCapture.
     *
     * @param path path to get the config for. The path is not case sensitive.
     * @param klass TypeCapture to get the class for.
     * @param <T> type of class to get.
     * @return the configuration.
     * @throws GestaltException any errors such as if there are no configs.
     */
    <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException;

    /**
     * Get a config for a path and a given TypeCapture.
     *
     * @param path path to get the config for. The path is not case sensitive.
     * @param klass TypeCapture to get the class for.
     * @param tags the tags to match while searching for configs
     * @param <T> type of class to get.
     * @return the configuration.
     * @throws GestaltException any errors such as if there are no configs.
     */
    <T> T getConfig(String path, TypeCapture<T> klass, Tags tags) throws GestaltException;

    /**
     * Get a config for a path and a given class.
     * If the config is missing or invalid it will return the default value.
     *
     * @param path path to get the config for. The path is not case sensitive.
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
     * @param path path to get the config for. The path is not case sensitive.
     * @param defaultVal the default value to return if the config is invalid.
     * @param klass class to get the class for.
     * @param tags the tags to match while searching for configs
     * @param <T> type of class to get.
     * @return the configuration, or the default if the configuration is not found.
     */
    <T> T getConfig(String path, T defaultVal, Class<T> klass, Tags tags);

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

    /**
     * Get a config for a path and a given class.
     * If the config is missing or invalid it will return the default value.
     *
     * @param path path to get the config for.
     * @param defaultVal the default value to return if the config is invalid.
     * @param klass TypeCapture to get the class for.
     * @param tags the tags to match while searching for configs
     * @param <T> type of class to get.
     * @return the configuration, or the default if the configuration is not found.
     */
    <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass, Tags tags);

    /**
     * Get a config Optional for a path and a given class. If there are any exceptions or errors it will return an Optional.empty()
     *
     * @param path path to get the config for. The path is not case sensitive.
     * @param klass class to get the class for.
     * @param <T> type of class to get.
     * @return the configuration or Optional.empty() if it failed.
     */
    <T> Optional<T> getConfigOptional(String path, Class<T> klass);

    /**
     * Get a config Optional for a path and a given class. If there are any exceptions or errors it will return an Optional.empty()
     *
     * @param path path to get the config for. The path is not case sensitive.
     * @param klass class to get the class for.
     * @param tags the tags to match while searching for configs
     * @param <T> type of class to get.
     * @return the configuration or Optional.empty() if it failed.
     */
    <T> Optional<T> getConfigOptional(String path, Class<T> klass, Tags tags);

    /**
     * Get a config Optional for a path and a given TypeCapture. If there are any exceptions or errors it will return an Optional.empty()
     *
     * @param path path to get the config for. The path is not case sensitive.
     * @param klass TypeCapture to get the class for.
     * @param <T> type of class to get.
     * @return the configuration or Optional.empty() if it failed.
     */
    <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass);

    /**
     * Get a config Optional for a path and a given TypeCapture. If there are any exceptions or errors it will return an Optional.empty()
     *
     * @param path path to get the config for. The path is not case sensitive.
     * @param klass TypeCapture to get the class for.
     * @param tags the tags to match while searching for configs
     * @param <T> type of class to get.
     * @return the configuration or Optional.empty() if it failed.
     */
    <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass, Tags tags);
}
