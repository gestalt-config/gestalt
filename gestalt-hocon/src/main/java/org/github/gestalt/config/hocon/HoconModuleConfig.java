package org.github.gestalt.config.hocon;

import com.typesafe.config.ConfigParseOptions;
import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.lexer.SentenceLexer;

/**
 * Gestalt module config for the Hocon Module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class HoconModuleConfig implements GestaltModuleConfig {

    private final ConfigParseOptions configParseOptions;
    private final SentenceLexer lexer;

    /**
     * Gestalt module config for the Hocon Module.
     *
     * @param configParseOptions options for the Hocon parsing
     * @param lexer        the lexer to normalize paths.
     */
    public HoconModuleConfig(ConfigParseOptions configParseOptions, SentenceLexer lexer) {
        this.configParseOptions = configParseOptions;
        this.lexer = lexer;
    }

    @Override
    public String name() {
        return "hocon";
    }

    /**
     * Get the Config Parse Options to use with the Hocon module.
     *
     * @return Config Parse Options
     */
    public ConfigParseOptions getConfigParseOptions() {
        return configParseOptions;
    }

    /**
     * get the SentenceLexer for the Hocon module, used to normalize sentences.
     *
     * @return the SentenceLexer
     */
    public SentenceLexer getLexer() {
        return lexer;
    }
}
