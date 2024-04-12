package org.github.gestalt.config.source;

import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;

import java.io.InputStream;

/**
 * ConfigSourceBuilder for the Input Stream Config Source.
 *
 *
 * <p>A format for the data in the string must also be provided.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class InputStreamConfigSourceBuilder extends SourceBuilder<InputStreamConfigSourceBuilder, StringConfigSource> {
    private InputStream config;
    private String format;

    /**
     * private constructor, use the builder method.
     */
    private InputStreamConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static InputStreamConfigSourceBuilder builder() {
        return new InputStreamConfigSourceBuilder();
    }

    /**
     * Get the InputStream based config source.
     *
     * @return the InputStream based config source
     */
    public InputStream getConfig() {
        return config;
    }

    /**
     * Set the InputStream based config source.
     *
     * @param config the InputStream based config source.
     * @return the builder
     */
    public InputStreamConfigSourceBuilder setConfig(InputStream config) {
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
    public InputStreamConfigSourceBuilder setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public ConfigSourcePackage build() throws GestaltException {
        return buildPackage(new InputStreamConfigSource(config, format));
    }
}
