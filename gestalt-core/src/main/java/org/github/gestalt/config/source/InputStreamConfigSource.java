package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Load a config source from an Input Stream.
 * db.port = 1234
 * db.password = password
 * dp.user = notroot
 *
 * <p>If the format is json the string would be
 * {
 * db {
 * "port" = 1234
 * "password" = "password"
 * "user" = "notroot"
 * }.
 * }
 *
 * <p>A format for the data in the string must also be provided.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class InputStreamConfigSource implements ConfigSource {
    private final InputStream config;
    private final String format;
    private final UUID id = UUID.randomUUID();

    /**
     * Create a Configuration from a provided string. Must alos provide the format.
     *
     * @param config config as a string.
     * @param format format for the string.
     * @throws GestaltException any exception
     */
    public InputStreamConfigSource(InputStream config, String format) throws GestaltException {
        this.config = config;
        if (config == null) {
            throw new GestaltException("The InputStream provided was null");
        }

        this.format = format;
        if (format == null) {
            throw new GestaltException("The InputStream format provided was null");
        }
    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() {
        return config;
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
    public Tags getTags() {
        return Tags.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InputStreamConfigSource)) {
            return false;
        }
        InputStreamConfigSource that = (InputStreamConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
