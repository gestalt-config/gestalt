package org.github.gestalt.config.loader;

import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.parser.ConfigParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestalt module config for the Property Loader Module builder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class PropertyLoaderModuleConfigBuilder {
    private ConfigParser parser;
    private SentenceLexer lexer;
    private List<String> acceptsFormats;

    private PropertyLoaderModuleConfigBuilder() {
    }

    public static PropertyLoaderModuleConfigBuilder builder() {
        return new PropertyLoaderModuleConfigBuilder();
    }

    /**
     * Set the Config Parse to use with the Property Loader module.
     * Used to converts the Property Loader to a config tree.
     *
     * @param parser the Config Parse
     * @return the builder
     */
    public PropertyLoaderModuleConfigBuilder setConfigParser(ConfigParser parser) {
        this.parser = parser;
        return this;
    }

    /**
     * set the SentenceLexer for the Property Loader module, used to build paths into tokens.
     *
     * @param lexer SentenceLexer for the Property Loader module.
     * @return the builder
     */
    public PropertyLoaderModuleConfigBuilder setLexer(SentenceLexer lexer) {
        this.lexer = lexer;
        return this;
    }

    /**
     * Sets the list of formats that the Property Loader module should use.
     *
     * @param acceptsFormats list of formats that the Property Loader module should use.
     * @return the builder
     */
    public PropertyLoaderModuleConfigBuilder setAcceptsFormats(List<String> acceptsFormats) {
        this.acceptsFormats = new ArrayList<>(acceptsFormats);
        return this;
    }

    /**
     * Adds a format that the Property Loader module should use.
     *
     * @param accept format that the Property Loader module should use.
     * @return the builder
     */
    public PropertyLoaderModuleConfigBuilder addAcceptedFormat(String accept) {
        if (acceptsFormats == null) {
            acceptsFormats = new ArrayList<>();
        }
        acceptsFormats.add(accept);
        return this;
    }

    /**
     * Build the PropertyLoaderModuleConfig
     * @return the PropertyLoaderModuleConfig
     */
    public PropertyLoaderModuleConfig build() {
        return new PropertyLoaderModuleConfig(parser, lexer, acceptsFormats);
    }
}
