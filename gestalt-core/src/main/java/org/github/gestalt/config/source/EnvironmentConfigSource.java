package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.StringUtils;
import org.github.gestalt.config.utils.SystemWrapper;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Convert the Environment Variables to a property file.
 * Apply the supplied transforms as we convert it.
 * Then write that as an input stream for the next stage in the parsing.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class EnvironmentConfigSource implements ConfigSource {

    /**
     * Format for the EnvironmentConfigSource.
     */
    public static final String ENV_VARS = "envVars";
    private final UUID id = UUID.randomUUID();

    private final boolean failOnErrors;

    private final String prefix;

    private final boolean ignoreCaseOnPrefix;

    private final boolean removePrefix;

    private final Tags tags;

    private final Pattern startsWith = Pattern.compile("^[^a-zA-Z0-9]");

    /**
     * Default constructor for EnvironmentConfigSource.
     * By default, it will not fail on errors  while loading Env Vars since they
     * are often uncontrolled and may not follow expected conventions of this library.
     */
    public EnvironmentConfigSource() {
        this("", false, false, false, Tags.of());
    }

    /**
     * constructor for EnvironmentConfigSource.
     *
     * @param failOnErrors Do not fail on errors while loading Env Vars since they
     *                     are often uncontrolled and may not follow expected conventions of this library.
     */
    public EnvironmentConfigSource(boolean failOnErrors) {
        this("", false, false, failOnErrors, Tags.of());
    }

    /**
     * Specify if you want to only parse the Environment variables that have a prefix.
     * By default, it will remove the prefix from the output and ignores the case on matching the prefix.
     *
     * @param prefix       only use the Environment variables that have a prefix.
     * @param removePrefix if we should remove the prefix
     */
    public EnvironmentConfigSource(String prefix, boolean removePrefix) {
        this(prefix, false, removePrefix, false, Tags.of());
    }

    /**
     * Specify if you want to only parse the Environment variables that have a prefix.
     * By default, it will remove the prefix from the output and ignores the case on matching the prefix.
     *
     * @param prefix             only use the Environment variables that have a prefix.
     * @param ignoreCaseOnPrefix Ignore the case on matching the prefix
     * @param removePrefix       if we should remove the prefix
     */
    public EnvironmentConfigSource(String prefix, boolean ignoreCaseOnPrefix, boolean removePrefix) {
        this(prefix, ignoreCaseOnPrefix, removePrefix, false, Tags.of());
    }

    /**
     * Specify if you want to only parse the Environment variables that have a prefix.
     * By default it will remove the prefix from the output, and it will only match the exact case.
     *
     * @param prefix only use the Environment variables that have a prefix.
     */
    public EnvironmentConfigSource(String prefix) {
        this(prefix, false, true, false, Tags.of());
    }

    /**
     * Specify if you want to only parse the Environment variables that have a prefix.
     *
     * @param prefix             only use the Environment variables that have a prefix.
     * @param ignoreCaseOnPrefix Ignore the case on matching the prefix
     * @param removePrefix       If you should remove the prefix from the output
     * @param failOnErrors       Do not fail on errors while loading Env Vars since they
     *                           are often uncontrolled and may not follow expected conventions of this library.
     */
    public EnvironmentConfigSource(String prefix, boolean ignoreCaseOnPrefix, boolean removePrefix, boolean failOnErrors) {
        this.failOnErrors = failOnErrors;
        this.prefix = prefix;
        this.ignoreCaseOnPrefix = ignoreCaseOnPrefix;
        this.removePrefix = removePrefix;
        this.tags = Tags.of();
    }

    /**
     * Specify if you want to only parse the Environment variables that have a prefix.
     *
     * @param prefix             only use the Environment variables that have a prefix.
     * @param ignoreCaseOnPrefix Ignore the case on matching the prefix
     * @param removePrefix       If you should remove the prefix from the output
     * @param failOnErrors       Do not fail on errors while loading Env Vars since they
     *                           are often uncontrolled and may not follow expected conventions of this library.
     * @param tags               set of tags associated with this source.
     * @deprecated Tags should be added via the builder. Storage of the tags have been moved to {@link ConfigSourcePackage#getTags()}.
     */
    @Deprecated(since = "0.26.0", forRemoval = true)
    public EnvironmentConfigSource(String prefix, boolean ignoreCaseOnPrefix, boolean removePrefix, boolean failOnErrors, Tags tags) {
        this.failOnErrors = failOnErrors;
        this.prefix = prefix;
        this.ignoreCaseOnPrefix = ignoreCaseOnPrefix;
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
            .filter(envVar -> StringUtils.startsWith(envVar.getKey(), prefix, ignoreCaseOnPrefix))
            .map(envVar -> {
                String key = envVar.getKey();
                if (removePrefix) {
                    key = key.substring(prefix.length());

                    //if the next character is a _ or . remove that as well

                    if (startsWith.matcher(key).find()) {
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
