package org.github.gestalt.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.lexer.SentenceLexer;

/**
 * Gestalt module config for the Json Module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class JsonModuleConfig implements GestaltModuleConfig {

    private final ObjectMapper objectMapper;
    private final SentenceLexer lexer;

    /**
     * Gestalt module config for the Json Module.
     *
     * @param objectMapper for loading yaml config, it should have a YAMLFactory registered to it.
     * @param lexer        the lexer to normalize paths.
     */
    public JsonModuleConfig(ObjectMapper objectMapper, SentenceLexer lexer) {
        this.objectMapper = objectMapper;
        this.lexer = lexer;
    }

    @Override
    public String name() {
        return "json";
    }

    /**
     * Get the object mapper to use with the Json module.
     *
     * @return object mapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * get the SentenceLexer for the Json module, used to normalize sentences.
     *
     * @return the SentenceLexer
     */
    public SentenceLexer getLexer() {
        return lexer;
    }
}
