package org.config.gestalt.reload;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.source.ConfigSource;

/**
 * Listener for when configs need to reloads.
 *
 * @author Colin Redmond
 */
public interface ConfigReloadListener {
    /**
     * Called when a config needs to be reloaded.
     *
     * @param source the source we should reload.
     * @throws GestaltException any exceptions
     */
    void reload(ConfigSource source) throws GestaltException;
}
