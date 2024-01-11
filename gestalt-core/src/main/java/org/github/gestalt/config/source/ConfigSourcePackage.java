package org.github.gestalt.config.source;

import org.github.gestalt.config.reload.ConfigReloadStrategy;

import java.util.List;
import java.util.Objects;

/**
 * Container that holds the Config Source as well as the configReloadStrategies.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class ConfigSourcePackage {
    private final ConfigSource configSource;
    private final List<ConfigReloadStrategy> configReloadStrategies;

    /**
     * Constructor for the ConfigSourcePackage that requires the config source and the configReloadStrategies.
     *
     * @param configSource the config source
     * @param configReloadStrategies the configReloadStrategies
     */
    public ConfigSourcePackage(ConfigSource configSource, List<ConfigReloadStrategy> configReloadStrategies) {
        this.configSource = configSource;
        this.configReloadStrategies = configReloadStrategies;
    }

    /**
     * Get the config source.
     *
     * @return the config source
     */
    public ConfigSource getConfigSource() {
        return configSource;
    }

    /**
     * Get the configReloadStrategies.
     *
     * @return the configReloadStrategies
     */
    public List<ConfigReloadStrategy> getConfigReloadStrategies() {
        return configReloadStrategies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigSourcePackage)) {
            return false;
        }

        ConfigSourcePackage that = (ConfigSourcePackage) o;
        return Objects.equals(configSource, that.configSource) && Objects.equals(configReloadStrategies, that.configReloadStrategies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configSource, configReloadStrategies);
    }
}
