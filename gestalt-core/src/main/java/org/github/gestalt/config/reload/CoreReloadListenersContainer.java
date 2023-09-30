package org.github.gestalt.config.reload;

import java.util.ArrayList;
import java.util.List;

/**
 * Store all core reload listeners and functionality to call the on reload.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class CoreReloadListenersContainer {
    /**
     * Listeners for the core reload.
     */
    protected final List<CoreReloadListener> listeners = new ArrayList<>();

    /**
     * Default constructor for CoreReloadStrategy.
     */
    public CoreReloadListenersContainer() {
    }

    /**
     * register a core event listener.
     *
     * @param listener to register
     */
    public void registerListener(CoreReloadListener listener) {
        listeners.add(listener);
    }

    /**
     * remove a core event listener.
     *
     * @param listener to remove
     */
    public void removeListener(CoreReloadListener listener) {
        listeners.remove(listener);
    }

    /**
     * called when the core has reloaded.
     */
    public void reload() {
        listeners.forEach(CoreReloadListener::reload);
    }
}
