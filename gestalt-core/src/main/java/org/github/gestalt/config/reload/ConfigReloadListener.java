package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;

/**
 * Listener for when configs need to reloads.
 *
 * @author <a href="mailto:colin.redmond@outlook.com">Colin Redmond (c) 2023.
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
