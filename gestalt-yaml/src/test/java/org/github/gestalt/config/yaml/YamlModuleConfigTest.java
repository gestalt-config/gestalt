package org.github.gestalt.config.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.github.gestalt.config.lexer.PathLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class YamlModuleConfigTest {

    @Test
    public void createModuleConfig() {
        var objectMapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
        var lexer = new PathLexer();
        var builder = YamlModuleConfigBuilder.builder()
            .setObjectMapper(objectMapper)
            .setLexer(lexer);

        var moduleConfig = builder.build();

        Assertions.assertEquals(objectMapper, moduleConfig.getObjectMapper());
        Assertions.assertEquals(lexer, moduleConfig.getLexer());
        Assertions.assertEquals("yaml", moduleConfig.name());
    }
}
