package org.github.gestalt.config;

import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.decoder.*;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.loader.ConfigLoaderRegistry;
import org.github.gestalt.config.loader.MapConfigLoader;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeManager;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.CoreReloadListenersContainer;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.test.classes.DBInfoOptional;
import org.github.gestalt.config.test.classes.DBInfoPathAnnotation;
import org.github.gestalt.config.test.classes.DBInfoPathMultiAnnotation;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class GestaltTest {

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }

    @Test
    public void test() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(
                List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(), new OptionalDecoder(),
                    new OptionalDoubleDecoder(), new OptionalIntDecoder(), new OptionalLongDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(
                List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(), new OptionalDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null, Collections.emptyList(), Tags.of());

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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).setTags(Tags.of("toys", "ball")).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer();

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(
                List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(), new ObjectDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer();

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(
                List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(), new ObjectDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build(),
                MapConfigSourceBuilder.builder().setCustomConfig(configs2).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");
        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build(),
                MapConfigSourceBuilder.builder().setCustomConfig(configs2).build(),
                MapConfigSourceBuilder.builder().setCustomConfig(configs3).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");
        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build(),
                MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.of("toy", "ball")).build(),
                MapConfigSourceBuilder.builder().setCustomConfig(configs3).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null,
            Collections.singletonList(new TestPostProcessor("aaa")), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
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

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null,
            Collections.singletonList(new TestPostProcessor("aaa")), Tags.of());

        Mockito.when(configNodeManager.postProcess(Mockito.any())).thenReturn(GResultOf.resultOf(null, Collections.emptyList()));

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

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null,
            Collections.singletonList(new TestPostProcessor("aaa")), Tags.of());

        Mockito.when(configNodeManager.postProcess(Mockito.any())).thenReturn(
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

        GestaltConfig config = new GestaltConfig();
        config.setTreatMissingValuesAsErrors(false);

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, configNodeManager, null,
            Collections.singletonList(new TestPostProcessor("aaa")), Tags.of());

        Mockito.when(configNodeManager.postProcess(Mockito.any())).thenReturn(
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null,
            List.of(new TestPostProcessorSwapNodes("path1", "path2"),
                new TestPostProcessorSwapNodes("prop1", "prop2")), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("Scott", gestalt.getConfig("admin[3a]", "Scott", String.class));
    }

    @Test
    public void testGetOptional() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

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

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build(),
                MapConfigSourceBuilder.builder().setCustomConfig(configs2).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
                new ListDecoder()), configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("Missing array index: 2 for path: admin.user", errors.get(0).description());

        config.setTreatWarningsAsErrors(true);
        config.setTreatMissingArrayIndexAsError(true);
        config.setTreatMissingValuesAsErrors(true);

        try {
            gestalt.getConfig("admin.user[2]", Integer.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: admin.user[2], for class: java.lang.Integer\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: admin.user[2], for class: ArrayToken, " +
                    "during navigating to next node");
        }

        try {
            gestalt.getConfig("admin.user", new TypeCapture<List<String>>() {
            });
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: admin.user, for class: java.util.List<java.lang.String>\n" +
                    " - level: MISSING_VALUE, message: Missing array index: 2");
        }

        try {
            config.setTreatWarningsAsErrors(false);
            config.setTreatMissingArrayIndexAsError(false);
            config.setTreatMissingValuesAsErrors(false);
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

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            List.of(),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        try {
            gestalt.loadConfigs();
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("No sources provided, unable to load any configs");
        }
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

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(Collections.singletonList(new StringDecoder()), configNodeManager, lexer,
                List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));

        try {
            gestalt.getConfig("db.port", Integer.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: db.port, for class: java.lang.Integer\n" +
                    " - level: ERROR, message: No decoders found for class: java.lang.Integer and node type: leaf");
        }
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

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new StringDecoder(), new ExceptionDecoder()), configNodeManager, lexer,
                List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

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

        Mockito.when(configLoaderRegistry.getLoader(Mockito.anyString())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(Mockito.any())).thenReturn(
            GResultOf.errors(new ValidationError.ArrayDuplicateIndex(1, "admin")));

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null, Collections.emptyList(), Tags.of());

        try {
            gestalt.loadConfigs();
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed to load configs from source: mapConfig\n" +
                    " - level: ERROR, message: Duplicate array index: 1 for path: admin");
        }
    }

    @Test
    public void testNoResultsForNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(true);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(false);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        try {
            gestalt.getConfig("no.exist.name", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: no.exist.name, for class: java.lang.String\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: no.exist.name, " +
                    "for class: ObjectToken, during navigating to next node");
        }
    }

    @Test
    public void testNullResultsForNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(false);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(true);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder(), new ObjectDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        try {
            gestalt.getConfig("db", DBInfo.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfo\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: db.uri, for class: DBInfo, " +
                    "during object decoding\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: db.password, for class: DBInfo, " +
                    "during object decoding");

        }
    }

    @Test
    public void testNullResultsForNodeIgnored() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(false);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(false);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder(), new ObjectDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        try {
            DBInfo dbInfo = gestalt.getConfig("db", DBInfo.class);
            Assertions.assertEquals("test", dbInfo.getPassword());
            Assertions.assertEquals(3306, dbInfo.getPort());
            Assertions.assertNull(dbInfo.getUri());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testOptionalResultsForMissingOkNullFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        try {
            DBInfoOptional dbInfo = gestalt.getConfig("db", DBInfoOptional.class);
            Assertions.assertEquals("test", dbInfo.getPassword().get());
            Assertions.assertEquals(3306, dbInfo.getPort().get());
            Assertions.assertTrue(dbInfo.getUri().isEmpty());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void testOptionalResultsForMissingFail() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build();

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("db", DBInfoOptional.class);
            Assertions.fail("Should not reach here");

        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: db, for class: org.github.gestalt.config.test.classes.DBInfoOptional\n" +
                    " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding Object on path: db.uri, from node: " +
                    "MapNode{mapNode={password=LeafNode{value='test'}, port=LeafNode{value='3306'}}}, with class: DBInfoOptional");
        }
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

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(false);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(true);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("db.password", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: db.password, for class: java.lang.String\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: db.password, for class: ObjectToken, " +
                    "during navigating to next node");
        }
    }

    @Test
    public void testGetArrayNonExistent() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(false);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(true);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, config, new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("admin[3]", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: admin[3], for class: java.lang.String\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: admin[3], for class: ArrayToken, " +
                    "during navigating to next node");
        }
    }

    @Test
    public void testGetBadTokenPath() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer(".");

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            new PathLexer("."), new GestaltConfig(), new ConfigNodeManager(), null, Collections.emptyList(), Tags.of());

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("admin[a3]", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Unable to parse path: admin[a3]\n" +
                    " - level: ERROR, message: Unable to tokenize element admin[a3] for path: admin[a3]");
        }

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
        ConfigSource source = new MapConfigSource(configs);

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(new ConfigSourcePackage(source, List.of())),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, coreReloadListenersContainer, Collections.emptyList(), Tags.of());

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

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, coreReloadListenersContainer, Collections.emptyList(), Tags.of());

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

        try {
            gestalt.reload(null);
        } catch (GestaltException e) {
            assertThat(e).hasMessage("No sources provided, unable to reload any configs");
        }

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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            null,
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, coreReloadListenersContainer, Collections.emptyList(), Tags.of());

        try {
            gestalt.reload(source);
        } catch (GestaltException e) {
            assertThat(e).hasMessage("No sources provided, unable to reload any configs");
        }

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

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).build()),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, coreReloadListenersContainer, Collections.emptyList(), Tags.of());

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

        try {
            gestalt.reload(source2);
        } catch (GestaltException e) {
            assertThat(e).hasMessage("Can not reload a source that was not registered.");
        }

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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        CoreReloadListenersContainer coreReloadListenersContainer = new CoreReloadListenersContainer();
        CoreListener coreListener = new CoreListener();
        coreReloadListenersContainer.registerListener(coreListener);


        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(new ConfigSourcePackage(source, List.of())),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, coreReloadListenersContainer, Collections.emptyList(), Tags.of());

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

        try {
            gestalt.reload(source);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).hasMessage("Failed to load configs from source: mapConfig\n" +
                " - level: ERROR, message: Unable to tokenize element name[a] for path: db.name[a]");
        }

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

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(source),
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, coreReloadListenersContainer, Collections.emptyList(), Tags.of());

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
        gestalt.reload(source.getConfigSource());

        Assertions.assertEquals(1, coreListener.count);
        Assertions.assertEquals("test1", gestalt.getConfig("db.name", TypeCapture.of(String.class)));

        coreReloadListenersContainer.removeListener(coreListener);
        configs.put("db.name", "test2");
        gestalt.reload(source.getConfigSource());
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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).setTags(Tags.of("env", "dev")).build()),
            new DecoderRegistry(
                List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(), new OptionalDecoder(),
                    new OptionalDoubleDecoder(), new OptionalIntDecoder(), new OptionalLongDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, null, Collections.emptyList(), Tags.of("env", "dev"));

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

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

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer(".");

        CoreReloadListenersContainer coreReloadListenersContainer = Mockito.mock();

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            List.of(MapConfigSourceBuilder.builder().setCustomConfig(configs).setTags(Tags.of("env", "dev")).build()),
            new DecoderRegistry(
                List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(), new OptionalDecoder(),
                    new OptionalDoubleDecoder(), new OptionalIntDecoder(), new OptionalLongDecoder()),
                configNodeManager, lexer, List.of(new StandardPathMapper())),
            lexer, new GestaltConfig(), configNodeManager, coreReloadListenersContainer, Collections.emptyList(),
            Tags.of("env", "dev"));

        gestalt.loadConfigs();

        CoreReloadListener listener = () -> {

        };
        gestalt.registerListener(listener);
        gestalt.removeListener(listener);

        Mockito.verify(coreReloadListenersContainer, Mockito.times(1)).registerListener(Mockito.any());
        Mockito.verify(coreReloadListenersContainer, Mockito.times(1)).removeListener(Mockito.any());
    }

    public static class TestPostProcessor implements PostProcessor {
        private final String add;

        public TestPostProcessor(String add) {
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

    public static class TestPostProcessorSwapNodes implements PostProcessor {
        private final String node1;
        private final String node2;

        public TestPostProcessorSwapNodes(String node1, String node2) {
            this.node1 = node1;
            this.node2 = node2;
        }

        @Override
        public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
            if (currentNode instanceof MapNode) {
                Map<String, ConfigNode> mapNode = ((MapNode) currentNode).getMapNode();
                if (mapNode.containsKey(node1) && mapNode.containsKey(node2)) {
                    ConfigNode configNode1 = mapNode.get(node1);
                    ConfigNode configNode2 = mapNode.get(node2);

                    mapNode.put(node1, configNode2);
                    mapNode.put(node2, configNode1);
                }
                return GResultOf.result(new MapNode(mapNode));
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
        protected GResultOf<String> leafDecode(String path, ConfigNode node) {
            return GResultOf.errors(new ValidationError.ArrayInvalidIndex(1, "should not happen"));
        }
    }

}
