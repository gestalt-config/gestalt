package org.github.gestalt.config.reload;

import org.github.gestalt.config.builder.ConfigReloadStrategyBuilder;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;

/**
 * Builder for the ManualConfigReloadStrategy.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class ManualConfigReloadStrategyBuilder
    extends ConfigReloadStrategyBuilder<ManualConfigReloadStrategyBuilder, ManualConfigReloadStrategy> {

    public static ManualConfigReloadStrategyBuilder builder() {
        return new ManualConfigReloadStrategyBuilder();
    }

    @Override
    public ManualConfigReloadStrategy build() throws GestaltConfigurationException {
        if (this.source == null) {
            throw new GestaltConfigurationException(
                "When building a Manual Change Reload Strategy with the builder you must set a source");
        }

        return new ManualConfigReloadStrategy(this.source);
    }
}
