package org.github.gestalt.config.post.process;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PostProcessorConfigTest {
    private GestaltConfig config;
    private ConfigNodeService configNodeService;
    private SentenceLexer lexer;
    private SecretConcealer secretConcealer;

    @BeforeEach
    public void setup() {
        config = new GestaltConfig();
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
        secretConcealer = Mockito.mock();
    }

    @Test
    void getConfig() {
        PostProcessorConfig ppConfig = new PostProcessorConfig(config, configNodeService, lexer, secretConcealer);

        Assertions.assertEquals(config, ppConfig.getConfig());
    }

    @Test
    void getConfigNodeService() {
        PostProcessorConfig ppConfig = new PostProcessorConfig(config, configNodeService, lexer, secretConcealer);

        Assertions.assertEquals(configNodeService, ppConfig.getConfigNodeService());
    }

    @Test
    void getLexer() {
        PostProcessorConfig ppConfig = new PostProcessorConfig(config, configNodeService, lexer, secretConcealer);

        Assertions.assertEquals(lexer, ppConfig.getLexer());
    }
}
