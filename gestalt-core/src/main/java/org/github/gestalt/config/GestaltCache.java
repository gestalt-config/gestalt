package org.github.gestalt.config;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.utils.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache layer that stores configurations by path and type.
 * Expects to be registered as a CoreReloadListener and will clear the configs
 *
 * @author Colin Redmond
 */
public class GestaltCache implements Gestalt, CoreReloadListener {
    private final Gestalt delegate;
    private final Map<Pair<String, TypeCapture<?>>, Object> cache = new ConcurrentHashMap<>();

    public GestaltCache(Gestalt delegate) {
        this.delegate = delegate;
    }

    @Override
    public void loadConfigs() throws GestaltException {
        delegate.loadConfigs();
        cache.clear();
    }

    @Override
    public <T> T getConfig(String path, Class<T> klass) throws GestaltException {
        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfig(path, typeCapture);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException {
        Pair<String, TypeCapture<?>> key = new Pair<>(path, klass);
        if (cache.containsKey(key)) {
            return (T) cache.get(key);
        } else {
            T result = delegate.getConfig(path, klass);
            if (result != null) {
                cache.put(key, result);
            }
            return result;
        }
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, Class<T> klass) {
        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfig(path, defaultVal, typeCapture);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass) {
        Pair<String, TypeCapture<?>> key = new Pair<>(path, klass);
        return (T) cache.computeIfAbsent(key, k -> delegate.getConfig(path, defaultVal, klass));
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass) {
        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfigOptional(path, typeCapture);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass) {
        Pair<String, TypeCapture<?>> key = new Pair<>(path, klass);
        if (cache.containsKey(key)) {
            return Optional.of((T) cache.get(key));
        } else {
            Optional<T> result = delegate.getConfigOptional(path, klass);
            if (result != null && result.isPresent()) {
                cache.put(key, result.get());
            }

            return result;
        }
    }

    @Override
    public void reload() {
        cache.clear();
    }
}
