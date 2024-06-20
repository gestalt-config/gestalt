package org.github.gestalt.config;

import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.decoder.*;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.PathLexerBuilder;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderRegistry;
import org.github.gestalt.config.loader.EnvironmentVarsLoaderModuleConfigBuilder;
import org.github.gestalt.config.loader.MapConfigLoader;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.processor.result.DefaultResultProcessor;
import org.github.gestalt.config.processor.result.ErrorResultProcessor;
import org.github.gestalt.config.processor.result.ResultsProcessorManager;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.CoreReloadListenersContainer;
import org.github.gestalt.config.reload.ManualConfigReloadStrategy;
import org.github.gestalt.config.secret.rules.MD5SecretObfuscator;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.secret.rules.SecretConcealerManager;
import org.github.gestalt.config.source.*;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.test.classes.DBInfoPathAnnotation;
import org.github.gestalt.config.test.classes.DBInfoPathMultiAnnotation;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.SystemWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.github.gestalt.config.lexer.PathLexer.DEFAULT_EVALUATOR;
import static org.mockito.Mockito.mockStatic;

class GestaltTest {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = GestaltObservationsTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    public void test() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertNotNull(((GestaltCore) gestalt).getDecoderContext());
        Assertions.assertNotNull(((GestaltCore) gestalt).getDecoderService());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class));

        //get the db.port as a Optional
        Assertions.assertEquals(Double.valueOf(3306), gestalt.getConfig("db.port", OptionalDouble.class).getAsDouble());
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", OptionalInt.class).getAsInt());
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", OptionalLong.class).getAsLong());

        //get a non-existent value as an optional
        Assertions.assertFalse(gestalt.getConfig("db.port.none", OptionalDouble.class).isPresent());
        Assertions.assertFalse(gestalt.getConfig("db.port.none", OptionalInt.class).isPresent());
        Assertions.assertFalse(gestalt.getConfig("db.port.none", OptionalLong.class).isPresent());

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class, Tags.of()));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class, Tags.of()));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class)));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class)));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class)));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<Optional<String>>() {
        }).get());
        Assertions.assertFalse(gestalt.getConfig("does.not.exist", new TypeCapture<Optional<String>>() {
        }).isPresent());
    }

    @Test
    public void testTagsNotInSource() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class)));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class)));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class)));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class, Tags.of("toys", "ball")));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class, Tags.of("toys", "ball")));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class, Tags.of("toys", "ball")));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class, Tags.of("toys", "ball")));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class, Tags.of("toys", "ball")));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class, Tags.of("toys", "ball")));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class), Tags.of("toys", "ball")));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }, Tags.of("toys", "ball")));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class), Tags.of("toys", "ball")));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class),
            Tags.of("toys", "ball")));

        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }, Tags.of("toys", "ball")));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class), Tags.of("toys", "ball")));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class), Tags.of("toys", "ball")));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class), Tags.of("toys", "ball")));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", new TypeCapture<Optional<String>>() {
        }, Tags.of("toys", "ball")).get());
        Assertions.assertFalse(gestalt.getConfig("admin[99]", new TypeCapture<Optional<String>>() {
        }, Tags.of("toys", "ball")).isPresent());
    }

    @Test
    public void testTagsInSource() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).setTags(Tags.of("toys", "ball")).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class, Tags.of("toys", "ball")));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class, Tags.of("toys", "ball")));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class, Tags.of("toys", "ball")));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class, Tags.of("toys", "ball")));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class, Tags.of("toys", "ball")));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class, Tags.of("toys", "ball")));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class), Tags.of("toys", "ball")));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }, Tags.of("toys", "ball")));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class), Tags.of("toys", "ball")));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class),
            Tags.of("toys", "ball")));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }, Tags.of("toys", "ball")));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class), Tags.of("toys", "ball")));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class), Tags.of("toys", "ball")));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class), Tags.of("toys", "ball")));

        Assertions.assertThrows(GestaltException.class, () ->
            gestalt.getConfig("db.name", String.class));
        Assertions.assertThrows(GestaltException.class, () ->
            gestalt.getConfig("db.port", String.class));
        Assertions.assertThrows(GestaltException.class, () ->
            gestalt.getConfig("db.port", Integer.class));
        Assertions.assertThrows(GestaltException.class, () ->
            gestalt.getConfig("db.port", Long.class));
    }

    @Test
    public void testGettingEmptyPath() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("password", "test");
        configs.put("uri", "somedatabase");
        configs.put("port", "3306");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        DBInfo dbInfo = gestalt.getConfig("", DBInfo.class);

        Assertions.assertEquals("test", dbInfo.getPassword());
        Assertions.assertEquals("somedatabase", dbInfo.getUri());
        Assertions.assertEquals(3306, dbInfo.getPort());

        // test different accessors
        Optional<DBInfo> dbInfoOptional = gestalt.getConfigOptional("", DBInfo.class);

        Assertions.assertEquals("test", dbInfoOptional.get().getPassword());
        Assertions.assertEquals("somedatabase", dbInfoOptional.get().getUri());
        Assertions.assertEquals(3306, dbInfoOptional.get().getPort());

        DBInfo defaultVal = new DBInfo();
        defaultVal.setPassword("test");
        defaultVal.setUri("somedatabase");
        defaultVal.setPort(3306);

        DBInfo dbInfoDefault = gestalt.getConfig("", defaultVal, DBInfo.class);

        Assertions.assertEquals("test", dbInfoDefault.getPassword());
        Assertions.assertEquals("somedatabase", dbInfoDefault.getUri());
        Assertions.assertEquals(3306, dbInfoDefault.getPort());
    }

    @Test
    public void testPrefixAnnotation() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.uri", "somedatabase");
        configs.put("db.port", "3306");

        configs.put("user.db.password", "test2");
        configs.put("user.db.uri", "anotherDB");
        configs.put("user.db.port", "1234");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        DBInfoPathAnnotation dbInfo = gestalt.getConfig("", DBInfoPathAnnotation.class);

        Assertions.assertEquals("test", dbInfo.getPassword());
        Assertions.assertEquals("somedatabase", dbInfo.getUri());
        Assertions.assertEquals(3306, dbInfo.getPort());

        // test different accessors
        Optional<DBInfoPathAnnotation> dbInfoOptional = gestalt.getConfigOptional("", DBInfoPathAnnotation.class);

        Assertions.assertEquals("test", dbInfoOptional.get().getPassword());
        Assertions.assertEquals("somedatabase", dbInfoOptional.get().getUri());
        Assertions.assertEquals(3306, dbInfoOptional.get().getPort());

        DBInfoPathAnnotation dbInfoDefault = gestalt.getConfig("", new DBInfoPathAnnotation(), DBInfoPathAnnotation.class);

        Assertions.assertEquals("test", dbInfoDefault.getPassword());
        Assertions.assertEquals("somedatabase", dbInfoDefault.getUri());
        Assertions.assertEquals(3306, dbInfoDefault.getPort());

        // test mixing a provided path with an annotated path
        DBInfoPathAnnotation usersDBInfo = gestalt.getConfig("user", DBInfoPathAnnotation.class);

        Assertions.assertEquals("test2", usersDBInfo.getPassword());
        Assertions.assertEquals("anotherDB", usersDBInfo.getUri());
        Assertions.assertEquals(1234, usersDBInfo.getPort());


        // test with a longer annotated path
        DBInfoPathMultiAnnotation multiDBInfo = gestalt.getConfig("", DBInfoPathMultiAnnotation.class);

        Assertions.assertEquals("test2", multiDBInfo.getPassword());
        Assertions.assertEquals("anotherDB", multiDBInfo.getUri());
        Assertions.assertEquals(1234, multiDBInfo.getPort());
    }

    @Test
    public void testMerge() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin.user[0]", "John");
        configs.put("admin.user[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "New Name");
        configs2.put("db.password", "123abc");
        configs2.put("redis.url", "redis.io");
        configs2.put("admin.user[1]", "Matt");
        configs2.put("admin.user[2]", "Paul");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("New Name", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("123abc", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.url", String.class));

        Assertions.assertEquals("John", gestalt.getConfig("admin.user[0]", String.class));
        Assertions.assertEquals("Matt", gestalt.getConfig("admin.user[1]", String.class));
        Assertions.assertEquals("Paul", gestalt.getConfig("admin.user[2]", String.class));

        Assertions.assertEquals("New Name", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("123abc", gestalt.getConfig("db.password", TypeCapture.of(String.class)));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class)));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.url", TypeCapture.of(String.class)));
    }

    @Test
    public void testMerge3Sources() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin.user[0]", "John");
        configs.put("admin.user[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "New Name");
        configs2.put("db.password", "123abc");
        configs2.put("redis.url", "redis.io");
        configs2.put("admin.user[1]", "Matt");
        configs2.put("admin.user[2]", "Paul");

        Map<String, String> configs3 = new HashMap<>();
        configs3.put("db.name", "New Name");
        configs3.put("db.timeout", "5000");
        configs3.put("admin.user[0]", "Scott");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs3).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("New Name", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("123abc", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.url", String.class));

        Assertions.assertEquals("Scott", gestalt.getConfig("admin.user[0]", String.class));
        Assertions.assertEquals("Matt", gestalt.getConfig("admin.user[1]", String.class));
        Assertions.assertEquals("Paul", gestalt.getConfig("admin.user[2]", String.class));
        Assertions.assertEquals("5000", gestalt.getConfig("db.timeout", String.class));

        Assertions.assertEquals("5000", gestalt.getConfig("db.timeout", TypeCapture.of(String.class)));
    }

    @Test
    public void testMerge3SourcesTags() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "players");
        configs2.put("db.password", "123abc");
        configs2.put("db.url", "mysql.io");

        Map<String, String> configs3 = new HashMap<>();
        configs3.put("db.name", "users");
        configs3.put("db.timeout", "5000");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.of("toy", "ball")).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs3).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("users", gestalt.getConfigOptional("db.name", String.class).get());
        Assertions.assertTrue(gestalt.getConfigOptional("db.password", String.class).isEmpty());
        Assertions.assertEquals("3306", gestalt.getConfigOptional("db.port", String.class).get());
        Assertions.assertTrue(gestalt.getConfigOptional("db.url", String.class).isEmpty());
        Assertions.assertEquals("5000", gestalt.getConfigOptional("db.timeout", String.class).get());
        Assertions.assertEquals("5000", gestalt.getConfigOptional("db.timeout", TypeCapture.of(String.class)).get());

        Assertions.assertEquals("players", gestalt.getConfig("db.name", String.class, Tags.of("toy", "ball")));
        Assertions.assertEquals("123abc", gestalt.getConfig("db.password", String.class, Tags.of("toy", "ball")));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class, Tags.of("toy", "ball")));
        Assertions.assertEquals("mysql.io", gestalt.getConfig("db.url", String.class, Tags.of("toy", "ball")));
        Assertions.assertEquals("5000", gestalt.getConfig("db.timeout", String.class, Tags.of("toy", "ball")));
        Assertions.assertEquals("5000", gestalt.getConfig("db.timeout", TypeCapture.of(String.class), Tags.of("toy", "ball")));

        Assertions.assertEquals("users", gestalt.getConfigOptional("db.name", String.class, Tags.of("toy", "car")).get());
        Assertions.assertTrue(gestalt.getConfigOptional("db.password", String.class, Tags.of("toy", "car")).isEmpty());
    }

    @Test
    public void testPostProcessor() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .addConfigNodeProcessor(new TestConfigNodeProcessor("aaa"))
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test aaa", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306 aaa", gestalt.getConfig("db.port", String.class));


        Assertions.assertEquals("John aaa", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve aaa", gestalt.getConfig("admin[1]", String.class));
    }

    @Test
    public void testPostProcessorNoResults() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = Mockito.mock(ConfigNodeManager.class);

        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null,
            secretConcealer, null, null,
            Tags.of(), new TagMergingStrategyFallback());

        Mockito.when(configNodeManager.processConfigNodes()).thenReturn(GResultOf.resultOf(null, Collections.emptyList()));

        GestaltException e = Assertions.assertThrows(GestaltException.class, gestalt::postProcessConfigs);

        Assertions.assertEquals("no results found post processing the config nodes", e.getMessage());
    }

    @Test
    public void testPostProcessorError() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = Mockito.mock(ConfigNodeManager.class);

        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null,
            secretConcealer, null, null,
            Tags.of(), new TagMergingStrategyFallback());

        Mockito.when(configNodeManager.processConfigNodes()).thenReturn(
            GResultOf.resultOf(true, Collections.singletonList(new ValidationError.ArrayInvalidIndex(-1, "test"))));

        GestaltException e = Assertions.assertThrows(GestaltException.class, gestalt::postProcessConfigs);
        Assertions.assertEquals("Failed post processing config nodes with errors \n" +
            " - level: ERROR, message: Invalid array index: -1 for path: test", e.getMessage());
    }

    @Test
    public void testPostProcessorMinorWarnings() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = Mockito.mock(ConfigNodeManager.class);

        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        GestaltConfig config = new GestaltConfig();
        config.setTreatMissingValuesAsErrors(false);

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, configNodeManager, null,
            secretConcealer, null, null,
            Tags.of(), new TagMergingStrategyFallback());

        Mockito.when(configNodeManager.processConfigNodes()).thenReturn(
            GResultOf.resultOf(true, Collections.singletonList(new ValidationError.ArrayMissingIndex(1, "test"))));

        gestalt.postProcessConfigs();
    }

    @Test
    public void testPostProcessorSwap() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("path1.conf1.prop1", "value1");
        configs.put("path1.conf1.prop2", "value2");
        configs.put("path1.conf1.prop3", "value3");
        configs.put("path2.conf2.prop1", "value4");
        configs.put("path2.conf2.prop2", "value5");
        configs.put("path2.conf2.prop3", "value6");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .addConfigNodeProcessor(new TestConfigNodeProcessorSwapNodes("path1", "path2"))
            .addConfigNodeProcessor(new TestConfigNodeProcessorSwapNodes("prop1", "prop2"))
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("value4", gestalt.getConfig("path1.conf2.prop2", String.class));
        Assertions.assertEquals("value5", gestalt.getConfig("path1.conf2.prop1", String.class));
        Assertions.assertEquals("value6", gestalt.getConfig("path1.conf2.prop3", String.class));

        Assertions.assertEquals("value1", gestalt.getConfig("path2.conf1.prop2", String.class));
        Assertions.assertEquals("value2", gestalt.getConfig("path2.conf1.prop1", String.class));
        Assertions.assertEquals("value3", gestalt.getConfig("path2.conf1.prop3", String.class));
    }

    @Test
    public void testGetDefault() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", "aaa", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", "aaa", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", 1, Integer.class));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", 2L, Long.class));

        Assertions.assertEquals(123, gestalt.getConfig("redis.port", 123, Integer.class));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", "redis.io", String.class));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", "redis.io", String.class));
        Assertions.assertEquals("Scott", gestalt.getConfig("admin[3]", "Scott", String.class));

        Assertions.assertEquals(123, gestalt.getConfig("redis.port", 123, Integer.class, Tags.of()));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", "redis.io", String.class, Tags.of()));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", "redis.io", String.class, Tags.of()));
        Assertions.assertEquals("Scott", gestalt.getConfig("admin[3]", "Scott", String.class, Tags.of()));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", "aaa", new TypeCapture<>() {
        }));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", "aaa", TypeCapture.of(String.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", 1, new TypeCapture<>() {
        }));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", 1, TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", 2L, TypeCapture.of(Long.class)));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", 2L, new TypeCapture<>() {
        }));

        Assertions.assertEquals(123, gestalt.getConfig("redis.port", 123, TypeCapture.of(Integer.class)));
        Assertions.assertEquals(123, gestalt.getConfig("redis.port", 123, new TypeCapture<>() {
        }));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", "redis.io", TypeCapture.of(String.class)));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", "redis.io", TypeCapture.of(String.class)));
        Assertions.assertEquals("Scott", gestalt.getConfig("admin[3]", "Scott", TypeCapture.of(String.class)));
    }

    @Test
    public void testGetDefaultBadPathInvalidToken() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("Scott", gestalt.getConfig("admin[3a]", "Scott", String.class));
    }

    @Test
    public void testGetOptional() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(Optional.of("test"), gestalt.getConfigOptional("db.name", String.class));
        Assertions.assertEquals(Optional.of("3306"), gestalt.getConfigOptional("db.port", String.class));
        Assertions.assertEquals(Optional.of(3306), gestalt.getConfigOptional("db.port", Integer.class));
        Assertions.assertEquals(Optional.of(3306L), gestalt.getConfigOptional("db.port", Long.class));

        Assertions.assertEquals(Optional.of("test"), gestalt.getConfigOptional("db.name", String.class, Tags.of()));
        Assertions.assertEquals(Optional.of("3306"), gestalt.getConfigOptional("db.port", String.class, Tags.of()));
        Assertions.assertEquals(Optional.of(3306), gestalt.getConfigOptional("db.port", Integer.class, Tags.of()));
        Assertions.assertEquals(Optional.of(3306L), gestalt.getConfigOptional("db.port", Long.class, Tags.of()));

        Assertions.assertEquals(Optional.empty(), gestalt.getConfigOptional("redis.port", Integer.class));
        Assertions.assertEquals(Optional.empty(), gestalt.getConfigOptional("redis.uri", String.class));
        Assertions.assertEquals(Optional.empty(), gestalt.getConfigOptional("redis.uri", String.class));
        Assertions.assertEquals(Optional.empty(), gestalt.getConfigOptional("admin[3]", String.class));

        Assertions.assertEquals(Optional.empty(), gestalt.getConfigOptional("redis.port", TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Optional.empty(), gestalt.getConfigOptional("redis.uri", TypeCapture.of(String.class)));
        Assertions.assertEquals(Optional.empty(), gestalt.getConfigOptional("redis.uri", TypeCapture.of(String.class)));
        Assertions.assertEquals(Optional.empty(), gestalt.getConfigOptional("admin[3]", TypeCapture.of(String.class)));
    }

    @Test
    public void testGetOptionalBadPathInvalidToken() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(Optional.empty(), gestalt.getConfigOptional("admin[3a]", String.class));
    }

    @Test
    public void testMergeArraysMissingIndex() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin.user[0]", "John");
        configs.put("admin.user[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "New Name");
        configs2.put("db.password", "123abc");
        configs2.put("redis.url", "redis.io");
        configs2.put("admin.user[1]", "Matt");
        configs2.put("admin.user[3]", "Paul");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(false);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(false);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build())
            .setGestaltConfig(config)
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("Missing array index: 2 for path: admin.user", errors.get(0).description());

        GestaltConfig configUpdate = ((GestaltCore) gestalt).getGestaltConfig();
        configUpdate.setTreatWarningsAsErrors(true);
        configUpdate.setTreatMissingArrayIndexAsError(true);
        configUpdate.setTreatMissingValuesAsErrors(true);

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("admin.user[2]", Integer.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: admin.user[2], for class: java.lang.Integer\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: admin.user[2], for class: ArrayToken, " +
                "during navigating to next node");

        ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("admin.user", new TypeCapture<List<String>>() {

        }));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: admin.user, for class: java.util.List<java.lang.String>\n" +
                " - level: MISSING_VALUE, message: Missing array index: 2");


        try {
            configUpdate.setTreatWarningsAsErrors(false);
            configUpdate.setTreatMissingArrayIndexAsError(false);
            configUpdate.setTreatMissingValuesAsErrors(false);
            List<String> test = gestalt.getConfig("admin.user", new TypeCapture<>() {
            });

            Assertions.assertEquals("John", test.get(0));
            Assertions.assertEquals("Matt", test.get(1));
            Assertions.assertNull(test.get(2));
            Assertions.assertEquals("Paul", test.get(3));
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }

        Assertions.assertEquals("John", gestalt.getConfig("admin.user[0]", String.class));
        Assertions.assertEquals("Matt", gestalt.getConfig("admin.user[1]", String.class));
        Assertions.assertEquals("Paul", gestalt.getConfig("admin.user[3]", String.class));
    }

    @Test
    public void testNoSources() throws GestaltException {
        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            List.of(),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, secretConcealer,
            null, null, Tags.of(), new TagMergingStrategyFallback());

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.loadConfigs());
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("No sources provided, unable to load any configs");
    }

    @Test
    public void testNullSources() throws GestaltException {
        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            null,
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, secretConcealer,
            null, null, Tags.of(), new TagMergingStrategyFallback());

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.loadConfigs());
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("No sources provided, unable to load any configs");
    }

    @Test
    public void testNoEncoder() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(Collections.singletonList(new StringDecoder()), configNodeManager, lexer,
                List.of(new StandardPathMapper())), lexer, new GestaltConfig(), new ConfigNodeManager(), null,
            secretConcealer, null,
            new ResultsProcessorManager(List.of(new ErrorResultProcessor(), new DefaultResultProcessor())),
            Tags.of(), new TagMergingStrategyFallback());

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db.port", Integer.class));
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db.port, for class: java.lang.Integer\n" +
                " - level: ERROR, message: No decoders found for class: java.lang.Integer and node type: leaf");

    }

    @Test
    public void testMultipleEncoder() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new StringDecoder(), new ExceptionDecoder()), configNodeManager, lexer,
                List.of(new StandardPathMapper())), lexer, new GestaltConfig(), new ConfigNodeManager(), null,
            secretConcealer, null,
            new ResultsProcessorManager(List.of(new ErrorResultProcessor(), new DefaultResultProcessor())),
            Tags.of(), new TagMergingStrategyFallback());

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
    }

    @Test
    public void testLoadConfigError() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = Mockito.mock(ConfigLoaderRegistry.class);

        ConfigLoader configLoader = Mockito.mock(ConfigLoader.class);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        Mockito.when(configLoaderRegistry.getLoader(Mockito.anyString())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(Mockito.any())).thenReturn(
            GResultOf.errors(new ValidationError.ArrayDuplicateIndex(1, "admin")));

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null, secretConcealer,
            null, null, Tags.of(), new TagMergingStrategyFallback());

        var ex = Assertions.assertThrows(GestaltException.class, gestalt::loadConfigs);
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed to load configs from source: mapConfig\n" +
                " - level: ERROR, message: Duplicate array index: 1 for path: admin");

    }

    @Test
    public void testNoResultsForNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .setTreatWarningsAsErrors(true)
            .setTreatMissingArrayIndexAsError(false)
            .setTreatMissingValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("no.exist.name", String.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: no.exist.name, for class: java.lang.String\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: no.exist.name, " +
                "for class: ObjectToken, during navigating to next node");

    }

    @Test
    public void testNoResultsLoadingNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoader mockConfigLoader = Mockito.mock(ConfigLoader.class);
        Mockito.when(mockConfigLoader.accepts(MapConfigSource.MAP_CONFIG)).thenReturn(true);
        Mockito.when(mockConfigLoader.loadSource(Mockito.any())).thenReturn(GResultOf.resultOf(null, Collections.emptyList()));

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(mockConfigLoader);

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(true);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(false);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, secretConcealer,
            null, null, Tags.of(), new TagMergingStrategyFallback());

        GestaltConfigurationException e = Assertions.assertThrows(GestaltConfigurationException.class, gestalt::loadConfigs);
        Assertions.assertEquals("No results found for node", e.getMessage());
    }

    @Test
    public void testGetNonExistentPath() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .setTreatWarningsAsErrors(false)
            .setTreatMissingArrayIndexAsError(false)
            .setTreatMissingValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db.password", String.class));

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: db.password, for class: java.lang.String\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: db.password, for class: ObjectToken, " +
                "during navigating to next node");

    }

    @Test
    public void testGetArrayNonExistent() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .setTreatWarningsAsErrors(false)
            .setTreatMissingArrayIndexAsError(false)
            .setTreatMissingValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();


        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("admin[3]", String.class));
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Failed getting config path: admin[3], for class: java.lang.String\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: admin[3], for class: ArrayToken, " +
                "during navigating to next node");
    }

    @Test
    public void testGetBadTokenPath() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");


        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("admin[a3]", String.class));
        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessage("Unable to parse path: admin[a3]\n" +
                " - level: ERROR, message: Unable to tokenize element admin[a3] for path: admin[a3]");


        Optional<String> result = gestalt.getConfigOptional("admin[a3]", String.class);
        Assertions.assertFalse(result.isPresent());

        String resultStr = gestalt.getConfig("admin[a3]", "test", String.class);
        Assertions.assertEquals("test", resultStr);

    }

    @Test
    public void testReload() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        CoreListener coreListener = new CoreListener();
        ManualConfigReloadStrategy reload = new ManualConfigReloadStrategy();
        GestaltCore gestalt = (GestaltCore) new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).addConfigReloadStrategy(reload).build())
            .useCacheDecorator(false)
            .addCoreReloadListener(coreListener)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class)));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class)));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class)));

        configs.put("db.name", "test1");
        reload.reload();

        Assertions.assertEquals(1, coreListener.count);
        Assertions.assertEquals("test1", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
    }

    @Test
    @SuppressWarnings("VariableDeclarationUsageDistance")
    public void testReloadTags() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test");
        configs2.put("db.port", "3306");
        configs2.put("admin[0]", "John");
        configs2.put("admin[1]", "Steve");

        CoreListener coreListener = new CoreListener();
        ManualConfigReloadStrategy reload = new ManualConfigReloadStrategy();
        GestaltCore gestalt = (GestaltCore) new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.of("toy", "ball"))
                .addConfigReloadStrategy(reload).build())
            .useCacheDecorator(false)
            .addCoreReloadListener(coreListener)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class, Tags.of("toy", "ball")));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class, Tags.of("toy", "ball")));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class, Tags.of("toy", "ball")));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class, Tags.of("toy", "ball")));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class, Tags.of("toy", "ball")));

        configs2.put("db.name", "test1");
        reload.reload();

        Assertions.assertEquals(1, coreListener.count);
        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("test1", gestalt.getConfig("db.name", TypeCapture.of(String.class), Tags.of("toy", "ball")));
    }

    @Test
    public void testReloadNullReloadSource() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())), lexer, new GestaltConfig(), configNodeManager,
            coreReloadListenersContainer, secretConcealer, null,
            new ResultsProcessorManager(List.of(new ErrorResultProcessor(), new DefaultResultProcessor())),
            Tags.of(), new TagMergingStrategyFallback());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class)));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class)));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class)));

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.reload(null));
        assertThat(ex).hasMessage("No sources provided, unable to reload any configs");


        Assertions.assertEquals(0, coreListener.count);
        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
    }

    @Test
    public void testReloadNullSource() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");
        ConfigSource source = new MapConfigSource(configs);

        var sourcePackage = new ConfigSourcePackage(source, List.of(), Tags.of());

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            null,
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, coreReloadListenersContainer, secretConcealer,
            null, null, Tags.of(), new TagMergingStrategyFallback());

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.reload(sourcePackage));
        assertThat(ex).hasMessage("No sources provided, unable to reload any configs");


        Assertions.assertEquals(0, coreListener.count);
    }

    @Test
    public void testReloadUnknownSource() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())), lexer, new GestaltConfig(),
            configNodeManager, coreReloadListenersContainer, secretConcealer, null,
            new ResultsProcessorManager(List.of(new ErrorResultProcessor(), new DefaultResultProcessor())),
            Tags.of(), new TagMergingStrategyFallback());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class)));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class)));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class)));

        Map<String, String> configs2 = new HashMap<>();
        configs.put("db.name", "test1");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");
        ConfigSource source2 = new MapConfigSource(configs2);
        var sourcePackage2 = new ConfigSourcePackage(source2, List.of(), Tags.of());

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.reload(sourcePackage2));
        assertThat(ex).hasMessage("Can not reload a source that was not registered.");


        Assertions.assertEquals(0, coreListener.count);
        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
    }

    @Test
    public void testReloadNoResults() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigSource source = new MapConfigSource(configs);

        var sourcePackage = new ConfigSourcePackage(source, List.of(), Tags.of());

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);


        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(sourcePackage),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())), lexer, new GestaltConfig(), configNodeManager,
            coreReloadListenersContainer, secretConcealer, null,
            new ResultsProcessorManager(List.of(new ErrorResultProcessor(), new DefaultResultProcessor())),
            Tags.of(), new TagMergingStrategyFallback());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class)));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class)));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class)));

        configs.put("db.name[a]", "test1");

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.reload(sourcePackage));

        assertThat(ex).hasMessage("Failed to load configs from source: mapConfig\n" +
            " - level: ERROR, message: Unable to tokenize element name[a] for path: db.name[a]");


        Assertions.assertEquals(0, coreListener.count);
        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
    }

    @Test
    public void testReloadRemoveListener() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);
        ConfigSourcePackage source = MapConfigSourceBuilder.builder().setCustomConfig(configs).build();
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(source),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())), lexer, new GestaltConfig(),
            configNodeManager, coreReloadListenersContainer, secretConcealer, null,
            new ResultsProcessorManager(List.of(new ErrorResultProcessor(), new DefaultResultProcessor())),
            Tags.of(), new TagMergingStrategyFallback());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class)));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class)));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class)));

        configs.put("db.name", "test1");
        gestalt.reload(source);

        Assertions.assertEquals(1, coreListener.count);
        Assertions.assertEquals("test1", gestalt.getConfig("db.name", TypeCapture.of(String.class)));

        coreReloadListenersContainer.removeListener(coreListener);
        configs.put("db.name", "test2");
        gestalt.reload(source);
        Assertions.assertEquals(1, coreListener.count);
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", TypeCapture.of(String.class)));

    }

    @Test
    public void testDefaultTags() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).setTags(Tags.of("toy", "ball")).build())
            .useCacheDecorator(false)
            .setDefaultTags(Tags.of("toy", "ball"))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class));

        //get the db.port as a Optional
        Assertions.assertEquals(Double.valueOf(3306), gestalt.getConfig("db.port", OptionalDouble.class).getAsDouble());
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", OptionalInt.class).getAsInt());
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", OptionalLong.class).getAsLong());

        //get a non-existent value as an optional
        Assertions.assertFalse(gestalt.getConfig("db.port.none", OptionalDouble.class).isPresent());
        Assertions.assertFalse(gestalt.getConfig("db.port.none", OptionalInt.class).isPresent());
        Assertions.assertFalse(gestalt.getConfig("db.port.none", OptionalLong.class).isPresent());

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", String.class));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", String.class));

        Assertions.assertTrue(gestalt.getConfigOptional("admin[0]", String.class, Tags.of()).isEmpty());
        Assertions.assertTrue(gestalt.getConfigOptional("admin[1]", String.class, Tags.of()).isEmpty());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", TypeCapture.of(String.class)));
        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class)));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<>() {
        }));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class)));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class)));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class)));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<Optional<String>>() {
        }).get());
        Assertions.assertFalse(gestalt.getConfig("does.not.exist", new TypeCapture<Optional<String>>() {
        }).isPresent());
    }

    @Test
    public void reloadListener() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        // increase code coverage for GestaltException
        try {
            throw new GestaltException(new Exception("test"));
        } catch (GestaltException ex) {
            Assertions.assertEquals("java.lang.Exception: test", ex.getMessage());
        }

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");
        SecretConcealer secretConcealer = new SecretConcealerManager(Set.of("secret"), it -> "*****");

        CoreReloadListenersContainer coreReloadListenersContainer = Mockito.mock();

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).setTags(Tags.of("env", "dev")).build()),
            new DecoderRegistry(
                List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(), new OptionalDecoder(),
                    new OptionalDoubleDecoder(), new OptionalIntDecoder(), new OptionalLongDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, coreReloadListenersContainer, secretConcealer,
            null, null, Tags.of("env", "dev"), new TagMergingStrategyFallback());

        gestalt.loadConfigs();

        CoreReloadListener listener = () -> {

        };
        gestalt.registerListener(listener);
        gestalt.removeListener(listener);

        Mockito.verify(coreReloadListenersContainer, Mockito.times(1)).registerListener(Mockito.any());
        Mockito.verify(coreReloadListenersContainer, Mockito.times(1)).removeListener(Mockito.any());
    }

    @Test
    public void testSecretMasking() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "abcdef");
        configs.put("db.uri", "my.sql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .setSecurityMaskingRule(new HashSet<>())
            .addSecurityMaskingRule("port")
            .setSecurityMask("&&&&&")
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        String rootNode = gestalt.debugPrint(Tags.of());

        Assertions.assertEquals("MapNode{db=MapNode{password=LeafNode{value='test'}, " +
            "port=LeafNode{value='&&&&&'}, uri=LeafNode{value='my.sql.com'}}}", rootNode);
    }

    @Test
    public void testSecretMaskingDefault() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "abcdef");
        configs.put("db.uri", "my.sql.com");
        configs.put("db.salt", "pepper");
        configs.put("db.secret.user", "12345");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        String rootNode = gestalt.debugPrint(Tags.of());

        Assertions.assertEquals("MapNode{db=MapNode{password=LeafNode{value='*****'}, salt=LeafNode{value='*****'}, " +
            "port=LeafNode{value='abcdef'}, secret=MapNode{user=LeafNode{value='*****'}}, uri=LeafNode{value='my.sql.com'}}}", rootNode);
    }

    @Test
    public void testSecretMaskingHash() throws GestaltException, NoSuchAlgorithmException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "abcdef");
        configs.put("db.uri", "my.sql.com");
        configs.put("db.salt", "pepper");
        configs.put("db.secret.user", "12345");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .setSecretObfuscation(new MD5SecretObfuscator())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        String rootNode = gestalt.debugPrint(Tags.of());

        Assertions.assertEquals("MapNode{db=MapNode{password=LeafNode{value='098f6bcd4621d373cade4e832627b4f6'}, " +
            "salt=LeafNode{value='b3f952d5d9adea6f63bee9d4c6fceeaa'}, port=LeafNode{value='abcdef'}, " +
            "secret=MapNode{user=LeafNode{value='827ccb0eea8a706c4c34a16891f84e7b'}}, uri=LeafNode{value='my.sql.com'}}}", rootNode);
    }

    @Test
    public void testDebugPrint() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "abcdef");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "hijklm");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).addTags(Tags.environment("dev")).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .setSecurityMaskingRule(new HashSet<>())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        String rootNode = gestalt.debugPrint(Tags.of());

        Assertions.assertEquals("MapNode{db=MapNode{password=LeafNode{value='test'}, port=LeafNode{value='abcdef'}, " +
            "uri=LeafNode{value='my.sql.com'}}}", rootNode);

        String devNode = gestalt.debugPrint(Tags.environment("dev"));

        Assertions.assertEquals("MapNode{db=MapNode{password=LeafNode{value='test2'}, port=LeafNode{value='hijklm'}, " +
            "uri=LeafNode{value='my.postgresql.com'}}}", devNode);

        String allNodes = gestalt.debugPrint();

        Assertions.assertEquals("tags: Tags{[]} = MapNode{db=MapNode{password=LeafNode{value='test'}, " +
            "port=LeafNode{value='abcdef'}, uri=LeafNode{value='my.sql.com'}}}\n" +
            "tags: Tags{[Tag{key='environment', value='dev'}]} = MapNode{db=MapNode{password=LeafNode{value='test2'}, " +
            "port=LeafNode{value='hijklm'}, uri=LeafNode{value='my.postgresql.com'}}}", allNodes);
    }

    @Test
    public void testNoDefaultSource() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "1234");
        configs.put("db.uri", "my.sql.com");


        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).addTags(Tags.environment("dev")).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertTrue(gestalt.getConfigOptional("db.password", String.class).isEmpty());
        Assertions.assertTrue(gestalt.getConfigOptional("db.port", Integer.class).isEmpty());
        Assertions.assertTrue(gestalt.getConfigOptional("db.uri", String.class).isEmpty());

        Assertions.assertEquals("test",
            gestalt.getConfigOptional("db.password", String.class, Tags.environment("dev")).get());
        Assertions.assertEquals(1234,
            gestalt.getConfigOptional("db.port", Integer.class, Tags.environment("dev")).get());
        Assertions.assertEquals("my.sql.com",
            gestalt.getConfigOptional("db.uri", String.class, Tags.environment("dev")).get());
    }

    @Test
    public void testTagsOnBuilder() throws GestaltException {
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
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).addTags(Tags.environment("dev")).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals(123, gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals("my.sql.com", gestalt.getConfig("db.uri", String.class));

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));
        Assertions.assertEquals(456, gestalt.getConfig("db.port", Integer.class, Tags.environment("dev")));
        Assertions.assertEquals("my.postgresql.com", gestalt.getConfig("db.uri", String.class, Tags.environment("dev")));
    }

    @Test
    @SuppressWarnings("removal")
    public void testTagsOnBuilderAndSource() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");

        Map<String, String> configs3 = new HashMap<>();
        configs3.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).addTags(Tags.environment("dev")).build())
            .addSource(new MapConfigSource(configs3, Tags.environment("dev")))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals(123, gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals("my.sql.com", gestalt.getConfig("db.uri", String.class));

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));
        Assertions.assertEquals(456, gestalt.getConfig("db.port", Integer.class, Tags.environment("dev")));
        Assertions.assertEquals("my.postgresql.com", gestalt.getConfig("db.uri", String.class, Tags.environment("dev")));
    }

    @Test
    @SuppressWarnings("removal")
    public void testTagsOnSource() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(new MapConfigSource(configs))
            .addSource(new MapConfigSource(configs2, Tags.environment("dev")))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals(123, gestalt.getConfig("db.port", Integer.class));
        Assertions.assertEquals("my.sql.com", gestalt.getConfig("db.uri", String.class));

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.environment("dev")));
        Assertions.assertEquals(456, gestalt.getConfig("db.port", Integer.class, Tags.environment("dev")));
        Assertions.assertEquals("my.postgresql.com", gestalt.getConfig("db.uri", String.class, Tags.environment("dev")));
    }

    @Test
    public void testObservationsGetError() throws GestaltException {

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

        var ex = Assertions.assertThrows(GestaltException.class,
            () -> gestalt.getConfig("db.password", Integer.class, Tags.environment("dev")));

        Assertions.assertEquals("Failed getting config path: db.password, for class: java.lang.Integer\n" +
            " - level: ERROR, message: Unable to parse a number on Path: db.password, from node: LeafNode{value='test2'} " +
            "attempting to decode Integer", ex.getMessage());
    }

    @Test
    public void testGetWarning() throws GestaltException {

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
            .setTreatWarningsAsErrors(false)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals((byte) 't', gestalt.getConfig("db.password", Byte.class, Tags.environment("dev")));
    }

    @Test
    public void testGetWarningAsErrors() throws GestaltException {

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
            .setTreatWarningsAsErrors(true)
            .build();

        gestalt.loadConfigs();

        var ex = Assertions.assertThrows(GestaltException.class,
            () -> gestalt.getConfig("db.password", Byte.class, Tags.environment("dev")));

        Assertions.assertEquals("Failed getting config path: db.password, for class: java.lang.Byte\n" +
                " - level: WARN, message: Expected a Byte on path: db.password, decoding node: " +
                "LeafNode{value='*****'} received the wrong size",
            ex.getMessage());
    }

    @Test
    public void testCaseSensitive() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.uri", "test");
        configs.put("db.port", "3306");
        configs.put("db.URI", "TEST");
        configs.put("db.PORT", "1234");
        configs.put("db.password", "abc123");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            // do not normalize the sentence return it as is.
            .setSentenceLexer(new PathLexer(".", DEFAULT_EVALUATOR, (it) -> it))
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertNotNull(((GestaltCore) gestalt).getDecoderContext());
        Assertions.assertNotNull(((GestaltCore) gestalt).getDecoderService());

        Assertions.assertEquals("test", gestalt.getConfig("db.uri", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));

        Assertions.assertEquals("TEST", gestalt.getConfig("db.URI", String.class));
        Assertions.assertEquals("1234", gestalt.getConfig("db.PORT", String.class));

        Assertions.assertEquals("abc123", gestalt.getConfig("db.password", String.class));

        Assertions.assertTrue(gestalt.getConfigOptional("db.Uri", String.class).isEmpty());

        Assertions.assertEquals("test", gestalt.getConfig("db", DBInfo.class).getUri());
        Assertions.assertEquals(3306, gestalt.getConfig("db", DBInfo.class).getPort());
        Assertions.assertEquals("abc123", gestalt.getConfig("db", DBInfo.class).getPassword());
    }

    @Test
    public void integrationTestCustomEnvironmentVariablesStyle() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("DB__URI", "test");
        configs.put("DB__PORT", "3306");
        configs.put("DB__PASSWORD", "abc123");

        try (MockedStatic<SystemWrapper> mocked = mockStatic(SystemWrapper.class)) {
            mocked.when(SystemWrapper::getEnvVars).thenReturn(configs);

            GestaltBuilder builder = new GestaltBuilder();
            Gestalt gestalt = builder
                .addSource(EnvironmentConfigSourceBuilder.builder().build())
                .addModuleConfig(EnvironmentVarsLoaderModuleConfigBuilder
                    .builder()
                    .setLexer(new PathLexer("__"))
                    .build())
                .build();

            gestalt.loadConfigs();

            Assertions.assertEquals("test", gestalt.getConfig("db.uri", String.class));
            Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
            Assertions.assertEquals("abc123", gestalt.getConfig("db.password", String.class));

            Assertions.assertEquals("test", gestalt.getConfig("db", DBInfo.class).getUri());
            Assertions.assertEquals(3306, gestalt.getConfig("db", DBInfo.class).getPort());
            Assertions.assertEquals("abc123", gestalt.getConfig("db", DBInfo.class).getPassword());
        }
    }

    @Test
    public void testRelaxedLexer() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.uri", "test");
        configs.put("db_port", "3306");
        configs.put("db-password", "abc123");
        configs.put("dbTimeout", "1000");

        SentenceLexer lexer = PathLexerBuilder.builder()
            .setDelimiter("([._-])|(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[0-9])(?=[A-Z][a-z])|(?<=[a-zA-Z])(?=[0-9])")
            .build();

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            // do not normalize the sentence return it as is.
            .setSentenceLexer(lexer)
            .build();

        gestalt.loadConfigs();
        Assertions.assertInstanceOf(GestaltCore.class, gestalt);
        List<ValidationError> errors = ((GestaltCore) gestalt).getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.uri", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));


        Assertions.assertEquals("abc123", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("1000", gestalt.getConfig("db.timeout", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db", DBInfo.class).getUri());
        Assertions.assertEquals(3306, gestalt.getConfig("db", DBInfo.class).getPort());
        Assertions.assertEquals("abc123", gestalt.getConfig("db", DBInfo.class).getPassword());
    }

    @Test
    public void testMergeTags() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Map<String, String> configs3 = new HashMap<>();
        configs3.put("db.password", "test3");
        configs3.put("db.port", "789");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.profile("one")).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs3).setTags(Tags.profiles("one", "two")).build())
            .setTreatWarningsAsErrors(true)
            .setTagMergingStrategy(new TagMergingStrategyCombine())
            .setDefaultTags(Tags.profile("one"))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("456", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals("my.postgresql.com", gestalt.getConfig("db.uri", String.class));

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.profile("one")));
        Assertions.assertEquals("456", gestalt.getConfig("db.port", String.class, Tags.profile("one")));
        Assertions.assertEquals("my.postgresql.com", gestalt.getConfig("db.uri", String.class, Tags.profile("one")));

        Assertions.assertEquals("test3", gestalt.getConfig("db.password", String.class, Tags.profile("two")));
        Assertions.assertEquals("789", gestalt.getConfig("db.port", String.class, Tags.profile("two")));
        Assertions.assertEquals("my.sql.com", gestalt.getConfig("db.uri", String.class, Tags.profile("two")));

    }


    public static class TestConfigNodeProcessor implements ConfigNodeProcessor {
        private final String add;

        public TestConfigNodeProcessor(String add) {
            this.add = add;
        }

        @Override
        public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
            if (currentNode instanceof LeafNode) {
                return GResultOf.result(new LeafNode(currentNode.getValue().get() + " " + add));
            }
            return GResultOf.result(currentNode);
        }
    }

    public static class TestConfigNodeProcessorSwapNodes implements ConfigNodeProcessor {
        private final String node1;
        private final String node2;

        public TestConfigNodeProcessorSwapNodes(String node1, String node2) {
            this.node1 = node1;
            this.node2 = node2;
        }

        @Override
        public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
            if (currentNode instanceof MapNode) {
                Map<String, ConfigNode> mapNode = ((MapNode) currentNode).getMapNode();
                Map<String, ConfigNode> newMapNode = new HashMap<>(mapNode);
                if (mapNode.containsKey(node1) && mapNode.containsKey(node2)) {

                    ConfigNode configNode1 = newMapNode.get(node1);
                    ConfigNode configNode2 = newMapNode.get(node2);

                    newMapNode.put(node1, configNode2);
                    newMapNode.put(node2, configNode1);
                }
                return GResultOf.result(new MapNode(newMapNode));
            }
            return GResultOf.result(currentNode);
        }
    }

    private static class CoreListener implements CoreReloadListener {

        public int count = 0;

        public CoreListener() {

        }

        @Override
        public void reload() {
            count++;
        }
    }

    public static class ExceptionDecoder extends LeafDecoder<String> {

        @Override
        public Priority priority() {
            return Priority.MEDIUM;
        }

        @Override
        public String name() {
            return "String";
        }

        @Override
        public boolean canDecode(String path, Tags tags, ConfigNode configNode, TypeCapture<?> klass) {
            return String.class.isAssignableFrom(klass.getRawType());
        }

        @Override
        protected GResultOf<String> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
            return GResultOf.errors(new ValidationError.ArrayInvalidIndex(1, "should not happen"));
        }
    }
}
