package org.config.gestalt.source;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Allows a user to provide a custom config source as a map.
 */
public class MapConfigSource implements ConfigSource {

    public static final String MAP_CONFIG = "mapConfig";
    private final Map<String, String> customConfig;

    public MapConfigSource(Map<String, String> customConfig) {
        this.customConfig = customConfig;
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
}
