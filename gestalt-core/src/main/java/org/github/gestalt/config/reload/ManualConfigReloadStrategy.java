package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;

/**
 * Reloads a source when called.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ManualConfigReloadStrategy extends ConfigReloadStrategy {

    /**
     * Constructor for ManualConfigReloadStrategy with no constructor.
     */
    public ManualConfigReloadStrategy() {
    }

    /**
     * Constructor for ManualConfigReloadStrategy.
     *
     * @param source the config source to reload
     */
    public ManualConfigReloadStrategy(ConfigSource source) {
        super(source);
    }

    /**
     * Force a manual reload.
     *
     * @throws GestaltException any exceptions from reloading
     */
    @Override
    public void reload() throws GestaltException {
        super.reload();
    }
}
