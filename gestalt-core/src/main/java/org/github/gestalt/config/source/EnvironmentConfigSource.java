package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Convert the Environment Variables to a property file.
 * Apply the supplied transforms as we convert it.
 * Then write that as an input stream for the next stage in the parsing.
 *
 * @author Colin Redmond
 */
public class EnvironmentConfigSource implements ConfigSource {

    /**
     * Format for the EnvironmentConfigSource.
     */
    public static final String ENV_VARS = "envVars";
    private final UUID id = UUID.randomUUID();

    /**
     * Default constructor for EnvironmentConfigSource.
     */
    public EnvironmentConfigSource() {
    }

    @Override
    public boolean hasStream() {
        return false;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        throw new GestaltException("Unsupported operation load stream on an EnvironmentConfigSource");
    }

    @Override
    public boolean hasList() {
        return true;
    }

    /**
     * gets the Environment Variables and converts them to a list of pairs. The first is the key and the second the value.
     *
     * @return list of environment variables.
     */
    @Override
    public List<Pair<String, String>> loadList() {
        return System.getenv().entrySet()
            .stream()
            .map(envVar -> new Pair<>(envVar.getKey(), envVar.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public String format() {
        return ENV_VARS;
    }

    @Override
    public String name() {
        return ENV_VARS;
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
        if (!(o instanceof EnvironmentConfigSource)) {
            return false;
        }
        EnvironmentConfigSource that = (EnvironmentConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
