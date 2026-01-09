package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.parser.MapConfigParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class PropertyLoaderModuleConfigBuilderTest {

    @Test
    public void createModuleConfig() {
        var configParser = new MapConfigParser();
        var lexer = new PathLexer();
        var builder = PropertyLoaderModuleConfigBuilder.builder()
            .setConfigParser(configParser)
            .setLexer(lexer)
            .setAcceptsFormats(List.of("myFormat"))
            .addAcceptedFormat("format2");

        var moduleConfig = builder.build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        Assertions.assertEquals(configParser, moduleConfig.getConfigParse());
        Assertions.assertEquals(lexer, moduleConfig.getLexer());
        Assertions.assertEquals("propertiesLoader", moduleConfig.name());
        Assertions.assertEquals(2, moduleConfig.getAcceptsFormats().size());
        Assertions.assertEquals("myFormat", moduleConfig.getAcceptsFormats().get(0));
        Assertions.assertEquals("format2", moduleConfig.getAcceptsFormats().get(1));
    }
}
