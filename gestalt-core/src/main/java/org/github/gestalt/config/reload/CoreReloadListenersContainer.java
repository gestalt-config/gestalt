package org.github.gestalt.config.reload;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Store all core reload listeners and functionality to call the on reload.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class CoreReloadListenersContainer {

    /**
     * Listeners for the core reload.
     */
    protected final List<WeakReference<CoreReloadListener>> listeners = new ArrayList<>();

    /**
     * register a core event listener.
     *
     * @param listener to register
     */
    public void registerListener(CoreReloadListener listener) {
        listeners.add(new WeakReference<>(listener));
    }

    /**
     * remove a core event listener.
     *
     * @param listener to remove
     */
    public void removeListener(CoreReloadListener listener) {
        cleanup();
        listeners.removeIf((it) -> it.get() == null || it.get() == listener);
    }

    /**
     * cleanup the listeners and removes expired ones.
     */
    private void cleanup() {
        listeners.removeIf((it) -> it.get() == null);
    }

    /**
     * Get the current listeners.
     *
     * @return the current listeners
     */
    public List<WeakReference<CoreReloadListener>> getListeners() {
        cleanup();
        return listeners;
    }

    /**
     * called when the core has reloaded.
     */
    public void reload() {
        cleanup();
        listeners.forEach((it) -> {
            var weakRef = it.get();
            if (weakRef != null) {
                weakRef.reload();
            }
        });
    }
}
