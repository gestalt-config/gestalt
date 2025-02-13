package org.github.gestalt.config.source;

import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;

/**
 * ConfigSourceBuilder for the String Config Source.
 *
 * <p>Load a config source from a String. The string must be in the format provided, so if a property file it would be
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
public final class StringConfigSourceBuilder extends SourceBuilder<StringConfigSourceBuilder, StringConfigSource> {
    private String config;
    private String format;

    /**
     * private constructor, use the builder method.
     */
    private StringConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static StringConfigSourceBuilder builder() {
        return new StringConfigSourceBuilder();
    }

    /**
     * Get the string based config source.
     *
     * @return the string based config source
     */
    public String getConfig() {
        return config;
    }

    /**
     * Set the string based config source.
     *
     * @param config the string based config source.
     * @return the builder
     */
    public StringConfigSourceBuilder setConfig(String config) {
        this.config = config;
        return this;
    }

    /**
     * Get the format the String Based Config source represents.
     *
     * @return the format the String Based Config source represents.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Set the format the String Based Config source represents.
     *
     * @param format the format the String Based Config source represents.
     * @return the builder
     */
    public StringConfigSourceBuilder setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public ConfigSourcePackage build() throws GestaltException {
        return buildPackage(new StringConfigSource(config, format));
    }
}
