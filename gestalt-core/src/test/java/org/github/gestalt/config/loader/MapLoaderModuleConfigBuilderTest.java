package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.parser.MapConfigParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class MapLoaderModuleConfigBuilderTest {

    @Test
    public void createModuleConfig() {
        var configParser = new MapConfigParser();
        var lexer = new PathLexer();
        var builder = MapConfigLoaderModuleConfigBuilder.builder()
            .setConfigParser(configParser)
            .setLexer(lexer);

        var moduleConfig = builder.build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        Assertions.assertEquals(configParser, moduleConfig.getConfigParse());
        Assertions.assertEquals(lexer, moduleConfig.getLexer());
        Assertions.assertEquals("mapLoader", moduleConfig.name());
    }
}
