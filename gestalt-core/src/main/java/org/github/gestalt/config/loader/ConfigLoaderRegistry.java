package org.github.gestalt.config.loader;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.WARNING;

/**
 * ConfigLoaderRegistry.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class ConfigLoaderRegistry implements ConfigLoaderService {

    private static final System.Logger logger = System.getLogger(ConfigLoaderRegistry.class.getName());

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
            logger.log(WARNING, "Found more than one configuration loaderFor format: {0}, found: {1}", format, matchingConfig);
        }
        return matchingConfig.get(0);
    }
}
