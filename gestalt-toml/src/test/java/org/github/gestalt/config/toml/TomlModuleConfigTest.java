package org.github.gestalt.config.toml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import org.github.gestalt.config.lexer.PathLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class TomlModuleConfigTest {

    @Test
    public void createModuleConfig() {
        var objectMapper = new ObjectMapper(new TomlFactory()).findAndRegisterModules();
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
