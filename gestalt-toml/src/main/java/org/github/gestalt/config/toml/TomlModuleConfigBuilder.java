package org.github.gestalt.config.toml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.lexer.SentenceLexer;

/**
 * Builder to build a Toml ModuleConfig.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class TomlModuleConfigBuilder {
    private ObjectMapper objectMapper;
    private SentenceLexer lexer;

    private TomlModuleConfigBuilder() {
    }

    /**
     * Get the builder for the Toml Module Config.
     *
     * @return the builder
     */
    public static TomlModuleConfigBuilder builder() {
        return new TomlModuleConfigBuilder();
    }

    /**
     * Set the object mapper to use for this Toml Module.
     *
     * @param objectMapper the object mapper
     * @return the builder
     */
    public TomlModuleConfigBuilder setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Set the Lexer used to normalize the paths in the Toml Module.
     * If not set it defaults to the one set in gestalt.
     * It is not recommended to set this lexer unless you want custom behaviour for this module. Preferably use the Gestalt Config one.
     *
     * @param lexer lexer used to normalize the paths.
     * @return the builder
     */
    public TomlModuleConfigBuilder setLexer(SentenceLexer lexer) {
        this.lexer = lexer;
        return this;
    }

    /**
     * build the Toml ModuleConfig.
     *
     * @return the Toml ModuleConfig
     */
    public TomlModuleConfig build() {
        return new TomlModuleConfig(objectMapper, lexer);
    }
}
