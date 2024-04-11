package org.github.gestalt.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.lexer.SentenceLexer;

/**
 * Builder to build a Json ModuleConfig.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class JsonModuleConfigBuilder {
    private ObjectMapper objectMapper;
    private SentenceLexer lexer;

    private JsonModuleConfigBuilder() {
    }

    /**
     * Get the builder for the Json Module Config.
     *
     * @return the builder
     */
    public static JsonModuleConfigBuilder builder() {
        return new JsonModuleConfigBuilder();
    }

    /**
     * Set the object mapper to use for this Json Module.
     *
     * @param objectMapper the object mapper
     * @return the builder
     */
    public JsonModuleConfigBuilder setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Set the Lexer used to normalize the paths in the Json Module.
     * If not set it defaults to the one set in gestalt.
     * It is not recommended to set this lexer unless you want custom behaviour for this module. Preferably use the Gestalt Config one.
     *
     * @param lexer lexer used to normalize the paths.
     * @return the builder
     */
    public JsonModuleConfigBuilder setLexer(SentenceLexer lexer) {
        this.lexer = lexer;
        return this;
    }

    /**
     * build the Json ModuleConfig.
     *
     * @return the Json ModuleConfig
     */
    public JsonModuleConfig build() {
        return new JsonModuleConfig(objectMapper, lexer);
    }
}
