package org.github.gestalt.config.json;

import org.github.gestalt.config.lexer.PathLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;


class Json3ModuleConfigTest {

    @Test
    public void createModuleConfig() {
        var objectMapper = JsonMapper.builder().build();
        var lexer = new PathLexer();
        var builder = JsonModuleConfigBuilder.builder()
            .setObjectMapper(objectMapper)
            .setLexer(lexer);

        var moduleConfig = builder.build();

        Assertions.assertEquals(objectMapper, moduleConfig.getObjectMapper());
        Assertions.assertEquals(lexer, moduleConfig.getLexer());
        Assertions.assertEquals("json", moduleConfig.name());
    }
}
