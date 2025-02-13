package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;

/**
 * Listener for when configs need to reloads. This is for use internally, end users most likely should not use this.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface ConfigReloadListener {
    /**
     * Called when a config needs to be reloaded.
     *
     * @param source the source we should reload.
     * @throws GestaltException any exceptions
     */
    void reload(ConfigSourcePackage source) throws GestaltException;
}
