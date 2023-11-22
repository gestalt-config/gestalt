package org.github.gestalt.config.reload;

import org.github.gestalt.config.builder.ConfigReloadStrategyBuilder;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;

import java.time.Duration;

/**
 *  ConfigReloadStrategyBuilder for the Timed Config Reload Strategy Builder.
 *
 *  <p>Convert the System properties into a config source.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class TimedConfigReloadStrategyBuilder
    extends ConfigReloadStrategyBuilder<TimedConfigReloadStrategyBuilder, TimedConfigReloadStrategy> {

    private Duration reloadDelay;

    public static TimedConfigReloadStrategyBuilder builder() {
        return new TimedConfigReloadStrategyBuilder();
    }

    public Duration getReloadDelay() {
        return reloadDelay;
    }

    public TimedConfigReloadStrategyBuilder setReloadDelay(Duration reloadDelay) {
        this.reloadDelay = reloadDelay;
        return this;
    }

    @Override
    public TimedConfigReloadStrategy build() throws GestaltConfigurationException {
        if (this.source == null) {
            throw new GestaltConfigurationException(
                "When building a Timed Change Reload Strategy with the builder you must set a source");
        }

        if (this.reloadDelay == null) {
            throw new GestaltConfigurationException(
                "When building a Timed Change Reload Strategy with the builder the Reload Delay must be set");
        }

        return new TimedConfigReloadStrategy(this.source, reloadDelay);
    }
}
