package org.github.gestalt.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.lexer.PathLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class JsonModuleConfigTest {

    @Test
    public void createModuleConfig() {
        var objectMapper = new ObjectMapper().findAndRegisterModules();
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
