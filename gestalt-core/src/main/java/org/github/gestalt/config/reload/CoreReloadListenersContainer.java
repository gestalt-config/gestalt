package org.github.gestalt.config.reload;

import java.util.WeakHashMap;

/**
 * Store all core reload listeners and functionality to call the on reload.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class CoreReloadListenersContainer {
    /**
     * Listeners for the core reload.
     */
    protected final WeakHashMap<Integer, CoreReloadListener> listeners = new WeakHashMap<>();

    /**
     * register a core event listener.
     *
     * @param listener to register
     */
    public void registerListener(CoreReloadListener listener) {
        listeners.put(listener.hashCode(), listener);
    }

    /**
     * remove a core event listener.
     *
     * @param listener to remove
     */
    public void removeListener(CoreReloadListener listener) {
        listeners.remove(listener.hashCode());
    }

    /**
     * called when the core has reloaded.
     */
    public void reload() {
        listeners.forEach((k, v) -> v.reload());
    }
}
