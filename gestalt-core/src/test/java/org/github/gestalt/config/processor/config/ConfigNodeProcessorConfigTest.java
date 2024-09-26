package org.github.gestalt.config.processor.config;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigNodeProcessorConfigTest {
    private GestaltConfig config;
    private final ConfigNodeService configNodeService  = Mockito.mock();
    private final SentenceLexer lexer = Mockito.mock();
    private final SecretConcealer secretConcealer = Mockito.mock();
    private final ConfigNodeFactoryService configNodeFactoryService = Mockito.mock();

    @BeforeEach
    public void setup() {
        config = new GestaltConfig();

        Mockito.reset(configNodeService, lexer, secretConcealer, configNodeFactoryService);
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
