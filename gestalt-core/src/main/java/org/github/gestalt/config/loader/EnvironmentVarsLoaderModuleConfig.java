package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.parser.ConfigParser;

/**
 * Gestalt module config for the Environment Variable Module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class EnvironmentVarsLoaderModuleConfig implements GestaltModuleConfig {

    private final ConfigParser parser;
    private final SentenceLexer lexer;

    /**
     * Gestalt module config for the Environment Variable Module.
     *
     * @param parser options for the ConfigParser
     * @param lexer  the lexer to normalize paths.
     */
    public EnvironmentVarsLoaderModuleConfig(ConfigParser parser, SentenceLexer lexer) {
        this.parser = parser;
        this.lexer = lexer;
    }

    @Override
    public String name() {
        return "environmentVarsLoader";
    }

    /**
     * Get the Config Parse to use with the Environment Variable module.
     *
     * @return Config Parse
     */
    public ConfigParser getConfigParse() {
        return parser;
    }

    /**
     * get the SentenceLexer for the Environment Variable module, used to build paths into tokens.
     *
     * @return the SentenceLexer
     */
    public SentenceLexer getLexer() {
        return lexer;
    }
}
