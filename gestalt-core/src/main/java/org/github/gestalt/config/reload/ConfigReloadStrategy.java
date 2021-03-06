package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Baseclass for all reload strategies.
 * Holds all the config reload listeners and the source we want to watch for.
 * On reload it will call the listeners with the sources.
 *
 * @author Colin Redmond
 */
public class ConfigReloadStrategy {

    protected final List<ConfigReloadListener> listeners = new ArrayList<>();

    protected final ConfigSource source;

    protected ConfigReloadStrategy(ConfigSource source) {
        this.source = source;
    }

    /**
     * Add a config reload listener.
     *
     * @param listener a config reload listener.
     */
    public void registerListener(ConfigReloadListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a config reload listener.
     *
     * @param listener a config reload listener.
     */
    public void removeListener(ConfigReloadListener listener) {
        listeners.remove(listener);
    }

    /**
     * call all listeners with the reload event.
     *
     * @throws GestaltException any exceptions
     */
    protected void reload() throws GestaltException {
        List<GestaltException> exceptions = new ArrayList<>();
        for (ConfigReloadListener it : listeners) {
            try {
                it.reload(source);
            } catch (GestaltException e) {
                exceptions.add(e);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new GestaltException(exceptions);
        }
    }
}
