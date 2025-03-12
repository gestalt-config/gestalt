package org.github.gestalt.config;

import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.EnvironmentConfigSourceBuilder;
import org.github.gestalt.config.source.StringConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.SystemWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;

public class GestaltBugsTest {

    @Test
    public void testCrossRootStringSubstitution() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("DB_URI", "test");
        configs.put("DB_PORT", "1234");
        configs.put("DB_PASSWORD", "abc123");

        try (MockedStatic<SystemWrapper> mocked = mockStatic(SystemWrapper.class)) {
            mocked.when(SystemWrapper::getEnvVars).thenReturn(configs);

            var tags = Tags.of("env", "dev");

            GestaltBuilder builder = new GestaltBuilder();
            Gestalt gestalt = builder
                .addSource(EnvironmentConfigSourceBuilder.builder().build())
                .addSource(StringConfigSourceBuilder.builder().setFormat("properties")
                    .setConfig(
                        "db.uri: dev\n" +
                            "db.port: ${db.port}\n" +
                            "db.password: 60000")
                    .setTags(tags)
                    .build())
                .build();

            gestalt.loadConfigs();

            Assertions.assertEquals("test", gestalt.getConfig("db.uri", String.class));
            Assertions.assertEquals("1234", gestalt.getConfig("db.port", String.class, tags));
            Assertions.assertEquals("abc123", gestalt.getConfig("db.password", String.class));
        }
    }
}
