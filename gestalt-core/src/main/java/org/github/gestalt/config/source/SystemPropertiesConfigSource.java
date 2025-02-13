package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.SystemWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Convert the System properties into a config source.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class SystemPropertiesConfigSource implements ConfigSource {

    /**
     * Format for the SystemPropertiesConfigSource.
     */
    public static final String SYSTEM_PROPERTIES = "systemProperties";
    private final UUID id = UUID.randomUUID();

    private final boolean failOnErrors;

    private final Tags tags;

    /**
     * Default constructor for SystemPropertiesConfigSource.
     * It will treat Errors while loading as warnings since System Properties
     * are often uncontrolled and may not follow expected conventions of this library.
     */
    public SystemPropertiesConfigSource() {
        this(false);
    }

    /**
     * constructor for SystemPropertiesConfigSource.
     *
     * @param failOnErrors treat Errors while loading as warnings since System Properties
     *                     are often uncontrolled and may not follow expected conventions of this library.
     */
    public SystemPropertiesConfigSource(boolean failOnErrors) {
        this(failOnErrors, Tags.of());
    }

    /**
     * constructor for SystemPropertiesConfigSource.
     *
     * @param tags tags associated with the source
     * @deprecated Tags should be added via the builder. Storage of the tags have been moved to {@link ConfigSourcePackage#getTags()}.
     */
    @Deprecated(since = "0.26.0", forRemoval = true)
    public SystemPropertiesConfigSource(Tags tags) {
        this(false, tags);
    }

    /**
     * constructor for SystemPropertiesConfigSource.
     *
     * @param failOnErrors treat Errors while loading as warnings since System Properties
     *                     are often uncontrolled and may not follow expected conventions of this library.
     * @param tags         tags associated with the source
     * @deprecated Tags should be added via the builder. Storage of the tags have been moved to {@link ConfigSourcePackage#getTags()}.
     */
    @Deprecated(since = "0.26.0", forRemoval = true)
    public SystemPropertiesConfigSource(boolean failOnErrors, Tags tags) {
        this.failOnErrors = failOnErrors;
        this.tags = tags;
    }

    @Override
    public boolean failOnErrors() {
        return failOnErrors;
    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        Properties properties = SystemWrapper.getProperties();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            properties.store(output, null);
            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException e) {
            throw new GestaltException("Exception while converting system properties to a InputStream", e);
        }
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
        Properties properties = SystemWrapper.getProperties();

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
    public Tags getTags() {
        return tags;
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
