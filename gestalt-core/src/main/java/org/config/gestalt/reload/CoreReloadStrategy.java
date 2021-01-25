package org.config.gestalt.reload;

import java.util.ArrayList;
import java.util.List;

/**
 * Store all core reload listeners and functionality to call the on reload.
 *
 * @author Colin Redmond
 */
public class CoreReloadStrategy {
    protected final List<CoreReloadListener> listeners = new ArrayList<>();

    public CoreReloadStrategy() {
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
