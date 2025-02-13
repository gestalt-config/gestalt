package org.github.gestalt.config.source;

import org.github.gestalt.config.reload.ConfigReloadStrategy;
import org.github.gestalt.config.tag.Tags;

import java.util.List;
import java.util.Objects;

/**
 * Container that holds the Config Source as well as the configReloadStrategies.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class ConfigSourcePackage {
    private final ConfigSource configSource;
    private final List<ConfigReloadStrategy> configReloadStrategies;

    private final Tags tags;

    /**
     * Constructor for the ConfigSourcePackage that requires the config source and the configReloadStrategies.
     *
     * @param configSource           the config source
     * @param configReloadStrategies the configReloadStrategies
     * @param tags                   the tags associated with the config source package
     */
    public ConfigSourcePackage(ConfigSource configSource, List<ConfigReloadStrategy> configReloadStrategies, Tags tags) {
        this.configSource = configSource;
        this.configReloadStrategies = configReloadStrategies;
        this.tags = tags;
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

    /**
     * Get the tags associated with the config source.
     *
     * @return the tags associated with the config source
     */
    public Tags getTags() {

        // remove this once we remove the tags from the config source.
        return tags.and(configSource.getTags());
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
