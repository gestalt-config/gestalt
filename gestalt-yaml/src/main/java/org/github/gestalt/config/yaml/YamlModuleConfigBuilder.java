package org.github.gestalt.config.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.lexer.SentenceLexer;

/**
 * Builder to build a YamlModuleConfig.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class YamlModuleConfigBuilder {
    private ObjectMapper objectMapper;
    private SentenceLexer lexer;

    private YamlModuleConfigBuilder() {
    }

    /**
     * Get the builder for the Yaml Module Config.
     *
     * @return the builder
     */
    public static YamlModuleConfigBuilder builder() {
        return new YamlModuleConfigBuilder();
    }

    /**
     * Set the object mapper to use for this Yaml Module.
     *
     * @param objectMapper the object mapper
     * @return the builder
     */
    public YamlModuleConfigBuilder setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Set the Lexer used to normalize the paths in the Yaml Module.
     * If not set it defaults to the one set in gestalt.
     * It is not recommended to set this lexer unless you want custom behaviour for this module. Preferably use the Gestalt Config one.
     *
     * @param lexer lexer used to normalize the paths.
     * @return the builder
     */
    public YamlModuleConfigBuilder setLexer(SentenceLexer lexer) {
        this.lexer = lexer;
        return this;
    }

    /**
     * build the Yaml ModuleConfig.
     *
     * @return the Yaml ModuleConfig
     */
    public YamlModuleConfig build() {
        return new YamlModuleConfig(objectMapper, lexer);
    }
}
