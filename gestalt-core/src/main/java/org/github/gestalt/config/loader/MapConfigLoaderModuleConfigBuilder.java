package org.github.gestalt.config.loader;

import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.parser.ConfigParser;

/**
 * Gestalt module config for the Map Loader Module builder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class MapConfigLoaderModuleConfigBuilder {
    private ConfigParser parser;
    private SentenceLexer lexer;

    private MapConfigLoaderModuleConfigBuilder() {
    }

    public static MapConfigLoaderModuleConfigBuilder builder() {
        return new MapConfigLoaderModuleConfigBuilder();
    }

    /**
     * Set the Config Parse to use with the Map Loader module.
     * Used to converts the Map to a config tree.
     *
     * @param parser the Config Parse
     * @return Config Parse
     */
    public MapConfigLoaderModuleConfigBuilder setConfigParser(ConfigParser parser) {
        this.parser = parser;
        return this;
    }

    /**
     * set the SentenceLexer for the Map Loader module, used to build paths into tokens.
     *
     * @param lexer SentenceLexer for the Map Loader module.
     * @return the SentenceLexer
     */
    public MapConfigLoaderModuleConfigBuilder setLexer(SentenceLexer lexer) {
        this.lexer = lexer;
        return this;
    }

    public MapConfigLoaderModuleConfig build() {
        return new MapConfigLoaderModuleConfig(parser, lexer);
    }
}
