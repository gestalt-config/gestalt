package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Allows a user to provide a custom config source as a map.
 * would take the same form as a property file with a key that can be tokenized and a value.
 * db.port = 1234
 * db.password = password
 * dp.user = notroot
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class MapConfigSource implements ConfigSource {

    /**
     * Format for the MapConfigSource.
     */
    public static final String MAP_CONFIG = "mapConfig";
    private final Map<String, String> customConfig;
    private final UUID id = UUID.randomUUID();

    private final Tags tags;

    /**
     * takes a map of configs.
     *
     * @param customConfig map of configs.
     */
    public MapConfigSource(Map<String, String> customConfig) {
        this(customConfig, Tags.of());
    }

    /**
     * takes a map of configs.
     *
     * @param customConfig map of configs.
     * @param tags         tags associated with the source
     * @deprecated Tags should be added via the builder. Storage of the tags have been moved to {@link ConfigSourcePackage#getTags()}.
     */
    @Deprecated(since = "0.26.0", forRemoval = true)
    public MapConfigSource(Map<String, String> customConfig, Tags tags) {
        Objects.requireNonNull(customConfig, "Custom map must not be null");
        this.customConfig = customConfig;
        this.tags = tags;
    }


    @Override
    public boolean hasStream() {
        return false;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        throw new GestaltException("Unsupported operation load stream on an CustomConfigSource");
    }

    @Override
    public boolean hasList() {
        return true;
    }

    /**
     * Convert the map to a list of pairs of configs.
     *
     * @return list of pairs of configs.
     */
    @Override
    public List<Pair<String, String>> loadList() {
        return customConfig.entrySet()
            .stream()
            .map(envVar -> new Pair<>(envVar.getKey(), envVar.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public String format() {
        return MAP_CONFIG;
    }

    @Override
    public String name() {
        return MAP_CONFIG;
    }

    @Override
    public UUID id() {  //NOPMD
        return id;
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapConfigSource)) {
            return false;
        }
        MapConfigSource that = (MapConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
