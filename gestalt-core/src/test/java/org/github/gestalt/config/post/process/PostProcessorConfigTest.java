package org.github.gestalt.config.post.process;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PostProcessorConfigTest {
    private GestaltConfig config;
    private ConfigNodeService configNodeService;
    private SentenceLexer lexer;

    @BeforeEach
    public void setup() {
        config = new GestaltConfig();
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void getConfig() {
        PostProcessorConfig ppConfig = new PostProcessorConfig(config, configNodeService, lexer);

        Assertions.assertEquals(config, ppConfig.getConfig());
    }

    @Test
    void getConfigNodeService() {
        PostProcessorConfig ppConfig = new PostProcessorConfig(config, configNodeService, lexer);

        Assertions.assertEquals(configNodeService, ppConfig.getConfigNodeService());
    }

    @Test
    void getLexer() {
        PostProcessorConfig ppConfig = new PostProcessorConfig(config, configNodeService, lexer);

        Assertions.assertEquals(lexer, ppConfig.getLexer());
    }
}
