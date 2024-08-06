package org.github.gestalt.config.processor.config;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConfigNodeProcessorConfigTest {
    private GestaltConfig config;
    private ConfigNodeService configNodeService;
    private SentenceLexer lexer;
    private SecretConcealer secretConcealer;
    private ConfigNodeFactoryService configNodeFactoryService;

    @BeforeEach
    public void setup() {
        config = new GestaltConfig();
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        secretConcealer = Mockito.mock();
        configNodeFactoryService = Mockito.mock();
    }

    @Test
    void getConfig() {
        ConfigNodeProcessorConfig ppConfig =
            new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
                configNodeFactoryService);

        Assertions.assertEquals(config, ppConfig.getConfig());
    }

    @Test
    void getConfigNodeService() {
        ConfigNodeProcessorConfig ppConfig =
            new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
                configNodeFactoryService);

        Assertions.assertEquals(configNodeService, ppConfig.getConfigNodeService());
    }

    @Test
    void getLexer() {
        ConfigNodeProcessorConfig ppConfig =
            new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
                configNodeFactoryService);

        Assertions.assertEquals(lexer, ppConfig.getLexer());
    }

    @Test
    void getConfigSourceFactoryService() {
        ConfigNodeProcessorConfig ppConfig =
            new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
                configNodeFactoryService);

        Assertions.assertEquals(configNodeFactoryService, ppConfig.getConfigSourceFactoryService());
    }

    @Test
    void getConfigLoaderService() {
        ConfigNodeProcessorConfig ppConfig =
            new ConfigNodeProcessorConfig(config, configNodeService, lexer, secretConcealer,
                configNodeFactoryService);

        Assertions.assertEquals(configNodeFactoryService, ppConfig.getConfigSourceFactoryService());
    }
}
