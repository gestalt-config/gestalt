package org.config.gestalt.reload;

import java.util.ArrayList;
import java.util.List;

public class CoreReloadStrategy {
    protected final List<CoreReloadListener> listeners = new ArrayList<>();

    public CoreReloadStrategy() {
    }

    public void registerListener(CoreReloadListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CoreReloadListener listener) {
        listeners.remove(listener);
    }

    public void reload() {
        listeners.forEach(CoreReloadListener::reload);
    }
}
