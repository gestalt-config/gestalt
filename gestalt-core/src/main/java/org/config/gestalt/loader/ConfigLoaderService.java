package org.config.gestalt.loader;

import org.config.gestalt.exceptions.ConfigurationException;

import java.util.List;

public interface ConfigLoaderService {
    void addLoaders(List<ConfigLoader> configLoaders);

    void addLoader(ConfigLoader configLoader);

    void setLoaders(List<ConfigLoader> configLoaders);

    List<ConfigLoader> getConfigLoaders();

    ConfigLoader getLoader(String format) throws ConfigurationException;
}
