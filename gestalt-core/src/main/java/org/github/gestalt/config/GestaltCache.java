package org.github.gestalt.config;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Triple;

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
    private final Map<Triple<String, TypeCapture<?>, Tags>, Object> cache = new ConcurrentHashMap<>();

    /**
     * Constructor for the GestaltCache that accepts a delegate.
     *
     * @param delegate real Gestalt to call for configs to cache.
     */
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
    public <T> T getConfig(String path, Class<T> klass, Tags tags) throws GestaltException {
        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfig(path, typeCapture, tags);
    }

    @Override
    public <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException {
        return getConfig(path, klass, Tags.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {
        Triple<String, TypeCapture<?>, Tags> key = new Triple<>(path, klass, tags);
        if (cache.containsKey(key)) {
            return (T) cache.get(key);
        } else {
            T result = delegate.getConfig(path, klass, tags);
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
    public <T> T getConfig(String path, T defaultVal, Class<T> klass, Tags tags) {
        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfig(path, defaultVal, typeCapture, tags);
    }



    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass) {
        return  getConfig(path, defaultVal, klass, Tags.of());
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        Triple<String, TypeCapture<?>, Tags> key = new Triple<>(path, klass, tags);
        return (T) cache.computeIfAbsent(key, k -> delegate.getConfig(path, defaultVal, klass, tags));
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass) {
        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfigOptional(path, typeCapture);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass, Tags tags) {
        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfigOptional(path, typeCapture, tags);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass) {
        return getConfigOptional(path, klass, Tags.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass, Tags tags) {
        Triple<String, TypeCapture<?>, Tags> key = new Triple<>(path, klass, tags);
        if (cache.containsKey(key)) {
            return Optional.of((T) cache.get(key));
        } else {
            Optional<T> result = delegate.getConfigOptional(path, klass, tags);
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
