package org.github.gestalt.config.processor.config;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.secret.rules.SecretConcealer;

/**
 * Holds the configuration that is provided to the config node processors.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ConfigNodeProcessorConfig {
    private final GestaltConfig config;
    private final ConfigNodeService configNodeService;
    private final SentenceLexer lexer;
    private final SecretConcealer secretConcealer;

    /**
     * Constructor for the config node processor config.
     *
     * @param config            Gestalt Config
     * @param configNodeService Config node service
     * @param lexer             Lexer to parse paths
     * @param secretConcealer   utility to conceal secrets
     */
    public ConfigNodeProcessorConfig(GestaltConfig config, ConfigNodeService configNodeService, SentenceLexer lexer,
                                     SecretConcealer secretConcealer) {
        this.config = config;
        this.configNodeService = configNodeService;
        this.lexer = lexer;
        this.secretConcealer = secretConcealer;
    }

    /**
     * Get the config node processor config.
     *
     * @return the config node processor config
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
