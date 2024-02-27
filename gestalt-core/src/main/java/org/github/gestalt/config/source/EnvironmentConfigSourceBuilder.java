package org.github.gestalt.config.source;

import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;

/**
 * ConfigSourceBuilder for the Class Path Config Source.
 *
 * <p>Convert the Environment Variables to a property file.
 * Apply the supplied transforms as we convert it.
 * Then write that as an input stream for the next stage in the parsing.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class EnvironmentConfigSourceBuilder extends SourceBuilder<EnvironmentConfigSourceBuilder, EnvironmentConfigSource> {

    private boolean failOnErrors = false;

    private String prefix = "";

    private boolean ignoreCaseOnPrefix = false;

    private boolean removePrefix = false;

    /**
     * private constructor, use the builder method.
     */
    private EnvironmentConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static EnvironmentConfigSourceBuilder builder() {
        return new EnvironmentConfigSourceBuilder();
    }

    /**
     * Get if we should fail on errors for the config source.
     *
     * @return if we should fail on errors for the config source
     */
    public boolean isFailOnErrors() {
        return failOnErrors;
    }

    /**
     * Set if we should fail on errors for the config source.
     *
     * @param failOnErrors Set if we should fail on errors for the config source.
     * @return the builder
     */
    public EnvironmentConfigSourceBuilder setFailOnErrors(boolean failOnErrors) {
        this.failOnErrors = failOnErrors;
        return this;
    }

    /**
     * Get the prefix we scan for. This will only include the environment variables that match the prefix.
     *
     * @return the prefix we scan for.
     */
    public String getPrefix() {
        return prefix;
    }


    /**
     * Set the prefix we scan for. This will only include the environment variables that match the prefix.
     *
     * @param prefix the prefix we scan for.
     * @return the builder
     */
    public EnvironmentConfigSourceBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Gets if we should ignore the case when matching the prefix.
     *
     * @return if we should ignore the case when matching the prefix.
     */
    public boolean isIgnoreCaseOnPrefix() {
        return ignoreCaseOnPrefix;
    }

    /**
     * Sets if we should ignore the case when matching the prefix.
     *
     * @param ignoreCaseOnPrefix if we should ignore the case when matching the prefix.
     * @return the builder
     */
    public EnvironmentConfigSourceBuilder setIgnoreCaseOnPrefix(boolean ignoreCaseOnPrefix) {
        this.ignoreCaseOnPrefix = ignoreCaseOnPrefix;
        return this;
    }

    /**
     * Get if we should remove the prefix in the environment variables.
     *
     * @return If we should remove the prefix in the environment variables.
     */
    public boolean isRemovePrefix() {
        return removePrefix;
    }

    /**
     * Set if we should remove the prefix in the environment variables.
     *
     * @param removePrefix if we should remove the prefix in the environment variables.
     * @return the builder
     */
    public EnvironmentConfigSourceBuilder setRemovePrefix(boolean removePrefix) {
        this.removePrefix = removePrefix;
        return this;
    }

    @Override
    public ConfigSourcePackage build() throws GestaltException {
        return buildPackage(new EnvironmentConfigSource(prefix, ignoreCaseOnPrefix, removePrefix, failOnErrors, tags));
    }
}
