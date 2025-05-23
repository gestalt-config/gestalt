package org.github.gestalt.config;

import org.github.gestalt.config.decoder.DecoderContext;
import org.github.gestalt.config.decoder.DecoderService;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.node.TagMergingStrategy;
import org.github.gestalt.config.observations.ObservationService;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.secret.rules.SecretChecker;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Triple;

import java.util.*;

/**
 * A cache layer that stores configurations by path and type.
 * Expects to be registered as a CoreReloadListener and will clear the configs
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@SuppressWarnings("OverloadMethodsDeclarationOrder")
public class GestaltCache implements Gestalt, CoreReloadListener {
    private final Gestalt delegate;
    private final Map<Triple<String, TypeCapture<?>, Tags>, Object> cache = Collections.synchronizedMap(new HashMap<>());
    private final Map<Triple<String, TypeCapture<?>, Tags>, GResultOf<Object>> cacheResultsOf = Collections.synchronizedMap(new HashMap<>());
    private final Tags defaultTags;
    private final ObservationService observationService;
    private final GestaltConfig gestaltConfig;
    private final TagMergingStrategy tagMergingStrategy;
    private final List<SecretChecker> nonCacheableSecrets;

    /**
     * Constructor for the GestaltCache that accepts a delegate.
     *
     * @param delegate            real Gestalt to call for configs to cache.
     * @param defaultTags         Default set of tags to apply to all calls to get a configuration where tags are not provided.
     * @param observationService  Observations service for submitting Observations
     * @param gestaltConfig       Gestalt Configuration
     * @param tagMergingStrategy  The strategy to merge tags
     * @param nonCacheableSecrets secrets that we should not be caching.
     */
    public GestaltCache(Gestalt delegate, Tags defaultTags, ObservationService observationService, GestaltConfig gestaltConfig,
                        TagMergingStrategy tagMergingStrategy, List<SecretChecker> nonCacheableSecrets) {
        Objects.requireNonNull(tagMergingStrategy);
        this.delegate = delegate;
        this.defaultTags = defaultTags;
        this.observationService = observationService;
        this.gestaltConfig = gestaltConfig;
        this.tagMergingStrategy = tagMergingStrategy;
        this.nonCacheableSecrets = nonCacheableSecrets;
    }

    @Override
    public void loadConfigs() throws GestaltException {
        delegate.loadConfigs();
        cache.clear();
    }

    @Override
    public void addConfigSourcePackage(ConfigSourcePackage sourcePackage) throws GestaltException {
        delegate.addConfigSourcePackage(sourcePackage);
    }

    @Override
    public <T> T getConfig(String path, Class<T> klass) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfigInternal(path, typeCapture, null);
    }

    @Override
    public <T> T getConfig(String path, Class<T> klass, Tags tags) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfigInternal(path, typeCapture, tags);
    }

    @Override
    public <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        return getConfigInternal(path, klass, null);
    }

    @Override
    public <T> T getConfig(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigInternal(path, klass, tags);
    }

    @Override
    public <T> GResultOf<T> getConfigResult(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigInternalResult(path, klass, tags);
    }

    @SuppressWarnings("unchecked")
    private <T> T getConfigInternal(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {

        Tags resolvedTags = tagMergingStrategy.mergeTags(tags, defaultTags);
        Triple<String, TypeCapture<?>, Tags> key = new Triple<>(path, klass, resolvedTags);
        if (cache.get(key) != null) {
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.recordObservation("cache.hit", 1, Tags.of());
            }
            return (T) cache.get(key);
        } else {
            GResultOf<T> result = delegate.getConfigResult(path, klass, resolvedTags);
            updateCache(path, key, result);
            return result  != null ? result.results() : null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> GResultOf<T> getConfigInternalResult(String path, TypeCapture<T> klass, Tags tags) throws GestaltException {

        Tags resolvedTags = tagMergingStrategy.mergeTags(tags, defaultTags);
        Triple<String, TypeCapture<?>, Tags> key = new Triple<>(path, klass, resolvedTags);
        if (cacheResultsOf.get(key) != null && cacheResultsOf.get(key).hasResults()) {
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.recordObservation("cache.hit", 1, Tags.of());
            }
            return (GResultOf<T>) cacheResultsOf.get(key);
        } else {
            GResultOf<T> result = delegate.getConfigResult(path, klass, resolvedTags);
            updateCacheResults(path, key, result);
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void updateCache(String path, Triple<String, TypeCapture<?>, Tags> key, GResultOf<T> result) {
        if (shouldCacheValue(path, result != null ? result.getMetadata() : Map.of())) {
            cache.put(key, result != null ? result.results() : null);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void updateCacheResults(String path, Triple<String, TypeCapture<?>, Tags> key, GResultOf<T> result) {
        if (shouldCacheValue(path, result != null ? result.getMetadata() : Map.of())) {
            cacheResultsOf.put(key, (GResultOf<Object>) result);
        }
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, Class<T> klass) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfigInternal(path, defaultVal, typeCapture, null);
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, Class<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfigInternal(path, defaultVal, typeCapture, tags);
    }

    @Override
    public <T> GResultOf<T> getConfigResult(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigInternalResult(path, defaultVal, klass, tags);
    }


    @Override

    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        return getConfigInternal(path, defaultVal, klass, null);
    }

    @Override
    public <T> T getConfig(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigInternal(path, defaultVal, klass, tags);
    }

    @SuppressWarnings("unchecked")
    private <T> T getConfigInternal(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        Tags resolvedTags = tagMergingStrategy.mergeTags(tags, defaultTags);
        Triple<String, TypeCapture<?>, Tags> key = new Triple<>(path, klass, resolvedTags);
        if (cache.containsKey(key)) {
            T result = (T) cache.get(key);
            if (result == null) {
                result = defaultVal;
            }

            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.recordObservation("cache.hit", 1, Tags.of());
            }

            return result;

        } else {
            Optional<GResultOf<T>> result = delegate.getConfigOptionalResult(path, klass, resolvedTags);

            updateCache(path, key, result.orElse(null));

            if (result.isPresent() && result.get().hasResults()) {
                return result.get().results();
            } else {
                return defaultVal;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> GResultOf<T> getConfigInternalResult(String path, T defaultVal, TypeCapture<T> klass, Tags tags) {
        Tags resolvedTags = tagMergingStrategy.mergeTags(tags, defaultTags);
        Triple<String, TypeCapture<?>, Tags> key = new Triple<>(path, klass, resolvedTags);
        if (cacheResultsOf.containsKey(key)) {
            GResultOf<T> result = (GResultOf<T>) cacheResultsOf.get(key);
            if (result == null || !result.hasResults()) {
                result = GResultOf.result(defaultVal, true);
            }

            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.recordObservation("cache.hit", 1, Tags.of());
            }

            return result;

        } else {
            Optional<GResultOf<T>> result = delegate.getConfigOptionalResult(path, klass, resolvedTags);

            updateCacheResults(path, key, result.orElse(null));

            if (result.isPresent() && result.get().hasResults()) {
                return result.get();
            } else {
                return GResultOf.result(defaultVal);
            }
        }
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfigOptionalInternal(path, typeCapture, null);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, Class<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        TypeCapture<T> typeCapture = TypeCapture.of(klass);
        return getConfigOptionalInternal(path, typeCapture, tags);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);

        return getConfigOptionalInternal(path, klass, null);
    }

    @Override
    public <T> Optional<T> getConfigOptional(String path, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigOptionalInternal(path, klass, tags);
    }

    @Override
    public <T> Optional<GResultOf<T>> getConfigOptionalResult(String path, TypeCapture<T> klass, Tags tags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(klass);
        Objects.requireNonNull(tags);

        return getConfigOptionalInternalResult(path, klass, tags);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getConfigOptionalInternal(String path, TypeCapture<T> klass, Tags tags) {

        Tags resolvedTags = tagMergingStrategy.mergeTags(tags, defaultTags);
        Triple<String, TypeCapture<?>, Tags> key = new Triple<>(path, klass, resolvedTags);
        if (cache.containsKey(key)) {
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.recordObservation("cache.hit", 1, Tags.of());
            }

            T result = (T) cache.get(key);
            return Optional.ofNullable(result);
        } else {
            Optional<GResultOf<T>> resultOptional = delegate.getConfigOptionalResult(path, klass, resolvedTags);
            GResultOf<T> result = resultOptional.orElse(null);
            updateCache(path, key, result);
            return Optional.ofNullable(result != null ? result.results() : null);
        }
    }

    public <T> Optional<GResultOf<T>> getConfigOptionalInternalResult(String path, TypeCapture<T> klass, Tags tags) {

        Tags resolvedTags = tagMergingStrategy.mergeTags(tags, defaultTags);
        Triple<String, TypeCapture<?>, Tags> key = new Triple<>(path, klass, resolvedTags);
        if (cacheResultsOf.containsKey(key)) {
            if (gestaltConfig.isObservationsEnabled() && observationService != null) {
                observationService.recordObservation("cache.hit", 1, Tags.of());
            }

            GResultOf<T> result = (GResultOf<T>) cacheResultsOf.get(key);
            return Optional.ofNullable(result);
        } else {
            Optional<GResultOf<T>> resultOptional = delegate.getConfigOptionalResult(path, klass, resolvedTags);
            GResultOf<T> result = resultOptional.orElse(null);
            updateCacheResults(path, key, result);
            return Optional.ofNullable(result);
        }
    }

    private boolean shouldCacheValue(String path, Map<String, List<MetaDataValue<?>>> metadata) {
        boolean notIsSecret = nonCacheableSecrets.stream().noneMatch(it -> it.isSecret(path));
        boolean noCacheMetadata = metadata.containsKey(IsNoCacheMetadata.NO_CACHE) &&
            metadata.get(IsNoCacheMetadata.NO_CACHE).stream()
                .map(it -> it instanceof IsNoCacheMetadata && ((IsNoCacheMetadata) it).getMetadata())
                .filter(it -> it)
                .findFirst().orElse(false);

        return notIsSecret && !noCacheMetadata;
    }

    @Override
    public void registerListener(CoreReloadListener listener) {
        delegate.registerListener(listener);
    }

    @Override
    public void removeListener(CoreReloadListener listener) {
        delegate.removeListener(listener);
    }

    @Override
    public String debugPrint(Tags tags) {
        return delegate.debugPrint(tags);
    }

    @Override
    public String debugPrint() {
        return delegate.debugPrint();
    }

    @Override
    public void reload() {
        cache.clear();
    }

    /**
     * Get the delegate for the cache.
     *
     * @return the delegate for the cache
     */
    public Gestalt getDelegate() {
        return delegate;
    }

    @Override
    public DecoderService getDecoderService() {
        return delegate.getDecoderService();
    }

    @Override
    public DecoderContext getDecoderContext() {
        return delegate.getDecoderContext();
    }
}
