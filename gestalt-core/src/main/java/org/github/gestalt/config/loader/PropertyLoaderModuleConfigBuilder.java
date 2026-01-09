package org.github.gestalt.config.loader;

import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.parser.ConfigParser;

import java.util.HashSet;
import java.util.Set;

/**
 * Gestalt module config for the Property Loader Module builder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class PropertyLoaderModuleConfigBuilder {
    private ConfigParser parser;
    private SentenceLexer lexer;
    private Set<String> customFileSuffixes = new HashSet<>();

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

    /**
     * Add a custom file suffix to accept (e.g. "conf").
     *
     * @param suffix the custom file suffix
     * @return the builder
     */
    public PropertyLoaderModuleConfigBuilder addCustomFileSuffix(String suffix) {
        this.customFileSuffixes.add(suffix);
        return this;
    }

    /**
     * Add multiple custom file suffixes to accept.
     *
     * @param suffixes the custom file suffixes
     * @return the builder
     */
    public PropertyLoaderModuleConfigBuilder addCustomFileSuffixes(Set<String> suffixes) {
        this.customFileSuffixes.addAll(suffixes);
        return this;
    }

    public PropertyLoaderModuleConfig build() {
        return new PropertyLoaderModuleConfig(parser, lexer, customFileSuffixes);
    }
}
