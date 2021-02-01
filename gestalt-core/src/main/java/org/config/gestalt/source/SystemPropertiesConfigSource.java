package org.config.gestalt.source;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Convert the System properties into a config source.
 *
 * @author Colin Redmond
 */
public class SystemPropertiesConfigSource implements ConfigSource {

    public static final String SYSTEM_PROPERTIES = "systemProperties";
    private final UUID id = UUID.randomUUID();

    @Override
    public boolean hasStream() {
        return false;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        throw new GestaltException("Unsupported operation load stream on an SystemPropertiesConfigSource");
    }

    @Override
    public boolean hasList() {
        return true;
    }

    /**
     * Convert the System properties into a config list.
     *
     * @return list of pairs of configs.
     */
    @Override
    public List<Pair<String, String>> loadList() {
        Properties properties = System.getProperties();

        return properties.entrySet()
            .stream()
            .map(prop -> new Pair<>((String) prop.getKey(), (String) prop.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public String format() {
        return SYSTEM_PROPERTIES;
    }

    @Override
    public String name() {
        return SYSTEM_PROPERTIES;
    }

    @Override
    public UUID id() {  //NOPMD
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemPropertiesConfigSource)) {
            return false;
        }
        SystemPropertiesConfigSource that = (SystemPropertiesConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
