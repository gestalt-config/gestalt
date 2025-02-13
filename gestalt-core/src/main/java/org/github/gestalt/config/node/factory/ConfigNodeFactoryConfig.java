package org.github.gestalt.config.node.factory;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.node.ConfigNodeService;

/**
 * Holds configuration applied to all ConfigNodeFactory.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class ConfigNodeFactoryConfig {
    private final ConfigLoaderService configLoaderService;
    private final ConfigNodeService configNodeService;
    private final SentenceLexer lexer;
    private final GestaltConfig config;

    public ConfigNodeFactoryConfig(ConfigLoaderService configLoaderService, ConfigNodeService configNodeService, SentenceLexer lexer,
                                   GestaltConfig config) {
        this.configLoaderService = configLoaderService;
        this.configNodeService = configNodeService;
        this.lexer = lexer;
        this.config = config;
    }

    public ConfigLoaderService getConfigLoaderService() {
        return configLoaderService;
    }

    public ConfigNodeService getConfigNodeService() {
        return configNodeService;
    }

    public SentenceLexer getLexer() {
        return lexer;
    }

    public GestaltConfig getConfig() {
        return config;
    }
}
