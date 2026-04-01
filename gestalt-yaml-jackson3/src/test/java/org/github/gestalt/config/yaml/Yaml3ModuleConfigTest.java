package org.github.gestalt.config.yaml;

import org.github.gestalt.config.lexer.PathLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.yaml.YAMLMapper;


class Yaml3ModuleConfigTest {

    @Test
    public void createModuleConfig() {
        var objectMapper = YAMLMapper.builder().build();
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
