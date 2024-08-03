package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.loader.ConfigLoaderService;

public class ConfigNodeFactoryConfig {
    private final ConfigLoaderService configLoaderService;

    public ConfigNodeFactoryConfig(ConfigLoaderService configLoaderService) {
        this.configLoaderService = configLoaderService;
    }

    public ConfigLoaderService getConfigLoaderService() {
        return configLoaderService;
    }
}
