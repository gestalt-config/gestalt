package org.github.gestalt.config.loader;

import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.parser.ConfigParser;

/**
 * Gestalt module config for the Property Loader Module builder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class PropertyLoaderModuleConfigBuilder {
    private ConfigParser parser;
    private SentenceLexer lexer;

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
     * @return Config Parse
     */
    public PropertyLoaderModuleConfigBuilder setConfigParser(ConfigParser parser) {
        this.parser = parser;
        return this;
    }

    /**
     * set the SentenceLexer for the Property Loader module, used to build paths into tokens.
     *
     * @param lexer SentenceLexer for the Property Loader module.
     * @return the SentenceLexer
     */
    public PropertyLoaderModuleConfigBuilder setLexer(SentenceLexer lexer) {
        this.lexer = lexer;
        return this;
    }

    public PropertyLoaderModuleConfig build() {
        return new PropertyLoaderModuleConfig(parser, lexer);
    }
}
