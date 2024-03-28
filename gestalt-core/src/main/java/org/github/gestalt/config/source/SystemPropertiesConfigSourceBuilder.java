package org.github.gestalt.config.source;

import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;

/**
 * ConfigSourceBuilder for the System Properties Config Source.
 *
 * <p>Convert the System properties into a config source.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class SystemPropertiesConfigSourceBuilder
    extends SourceBuilder<SystemPropertiesConfigSourceBuilder, SystemPropertiesConfigSource> {

    private boolean failOnErrors = false;

    /**
     * private constructor, use the builder method.
     */
    private SystemPropertiesConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static SystemPropertiesConfigSourceBuilder builder() {
        return new SystemPropertiesConfigSourceBuilder();
    }

    /**
     * Get if we should fail on errors for the config source.
     *
     * @return if we should fail on errors for the config source
     */
    public boolean failOnErrors() {
        return failOnErrors;
    }

    /**
     * Set if we should fail on errors for the config source.
     *
     * @param failOnErrors Set if we should fail on errors for the config source.
     * @return the builder
     */
    public SystemPropertiesConfigSourceBuilder setFailOnErrors(boolean failOnErrors) {
        this.failOnErrors = failOnErrors;
        return this;
    }

    @Override
    public ConfigSourcePackage build() throws GestaltException {
        return buildPackage(new SystemPropertiesConfigSource(failOnErrors));
    }
}
