package org.github.gestalt.config.post.process;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;

public class PostProcessorConfig {
    private final GestaltConfig config;
    private final ConfigNodeService configNodeService;
    private final SentenceLexer lexer;

    public PostProcessorConfig(GestaltConfig config, ConfigNodeService configNodeService, SentenceLexer lexer) {
        this.config = config;
        this.configNodeService = configNodeService;
        this.lexer = lexer;
    }

    public GestaltConfig getConfig() {
        return config;
    }

    public ConfigNodeService getConfigNodeService() {
        return configNodeService;
    }

    public SentenceLexer getLexer() {
        return lexer;
    }
}
