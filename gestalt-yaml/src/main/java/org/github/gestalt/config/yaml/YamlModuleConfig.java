package org.github.gestalt.config.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.lexer.SentenceLexer;

/**
 * Gestalt module config for the Yaml Module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class YamlModuleConfig implements GestaltModuleConfig {

    private final ObjectMapper objectMapper;
    private final SentenceLexer lexer;

    /**
     * Gestalt module config for the Yaml Module.
     *
     * @param objectMapper for loading yaml config, it should have a YAMLFactory registered to it.
     * @param lexer        the lexer to normalize paths.
     */
    public YamlModuleConfig(ObjectMapper objectMapper, SentenceLexer lexer) {
        this.objectMapper = objectMapper;
        this.lexer = lexer;
    }

    @Override
    public String name() {
        return "yaml";
    }

    /**
     * Get the object mapper to use with the Yaml module.
     *
     * @return object mapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * get the SentenceLexer for the Yaml module, used to normalize sentences.
     *
     * @return the SentenceLexer
     */
    public SentenceLexer getLexer() {
        return lexer;
    }
}
