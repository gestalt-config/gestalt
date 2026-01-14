package org.github.gestalt.dotenv.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.github.gestalt.config.entity.GestaltModuleConfig;

import java.util.Objects;

/**
 * Dotenv Module Config.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2026.
 */
public class DotenvModuleConfig implements GestaltModuleConfig {
    private final Dotenv dotenv;

    /**
     * Config to setup the Dotenv instance for the DotEnvPropertiesTransformer.
     *
     * @param dotenv the dotenv instance
     */
    public DotenvModuleConfig(Dotenv dotenv) {
        Objects.requireNonNull(dotenv, "Dotenv instance should not be null");
        this.dotenv = dotenv;
    }

    @Override
    public String name() {
        return "dotEnv";
    }

    /**
     * Get the Dotenv instance.
     *
     * @return the Dotenv instance.
     */
    public Dotenv getDotenv() {
        return dotenv;
    }
}
