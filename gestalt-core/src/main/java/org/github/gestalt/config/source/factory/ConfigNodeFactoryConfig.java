package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.node.ConfigNodeService;

public class ConfigNodeFactoryConfig {
    private final ConfigLoaderService configLoaderService;
    private final ConfigNodeService configNodeService;
    private final SentenceLexer lexer;

    public ConfigNodeFactoryConfig(ConfigLoaderService configLoaderService, ConfigNodeService configNodeService, SentenceLexer lexer) {
        this.configLoaderService = configLoaderService;
        this.configNodeService = configNodeService;
        this.lexer = lexer;
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
}
