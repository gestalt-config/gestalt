package org.github.gestalt.dotenv.source;

import io.github.cdimascio.dotenv.Dotenv;
import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.EnvironmentConfigSource;
import org.github.gestalt.config.source.MapConfigSource;

import java.util.Objects;

/**
 * ConfigSourceBuilder for the Dotenv Config Source.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2026.
 */
public final class DotenvSourceBuilder extends SourceBuilder<DotenvSourceBuilder, MapConfigSource> {
    private Dotenv dotenv;
    private Dotenv.Filter filter;
    private String format = EnvironmentConfigSource.ENV_VARS;

    /**
     * private constructor, use the builder method.
     */
    private DotenvSourceBuilder() {
    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static DotenvSourceBuilder builder() {
        return new DotenvSourceBuilder();
    }

    /**
     * Set the Dotenv that backs the config source.
     *
     * @param dotenv the Dotenv that backs the config source.
     * @return the builder
     */
    public DotenvSourceBuilder setDotenv(Dotenv dotenv) {
        Objects.requireNonNull(dotenv, "dotenv");
        this.dotenv = dotenv;
        return this;
    }

    /**
     * Set the filter to filter which entries to load.
     *
     * @param filter the filter
     * @return the builder
     */
    public DotenvSourceBuilder setFilter(Dotenv.Filter filter) {
        Objects.requireNonNull(filter, "filter");
        this.filter = filter;
        return this;
    }

    /**
     * Set the format of the config source, defaults to load like environment variables.
     *
     * @param format the format
     * @return the builder
     */
    public DotenvSourceBuilder setFormat(String format) {
        Objects.requireNonNull(format, "format");
        this.format = format;
        return this;
    }

    @Override
    public ConfigSourcePackage build() throws GestaltException {
        return buildPackage(new DotenvConfigSource(dotenv, filter, format));
    }
}
