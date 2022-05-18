package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.utils.Pair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Load a config source from a String. A format for the data in the string must also be provided.
 *
 * @author Colin Redmond
 */
public class StringConfigSource implements ConfigSource {
    private final String config;
    private final String format;
    private final UUID id = UUID.randomUUID();

    /**
     * Create a Configuration from a provided string. Must alos provide the format.
     *
     * @param config config as a string.
     * @param format format for the string.
     * @throws GestaltException any exception
     */
    public StringConfigSource(String config, String format) throws GestaltException {
        this.config = config;
        if (config == null) {
            throw new GestaltException("The string provided was null");
        }

        this.format = format;
        if (format == null) {
            throw new GestaltException("The string format provided was null");
        }
    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() {
        return new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {
        throw new GestaltException("Unsupported operation loadList on an StringConfigSource");
    }

    @Override
    public String format() {
        return format;
    }


    @Override
    public String name() {
        return "String format: " + format;
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
        if (!(o instanceof StringConfigSource)) {
            return false;
        }
        StringConfigSource that = (StringConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
