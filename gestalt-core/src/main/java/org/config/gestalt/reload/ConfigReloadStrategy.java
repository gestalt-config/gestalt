package org.config.gestalt.reload;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.source.ConfigSource;

import java.util.ArrayList;
import java.util.List;

public class ConfigReloadStrategy {

    protected final List<ConfigReloadListener> listeners = new ArrayList<>();

    protected final ConfigSource source;

    protected ConfigReloadStrategy(ConfigSource source) {
        this.source = source;
    }

    public void registerListener(ConfigReloadListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ConfigReloadListener listener) {
        listeners.remove(listener);
    }

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
