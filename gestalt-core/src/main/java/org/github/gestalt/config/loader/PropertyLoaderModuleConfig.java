package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.parser.ConfigParser;

import java.util.Collections;
import java.util.Set;

/**
 * Gestalt module config for the Property Loader Module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class PropertyLoaderModuleConfig implements GestaltModuleConfig {

    private final ConfigParser parser;
    private final SentenceLexer lexer;
    private final Set<String> customFileSuffixes;

    /**
     * Gestalt module config for the Property Loader Module.
     *
     * @param parser options for the ConfigParser
     * @param lexer  the lexer to normalize paths.
     */
    public PropertyLoaderModuleConfig(ConfigParser parser, SentenceLexer lexer) {
        this(parser, lexer, Collections.emptySet());
    }

    /**
     * Gestalt module config for the Property Loader Module.
     *
     * @param parser                options for the ConfigParser
     * @param lexer                 the lexer to normalize paths.
     * @param customFileSuffixes    custom file suffixes to accept (e.g. "conf")
     */
    public PropertyLoaderModuleConfig(ConfigParser parser, SentenceLexer lexer, Set<String> customFileSuffixes) {
        this.parser = parser;
        this.lexer = lexer;
        this.customFileSuffixes = customFileSuffixes;
    }

    @Override
    public String name() {
        return "propertiesLoader";
    }

    /**
     * Get the Config Parse to use with the Property Loader module.
     *
     * @return Config Parse
     */
    public ConfigParser getConfigParse() {
        return parser;
    }

    /**
     * get the SentenceLexer for the Property Loader module, used to build paths into tokens.
     *
     * @return the SentenceLexer
     */
    public SentenceLexer getLexer() {
        return lexer;
    }
    

    /**
     * Get the custom file suffixes for the Property Loader module.
     *
     * @return custom file suffixes
     */
    public Set<String> getCustomFileSuffixes() {
        return customFileSuffixes;
    }
}
