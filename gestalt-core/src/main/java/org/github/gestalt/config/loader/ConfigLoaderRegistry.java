package org.github.gestalt.config.loader;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ConfigLoaderRegistry.
 *
 * @author Colin Redmond
 */
public class ConfigLoaderRegistry implements ConfigLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoaderRegistry.class.getName());

    private List<ConfigLoader> configLoaders = new ArrayList<>();

    /**
     * Default constructor for the ConfigLoaderRegistry.
     */
    public ConfigLoaderRegistry() {
    }

    @Override
    public void addLoaders(List<ConfigLoader> configLoaders) {
        this.configLoaders.addAll(configLoaders);
    }

    @Override
    public void addLoader(ConfigLoader configLoader) {
        this.configLoaders.add(configLoader);
    }

    @Override
    public void setLoaders(List<ConfigLoader> configLoaders) {
        this.configLoaders = configLoaders;
    }

    @Override
    public List<ConfigLoader> getConfigLoaders() {
        return configLoaders;
    }

    @Override
    public ConfigLoader getLoader(String format) throws GestaltConfigurationException {
        List<ConfigLoader> matchingConfig = configLoaders
            .stream()
            .filter(config -> config.accepts(format))
            .collect(Collectors.toList());
        if (matchingConfig.isEmpty()) {
            throw new GestaltConfigurationException("Unable to find a config loader to match: " + format);
        } else if (matchingConfig.size() > 1) {
            logger.warn("Found more than one configuration loaderFor format: {}, found: {}", format, matchingConfig);
        }
        return matchingConfig.get(0);
    }
}
