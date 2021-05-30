package org.github.gestalt.config.post.process;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;

/**
 * Holds the configuration that is provided to the post processors.
 */
public class PostProcessorConfig {
    private final GestaltConfig config;
    private final ConfigNodeService configNodeService;
    private final SentenceLexer lexer;

    /**
     * Constructor for the post processor config.
     *
     * @param config Gestalt Config
     * @param configNodeService Config node service
     * @param lexer Lexer to parse paths
     */
    public PostProcessorConfig(GestaltConfig config, ConfigNodeService configNodeService, SentenceLexer lexer) {
        this.config = config;
        this.configNodeService = configNodeService;
        this.lexer = lexer;
    }

    /**
     * Get the post processor config.
     *
     * @return the post processor config
     */
    public GestaltConfig getConfig() {
        return config;
    }

    /**
     * Get the ConfigNodeService.
     *
     * @return ConfigNodeService
     */
    public ConfigNodeService getConfigNodeService() {
        return configNodeService;
    }

    /**
     * Get the Lexer.
     *
     * @return SentenceLexer
     */
    public SentenceLexer getLexer() {
        return lexer;
    }
}
