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
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public abstract class ConfigReloadStrategy {

    /**
     * The listeners for the Config Reload.
     */
    protected final List<ConfigReloadListener> listeners = new ArrayList<>();

    /**
     * The source we are listening for a reload.
     */
    protected final ConfigSource source;

    /**
     * Protected constructor for the ConfigReloadStrategy. So end users cant create this class, only inherit it.
     *
     * @param source source we are listening for a reload
     */
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
