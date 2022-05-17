package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.utils.Pair;

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
 * @author Colin Redmond
 */
public class SystemPropertiesConfigSource implements ConfigSource {

    /**
     * Format for the SystemPropertiesConfigSource.
     */
    public static final String SYSTEM_PROPERTIES = "systemProperties";
    private final UUID id = UUID.randomUUID();

    private final boolean failOnErrors;

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
     *     are often uncontrolled and may not follow expected conventions of this library.
     */
    public SystemPropertiesConfigSource(boolean failOnErrors) {
        this.failOnErrors = failOnErrors;
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
        Properties properties = System.getProperties();

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
