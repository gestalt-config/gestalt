package org.github.gestalt.config.hocon;

import com.typesafe.config.ConfigParseOptions;
import org.github.gestalt.config.lexer.SentenceLexer;

/**
 * Builder to build a Json ModuleConfig.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class HoconModuleConfigBuilder {
    private ConfigParseOptions configParseOptions;
    private SentenceLexer lexer;

    private HoconModuleConfigBuilder() {
    }

    /**
     * Get the builder for the Hocon Module Config.
     *
     * @return the builder
     */
    public static HoconModuleConfigBuilder builder() {
        return new HoconModuleConfigBuilder();
    }

    /**
     * Set the config Parse Options to use for this Hocon Module.
     *
     * @param configParseOptions the config Parse Options
     * @return the builder
     */
    public HoconModuleConfigBuilder setConfigParseOptions(ConfigParseOptions configParseOptions) {
        this.configParseOptions = configParseOptions;
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
    public HoconModuleConfigBuilder setLexer(SentenceLexer lexer) {
        this.lexer = lexer;
        return this;
    }

    /**
     * build the Hocon ModuleConfig.
     *
     * @return the Hocon ModuleConfig
     */
    public HoconModuleConfig build() {
        return new HoconModuleConfig(configParseOptions, lexer);
    }
}
