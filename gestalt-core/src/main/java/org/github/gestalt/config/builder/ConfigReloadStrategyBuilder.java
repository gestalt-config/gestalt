package org.github.gestalt.config.builder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.reload.ConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSource;

import java.util.Objects;

/**
 * Abstract builder for the ConfigReloadStrategy.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public abstract class ConfigReloadStrategyBuilder
    <SELF extends ConfigReloadStrategyBuilder<SELF, T>, T extends ConfigReloadStrategy> { //NOPMD
    protected ConfigSource source;

    @SuppressWarnings("unchecked")
    protected SELF self() {
        return (SELF) this;
    }

    /**
     * Get the source for the ConfigReloadStrategy.
     *
     * @return the source for the ConfigReloadStrategy
     */
    public ConfigSource getSource() {
        return source;
    }

    /**
     * Set the source for the ConfigReloadStrategy.
     * You dont need to call this yourself, if you pass this into ConfigReloadStrategyBuilder
     * it will set the ConfigSource and call build.
     *
     * @param source the source for the ConfigReloadStrategy
     * @return the builder
     */
    public SELF setSource(ConfigSource source) {
        Objects.requireNonNull(source, "config source must not be null");
        this.source = source;
        return self();
    }

    /**
     * Build the implementation of the ConfigReloadStrategy.
     *
     * @return the implementation of the ConfigReloadStrategy
     * @throws GestaltConfigurationException any exceptions while building.
     */
    public abstract T build() throws GestaltConfigurationException;
}
