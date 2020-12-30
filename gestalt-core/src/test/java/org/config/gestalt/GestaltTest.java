package org.config.gestalt;

import org.config.gestalt.decoder.*;
import org.config.gestalt.entity.GestaltConfig;
import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.PathLexer;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.loader.ConfigLoader;
import org.config.gestalt.loader.ConfigLoaderRegistry;
import org.config.gestalt.loader.MapConfigLoader;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.ConfigNodeManager;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.source.MapConfigSource;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class GestaltTest {
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

        SentenceLexer lexer = new PathLexer("\\.");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, new GestaltConfig(), configNodeManager);

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
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<Integer>() {
        }));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class)));

        Assertions.assertEquals("John", gestalt.getConfig("admin[0]", TypeCapture.of(String.class)));
        Assertions.assertEquals("Steve", gestalt.getConfig("admin[1]", TypeCapture.of(String.class)));
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
        SentenceLexer lexer = new PathLexer("\\.");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Arrays.asList(new MapConfigSource(configs), new MapConfigSource(configs2)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, new GestaltConfig(), new ConfigNodeManager());

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

        SentenceLexer lexer = new PathLexer("\\.");
        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Arrays.asList(new MapConfigSource(configs), new MapConfigSource(configs2), new MapConfigSource(configs3)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, new GestaltConfig(), new ConfigNodeManager());

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
    public void testGetDefault() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        ConfigNodeManager configNodeManager = new ConfigNodeManager();

        SentenceLexer lexer = new PathLexer("\\.");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, new GestaltConfig(), new ConfigNodeManager());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("test", gestalt.getConfig("db.name", String.class, "aaa"));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class, "aaa"));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", Integer.class, 1));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", Long.class, 2L));

        Assertions.assertEquals(123, gestalt.getConfig("redis.port", Integer.class, 123));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", String.class, "redis.io"));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", String.class, "redis.io"));
        Assertions.assertEquals("Scott", gestalt.getConfig("admin[3]", String.class, "Scott"));

        Assertions.assertEquals("test", gestalt.getConfig("db.name", new TypeCapture<String>() {
        }, "aaa"));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", TypeCapture.of(String.class), "aaa"));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<Integer>() {
        }, 1));
        Assertions.assertEquals(Integer.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Integer.class), 1));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", TypeCapture.of(Long.class), 2L));
        Assertions.assertEquals(Long.valueOf(3306), gestalt.getConfig("db.port", new TypeCapture<Long>() {
        }, 2L));

        Assertions.assertEquals(123, gestalt.getConfig("redis.port", TypeCapture.of(Integer.class), 123));
        Assertions.assertEquals(123, gestalt.getConfig("redis.port", new TypeCapture<Integer>() {
        }, 123));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", TypeCapture.of(String.class), "redis.io"));
        Assertions.assertEquals("redis.io", gestalt.getConfig("redis.uri", TypeCapture.of(String.class), "redis.io"));
        Assertions.assertEquals("Scott", gestalt.getConfig("admin[3]", TypeCapture.of(String.class), "Scott"));
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
        SentenceLexer lexer = new PathLexer("\\.");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, new GestaltConfig(), new ConfigNodeManager());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        Assertions.assertEquals("Scott", gestalt.getConfig("admin[3a]", String.class, "Scott"));
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
        SentenceLexer lexer = new PathLexer("\\.");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Arrays.asList(new MapConfigSource(configs), new MapConfigSource(configs2)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
                new ListDecoder()), configNodeManager, lexer), lexer, config, new ConfigNodeManager());

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
                    " - level: ERROR, message: Unable to find array node for path: admin.user[2], at token: ArrayToken");
        }

        try {
            gestalt.getConfig("admin.user", new TypeCapture<List<String>>() {
            });
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: admin.user, for class: java.util.List<java.lang.String>\n" +
                    " - level: WARN, message: Missing array index: 2");
        }

        try {
            config.setTreatWarningsAsErrors(false);
            config.setTreatMissingArrayIndexAsError(false);
            config.setTreatMissingValuesAsErrors(false);
            List<String> test = gestalt.getConfig("admin.user", new TypeCapture<List<String>>() {
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
        SentenceLexer lexer = new PathLexer("\\.");

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            Collections.emptyList(),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, new GestaltConfig(), new ConfigNodeManager());

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
        SentenceLexer lexer = new PathLexer("\\.");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Collections.singletonList(new StringDecoder()), configNodeManager, lexer),
            lexer, new GestaltConfig(), new ConfigNodeManager());

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
                    " - level: ERROR, message: No decoders found for class: java.lang.Integer");
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
        SentenceLexer lexer = new PathLexer("\\.");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Arrays.asList(new StringDecoder(), new ExceptionDecoder()), configNodeManager, lexer),
            lexer, new GestaltConfig(), new ConfigNodeManager());

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

        SentenceLexer lexer = new PathLexer("\\.");

        Mockito.when(configLoaderRegistry.getLoader(Mockito.anyString())).thenReturn(configLoader);
        Mockito.when(configLoader.loadSource(Mockito.any())).thenReturn(ValidateOf.inValid(new ValidationError.ArrayDuplicateIndex(1, "admin")));

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, new GestaltConfig(), configNodeManager);

        try {
            gestalt.loadConfigs();
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed to load configs\n" +
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

        ConfigLoader mockConfigLoader = Mockito.mock(ConfigLoader.class);
        Mockito.when(mockConfigLoader.accepts(MapConfigSource.MAP_CONFIG)).thenReturn(true);
        Mockito.when(mockConfigLoader.loadSource(Mockito.any())).thenReturn(ValidateOf.validateOf(null, Collections.emptyList()));

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(mockConfigLoader);

        GestaltConfig config = new GestaltConfig();
        config.setTreatWarningsAsErrors(true);
        config.setTreatMissingArrayIndexAsError(false);
        config.setTreatMissingValuesAsErrors(false);

        ConfigNodeManager configNodeManager = new ConfigNodeManager();
        SentenceLexer lexer = new PathLexer("\\.");

        GestaltCore gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, config, new ConfigNodeManager());

        gestalt.loadConfigs();
        List<ValidationError> errors = gestalt.getLoadErrors();
        Assertions.assertEquals(0, errors.size());

        try {
            gestalt.getConfig("db.name", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: db.name, for class: java.lang.String\n" +
                    " - level: WARN, message: Null Nodes on path: db.name");
        }
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
        SentenceLexer lexer = new PathLexer("\\.");

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, config, new ConfigNodeManager());

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("db.password", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: db.password, for class: java.lang.String\n" +
                    " - level: ERROR, message: Unable to find object node for path: db.password, at token: ObjectToken");
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
        SentenceLexer lexer = new PathLexer("\\.");

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            lexer, config, new ConfigNodeManager());

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("admin[3]", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Failed getting config path: admin[3], for class: java.lang.String\n" +
                    " - level: ERROR, message: Unable to find array node for path: admin[3], at token: ArrayToken");
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
        SentenceLexer lexer = new PathLexer("\\.");

        Gestalt gestalt = new GestaltCore(configLoaderRegistry,
            Collections.singletonList(new MapConfigSource(configs)),
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(), new StringDecoder()),
                configNodeManager, lexer),
            new PathLexer("\\."), new GestaltConfig(), new ConfigNodeManager());

        gestalt.loadConfigs();

        try {
            gestalt.getConfig("admin[a3]", String.class);
            Assertions.fail("Should not reach here");
        } catch (GestaltException e) {
            assertThat(e).isInstanceOf(GestaltException.class)
                .hasMessage("Unable to parse path: admin[a3]\n" +
                    " - level: ERROR, message: Unable to tokenize element admin[a3] for path: admin[a3]");
        }
    }

    public static class ExceptionDecoder extends LeafDecoder {

        @Override
        public String name() {
            return "String";
        }

        @Override
        public boolean matches(TypeCapture<?> klass) {
            return klass.isAssignableFrom(String.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected ValidateOf<String> leafDecode(String path, ConfigNode node) {
            return ValidateOf.inValid(new ValidationError.ArrayInvalidIndex(1, "should not happen"));
        }
    }

}
