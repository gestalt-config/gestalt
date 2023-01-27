package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.SystemWrapper;

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
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class EnvironmentConfigSource implements ConfigSource {

    /**
     * Format for the EnvironmentConfigSource.
     */
    public static final String ENV_VARS = "envVars";
    private final UUID id = UUID.randomUUID();

    private final boolean failOnErrors;

    private final String prefix;

    private final boolean removePrefix;

    private final Tags tags;

    /**
     * Default constructor for EnvironmentConfigSource.
     * By default, it will not fail on errors  while loading Env Vars since they
     * are often uncontrolled and may not follow expected conventions of this library.
     */
    public EnvironmentConfigSource() {
        this("", false, false, Tags.of());
    }

    /**
     * constructor for EnvironmentConfigSource.
     *
     * @param failOnErrors Do not fail on errors while loading Env Vars since they
     *     are often uncontrolled and may not follow expected conventions of this library.
     */
    public EnvironmentConfigSource(boolean failOnErrors) {
        this("", false, failOnErrors, Tags.of());
    }

    /**
     * Specify if you want to only parse the Environment variables that have a prefix.
     * By default it will remove the prefix from the output.
     *
     * @param prefix only use the Environment variables that have a prefix.
     * @param removePrefix if we should remove the prefix
     */
    public EnvironmentConfigSource(String prefix, boolean removePrefix) {
        this(prefix, removePrefix, false, Tags.of());
    }

    /**
     * Specify if you want to only parse the Environment variables that have a prefix.
     * By default it will remove the prefix from the output.
     *
     * @param prefix only use the Environment variables that have a prefix.
     */
    public EnvironmentConfigSource(String prefix) {
        this(prefix, true, false, Tags.of());
    }

    /**
     * Specify if you want to only parse the Environment variables that have a prefix.
     *
     * @param prefix only use the Environment variables that have a prefix.
     * @param removePrefix If you should remove the prefix from the output
     * @param failOnErrors Do not fail on errors while loading Env Vars since they
     *     are often uncontrolled and may not follow expected conventions of this library.
     * @param tags set of tags associated with this source.
     */
    public EnvironmentConfigSource(String prefix, boolean removePrefix, boolean failOnErrors, Tags tags) {
        this.failOnErrors = failOnErrors;
        this.prefix = prefix;
        this.removePrefix = removePrefix;
        this.tags = tags;
    }

    @Override
    public boolean failOnErrors() {
        return failOnErrors;
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
        return SystemWrapper.getEnvVars().entrySet()
                            .stream()
                            .filter(envVar -> envVar.getKey().startsWith(prefix))
                            .map(envVar -> {
                                String key = envVar.getKey();
                                if (removePrefix) {
                                    key = key.substring(prefix.length());

                                    //if the next character is a _ or . remove that as well
                                    if (key.startsWith("_") || key.startsWith(".")) {
                                        key = key.substring(1);
                                    }
                                }

                                return new Pair<>(key, envVar.getValue());

                            })
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
    public Tags getTags() {
        return tags;
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
