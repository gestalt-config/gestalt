package org.github.gestalt.config.hocon;

import com.typesafe.config.ConfigParseOptions;
import org.github.gestalt.config.lexer.PathLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class HoconModuleConfigTest {

    @Test
    public void createModuleConfig() {
        var configParser = ConfigParseOptions.defaults();
        var lexer = new PathLexer();
        var builder = HoconModuleConfigBuilder.builder()
            .setConfigParseOptions(configParser)
            .setLexer(lexer);

        var moduleConfig = builder.build();

        Assertions.assertEquals(configParser, moduleConfig.getConfigParseOptions());
        Assertions.assertEquals(lexer, moduleConfig.getLexer());
        Assertions.assertEquals("hocon", moduleConfig.name());
    }
}
