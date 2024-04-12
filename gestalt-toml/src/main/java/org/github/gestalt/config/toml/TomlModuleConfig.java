package org.github.gestalt.config.toml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.lexer.SentenceLexer;

/**
 * Gestalt module config for the Toml Module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class TomlModuleConfig implements GestaltModuleConfig {

    private final ObjectMapper objectMapper;
    private final SentenceLexer lexer;

    /**
     * Gestalt module config for the Toml Module.
     *
     * @param objectMapper for loading yaml config, it should have a YAMLFactory registered to it.
     * @param lexer        the lexer to normalize paths.
     */
    public TomlModuleConfig(ObjectMapper objectMapper, SentenceLexer lexer) {
        this.objectMapper = objectMapper;
        this.lexer = lexer;
    }

    @Override
    public String name() {
        return "toml";
    }

    /**
     * Get the object mapper to use with the Toml module.
     *
     * @return object mapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * get the SentenceLexer for the Toml module, used to normalize sentences.
     *
     * @return the SentenceLexer
     */
    public SentenceLexer getLexer() {
        return lexer;
    }
}
