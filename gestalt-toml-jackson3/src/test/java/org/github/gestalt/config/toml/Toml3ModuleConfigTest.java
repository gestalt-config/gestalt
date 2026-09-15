package org.github.gestalt.config.toml;

import org.github.gestalt.config.lexer.PathLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.toml.TomlMapper;


class Toml3ModuleConfigTest {

    @Test
    public void createModuleConfig() {
        var objectMapper = TomlMapper.builder().build();
        var lexer = new PathLexer();
        var builder = TomlModuleConfigBuilder.builder()
            .setObjectMapper(objectMapper)
            .setLexer(lexer);

        var moduleConfig = builder.build();

        Assertions.assertEquals(objectMapper, moduleConfig.getObjectMapper());
        Assertions.assertEquals(lexer, moduleConfig.getLexer());
        Assertions.assertEquals("toml", moduleConfig.name());
    }
}
