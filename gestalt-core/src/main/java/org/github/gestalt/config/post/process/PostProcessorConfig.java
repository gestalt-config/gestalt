package org.github.gestalt.config.post.process;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.secret.rules.SecretConcealer;

/**
 * Holds the configuration that is provided to the post processors.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class PostProcessorConfig {
    private final GestaltConfig config;
    private final ConfigNodeService configNodeService;
    private final SentenceLexer lexer;
    private final SecretConcealer secretConcealer;

    /**
     * Constructor for the post processor config.
     *
     * @param config            Gestalt Config
     * @param configNodeService Config node service
     * @param lexer             Lexer to parse paths
     * @param secretConcealer   utility to conceal secrets
     */
    public PostProcessorConfig(GestaltConfig config, ConfigNodeService configNodeService, SentenceLexer lexer,
                               SecretConcealer secretConcealer) {
        this.config = config;
        this.configNodeService = configNodeService;
        this.lexer = lexer;
        this.secretConcealer = secretConcealer;
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

    /**
     * Get the secret concealer.
     *
     * @return the secret concealer
     */
    public SecretConcealer getSecretConcealer() {
        return secretConcealer;
    }
}
