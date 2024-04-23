package org.github.gestalt.config;

import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.processor.TestValidationProcessor;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.processor.result.ResultProcessor;
import org.github.gestalt.config.processor.TestResultProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

public class GestaltResultProcessorTest {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = GestaltObservationsTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    public void testValidatorGetOkNoProcessResults() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(456, gestalt.getConfig("db.port", Integer.class, Tags.environment("dev")));
    }

    @Test
    public void testValidatorGetOkNullProcessResults() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        var validatorArray = new ArrayList<ResultProcessor>();
        validatorArray.add(null);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build())
            .addResultProcessors(validatorArray)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(456, gestalt.getConfig("db.port", Integer.class, Tags.environment("dev")));
    }

    @Test
    public void testProcessResultsGetOkPrimitive() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build())
            .addResultProcessor(new TestResultProcessor(true))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(456, gestalt.getConfig("db.port", Integer.class, Tags.environment("dev")));
    }

    @Test
    public void testProcessResultsGetOkString() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build())
            .addResultProcessor(new TestResultProcessor(true))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));
    }

    @Test
    public void testProcessResultsGetWouldFailPrimitive() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build())
            .addValidator(new TestValidationProcessor(false))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(456, gestalt.getConfig("db.port", Integer.class, Tags.environment("dev")));
    }

    @Test
    public void testProcessResultsGetWouldFailString() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build())
            .addValidator(new TestValidationProcessor(false))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));
    }

    @Test
    public void testProcessResultsGetOkClass() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build())
            .addValidator(new TestValidationProcessor(true))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test2", gestalt.getConfig("db", DBInfo.class, Tags.environment("dev")).getPassword());
    }

    @Test
    public void testProcessResultsGetFailClass() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build())
            .setValidationEnabled(true)
            .addValidator(new TestValidationProcessor(false))
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class,
            () -> gestalt.getConfig("db", DBInfo.class, Tags.environment("dev")));

        Assertions.assertEquals("Validation failed for config path: db, and " +
            "class: org.github.gestalt.config.test.classes.DBInfo\n" +
            " - level: ERROR, message: something broke", ex.getMessage());
    }

    @Test
    public void testProcessResultsGetFailClassReturnDefault() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.environment("dev")).build())
            .setValidationEnabled(true)
            .addValidator(new TestValidationProcessor(false))
            .build();

        gestalt.loadConfigs();
        Assertions.assertTrue(gestalt.getConfigOptional("db", DBInfo.class, Tags.environment("dev")).isEmpty());
    }
}
