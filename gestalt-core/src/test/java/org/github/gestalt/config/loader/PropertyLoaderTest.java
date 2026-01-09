package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.parser.MapConfigParser;
import org.github.gestalt.config.source.*;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.*;

class PropertyLoaderTest {

    @Test
    void accepts() {
        PropertyLoader propsLoader = new PropertyLoader();
        Assertions.assertFalse(propsLoader.accepts("envVars"));
        Assertions.assertTrue(propsLoader.accepts("properties"));
        Assertions.assertTrue(propsLoader.accepts("props"));
    }

    @Test
    void loadSourceMockDependenciesAllOk() throws GestaltException {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // setup the input stream, instead of loading from a file.
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test = value\ndb.name = redis".getBytes(UTF_8));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(any(), anyList(), eq(false))).thenReturn(GResultOf.result(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.result(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(GResultOf.result(List.of(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenReturn(inputStream);

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        GResultOf<List<ConfigNodeContainer>> results = propsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        //setup the argument captor
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<List<Token>, ConfigValue>>> argument = ArgumentCaptor.forClass(List.class);

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(any(), argument.capture(), eq(false));

        // result the parser was sent the correct arguments.
        Assertions.assertEquals(2, argument.getValue().size());

        Assertions.assertEquals(1, argument.getValue().get(0).getFirst().size());
        Assertions.assertEquals("test", ((ObjectToken) argument.getValue().get(0).getFirst().get(0)).getName());
        Assertions.assertEquals("value", argument.getValue().get(0).getSecond().getValue());

        Assertions.assertEquals(2, argument.getValue().get(1).getFirst().size());
        Assertions.assertEquals("db", ((ObjectToken) argument.getValue().get(1).getFirst().get(0)).getName());
        Assertions.assertEquals("name", ((ObjectToken) argument.getValue().get(1).getFirst().get(1)).getName());
        Assertions.assertEquals("redis", argument.getValue().get(1).getSecond().getValue());

        List<ConfigNodeContainer> result = results.results();
        // result we got back the expected result. Not a of value testing this as it is only a mock.
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(node, result.get(0).getConfigNode());
    }

    @Test
    void loadSourceMockDependenciesAllOkTags() throws GestaltException {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // setup the input stream, instead of loading from a file.
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test = value\ndb.name = redis".getBytes(UTF_8));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(any(), anyList(), eq(false))).thenReturn(GResultOf.result(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.result(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(GResultOf.result(List.of(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenReturn(inputStream);

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        var results = propsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of("toy", "ball")));
        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        //setup the argument captor
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<List<Token>, ConfigValue>>> argument = ArgumentCaptor.forClass(List.class);

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(any(), argument.capture(), eq(false));

        // result the parser was sent the correct arguments.
        Assertions.assertEquals(2, argument.getValue().size());

        Assertions.assertEquals(1, argument.getValue().get(0).getFirst().size());
        Assertions.assertEquals("test", ((ObjectToken) argument.getValue().get(0).getFirst().get(0)).getName());
        Assertions.assertEquals("value", argument.getValue().get(0).getSecond().getValue());

        Assertions.assertEquals(2, argument.getValue().get(1).getFirst().size());
        Assertions.assertEquals("db", ((ObjectToken) argument.getValue().get(1).getFirst().get(0)).getName());
        Assertions.assertEquals("name", ((ObjectToken) argument.getValue().get(1).getFirst().get(1)).getName());
        Assertions.assertEquals("redis", argument.getValue().get(1).getSecond().getValue());

        List<ConfigNodeContainer> result = results.results();
        // result we got back the expected result. Not a of value testing this as it is only a mock.
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(node, result.get(0).getConfigNode());
        Assertions.assertEquals(Tags.of("toy", "ball"), result.get(0).getTags());
    }

    @Test
    void loadSourceMockDependenciesValidationErrors() throws GestaltException {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // setup the input stream, instead of loading from a file.
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test = value\ndb.name = redis".getBytes(UTF_8));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(any(), anyList(), eq(false))).thenReturn(GResultOf.result(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.result(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(GResultOf.errors(
                List.of(new ValidationError.FailedToTokenizeElement("name", "db.name"),
                    new ValidationError.EmptyPath())));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenReturn(inputStream);
        Mockito.when(source.name()).thenReturn("mock");

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        GResultOf<List<ConfigNodeContainer>> results = propsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
        Assertions.assertTrue("empty path provided".equals(results.getErrors().get(0).description()) ||
            "Unable to tokenize element name for path: db.name".equals(results.getErrors().get(0).description()));
        Assertions.assertTrue("empty path provided".equals(results.getErrors().get(1).description()) ||
            "Unable to tokenize element name for path: db.name".equals(results.getErrors().get(1).description()));


        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(any(), any(), eq(false));
    }

    @Test
    void loadSourceMockDependenciesValidationWarn() throws GestaltException {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // setup the input stream, instead of loading from a file.
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test = value\ndb.name = redis".getBytes(UTF_8));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(any(), anyList(), eq(false))).thenReturn(GResultOf.result(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.errors(new ValidationError.EmptyPath()));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(GResultOf.result(List.of(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenReturn(inputStream);

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        GResultOf<List<ConfigNodeContainer>> results = propsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
        Assertions.assertTrue(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        //setup the argument captor
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<List<Token>, ConfigValue>>> argument = ArgumentCaptor.forClass(List.class);

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(any(), argument.capture(), eq(false));

        // result the parser was sent the correct arguments.
        Assertions.assertEquals(1, argument.getValue().size());

        Assertions.assertEquals(2, argument.getValue().get(0).getFirst().size());
        Assertions.assertEquals("db", ((ObjectToken) argument.getValue().get(0).getFirst().get(0)).getName());
        Assertions.assertEquals("name", ((ObjectToken) argument.getValue().get(0).getFirst().get(1)).getName());
        Assertions.assertEquals("redis", argument.getValue().get(0).getSecond().getValue());

        List<ConfigNodeContainer> result = results.results();
        // result we got back the expected result. Not a of value testing this as it is only a mock.
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(node, result.get(0).getConfigNode());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("empty path provided", results.getErrors().get(0).description());
    }

    @Test
    void loadSourceMockDependenciesNoStream() {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(false);
        Mockito.when(source.name()).thenReturn("mock");

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        try {
            propsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("Config source: mock does not have a stream to load.", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any(), any(), eq(false));

    }

    @Test
    void loadSourceMockDependenciesExceptionLoadStream() throws GestaltException {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenThrow(new GestaltException("bad stream"));
        Mockito.when(source.name()).thenReturn("mock");

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        try {
            propsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("bad stream", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any(), any(), eq(false));
    }

    @Test
    void loadSourceMockDependenciesBadStream() throws GestaltException {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenReturn(null);
        Mockito.when(source.name()).thenReturn("mock");

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        try {
            propsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("Exception loading source: mock", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any(), any(), eq(false));
    }

    @Test
    void loadSourceBadSource() {

        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);

        Map<String, String> configs = new HashMap<>();
        MapConfigSource source = new MapConfigSource(configs);

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        try {
            propsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Config source: mapConfig does not have a stream to load.", e.getMessage());
        }
    }

    @Test
    void loadSourceEmpty() throws GestaltException {

        StringConfigSource source = new StringConfigSource("", "properties");

        PropertyLoader propertyLoader = new PropertyLoader();

        var resultContainer = propertyLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals(0, result.size());
    }


    @Test
    void loadSource() throws GestaltException, IOException {
        InputStreamConfigSource source = getConfigSource();

        PropertyLoader loader = new PropertyLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = loader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals(3, result.getKey("cars").get().size());
        Assertions.assertEquals("Ford", result.getKey("cars").get().getIndex(0).get().getKey("name")
            .get().getValue().get());
        Assertions.assertEquals("Fiesta, Focus, Mustang", result.getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getValue().get());

        Assertions.assertFalse(result.getKey("cars").get().getIndex(3).isPresent());
    }

    @Test
    void loadSourceModuleConfig() throws GestaltException, IOException {
        InputStreamConfigSource source = getConfigSource();

        var configParser = new MapConfigParser();
        var lexer = new PathLexer(".");
        var moduleConfig = MapConfigLoaderModuleConfigBuilder.builder()
            .setConfigParser(configParser)
            .setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        PropertyLoader loader = new PropertyLoader();
        loader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = loader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals(3, result.getKey("cars").get().size());
        Assertions.assertEquals("Ford", result.getKey("cars").get().getIndex(0).get().getKey("name")
            .get().getValue().get());
        Assertions.assertEquals("Fiesta, Focus, Mustang", result.getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getValue().get());

        Assertions.assertFalse(result.getKey("cars").get().getIndex(3).isPresent());
    }

    @Test
    void loadSourceModuleConfigConstructor() throws GestaltException, IOException {
        InputStreamConfigSource source = getConfigSource();

        var configParser = new MapConfigParser();
        var lexer = new PathLexer("_"); // bad config, but shouldnt be used.
        var moduleConfig = MapConfigLoaderModuleConfigBuilder.builder()
            .setConfigParser(configParser)
            .setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        PropertyLoader loader = new PropertyLoader(new PathLexer(), new MapConfigParser());
        loader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = loader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals(3, result.getKey("cars").get().size());
        Assertions.assertEquals("Ford", result.getKey("cars").get().getIndex(0).get().getKey("name")
            .get().getValue().get());
        Assertions.assertEquals("Fiesta, Focus, Mustang", result.getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getValue().get());

        Assertions.assertFalse(result.getKey("cars").get().getIndex(3).isPresent());
    }

    private static InputStreamConfigSource getConfigSource() throws IOException, GestaltException {
        Properties configMap = new Properties();

        configMap.put("name", "Steve");
        configMap.put("age", "42");
        configMap.put("cars[0].name", "Ford");
        configMap.put("cars[0].models", "Fiesta, Focus, Mustang");
        configMap.put("cars[1].name", "BMW");
        configMap.put("cars[1].models", "320, X3, X5");
        configMap.put("cars[2].name", "Fiat");
        configMap.put("cars[2].models", "500, Panda");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        configMap.store(outputStream, "");
        return new InputStreamConfigSource(new ByteArrayInputStream(outputStream.toByteArray()), "properties");
    }

    @Test
    void loadSourceModuleConfigDontFallbackToGestaltLexer() throws GestaltException, IOException {
        Properties configMap = new Properties();

        configMap.put("name", "Steve");
        configMap.put("age", "42");
        configMap.put("cars[0].name", "Ford");
        configMap.put("cars[0].models", "Fiesta, Focus, Mustang");
        configMap.put("cars[1].name", "BMW");
        configMap.put("cars[1].models", "320, X3, X5");
        configMap.put("cars[2].name", "Fiat");
        configMap.put("cars[2].models", "500, Panda");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        configMap.store(outputStream, "");

        var configParser = new MapConfigParser();
        var lexer = new PathLexer(".");
        var moduleConfig = MapConfigLoaderModuleConfigBuilder.builder()
            .setConfigParser(configParser)
            //.setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);
        config.setSentenceLexer(lexer);

        PropertyLoader loader = new PropertyLoader();
        loader.applyConfig(config);

        InputStreamConfigSource source = new InputStreamConfigSource(new ByteArrayInputStream(outputStream.toByteArray()), "properties");
        GResultOf<List<ConfigNodeContainer>> resultContainer = loader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals(3, result.getKey("cars").get().size());
        Assertions.assertEquals("Ford", result.getKey("cars").get().getIndex(0).get().getKey("name")
            .get().getValue().get());
        Assertions.assertEquals("Fiesta, Focus, Mustang", result.getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getValue().get());

        Assertions.assertFalse(result.getKey("cars").get().getIndex(3).isPresent());
    }

    @Test
    void acceptsCustomFileSuffixes() {
        // Test that custom file suffixes can be configured
        PropertyLoader propsLoader = new PropertyLoader();
        
        // By default, conf is not accepted
        Assertions.assertFalse(propsLoader.accepts("conf"));
        
        // Configure custom suffixes via module config
        Set<String> customSuffixes = new HashSet<>();
        customSuffixes.add("conf");
        customSuffixes.add("config");
        
        PropertyLoaderModuleConfig moduleConfig = new PropertyLoaderModuleConfig(
            new MapConfigParser(), 
            new PathLexer(), 
            customSuffixes
        );
        
        GestaltConfig gestaltConfig = Mockito.mock(GestaltConfig.class);
        Mockito.when(gestaltConfig.getModuleConfig(PropertyLoaderModuleConfig.class)).thenReturn(moduleConfig);
        
        // Apply the configuration
        propsLoader.applyConfig(gestaltConfig);
        
        // Now conf and config should be accepted
        Assertions.assertTrue(propsLoader.accepts("conf"));
        Assertions.assertTrue(propsLoader.accepts("config"));
        
        // But other suffixes should not be accepted
        Assertions.assertFalse(propsLoader.accepts("yaml"));
        
        // The default suffixes should still be accepted
        Assertions.assertTrue(propsLoader.accepts("properties"));
        Assertions.assertTrue(propsLoader.accepts("props"));
    }

    @Test
    void acceptsCustomFileSuffixesWithBuilder() {
        // Test that custom file suffixes can be configured using the builder
        PropertyLoader propsLoader = new PropertyLoader();
        
        // By default, conf is not accepted
        Assertions.assertFalse(propsLoader.accepts("conf"));
        
        // Configure custom suffixes via builder
        PropertyLoaderModuleConfig moduleConfig = PropertyLoaderModuleConfigBuilder.builder()
            .setConfigParser(new MapConfigParser())
            .setLexer(new PathLexer())
            .addCustomFileSuffix("conf")
            .addCustomFileSuffix("config")
            .build();
        
        GestaltConfig gestaltConfig = Mockito.mock(GestaltConfig.class);
        Mockito.when(gestaltConfig.getModuleConfig(PropertyLoaderModuleConfig.class)).thenReturn(moduleConfig);
        
        // Apply the configuration
        propsLoader.applyConfig(gestaltConfig);
        
        // Now conf and config should be accepted
        Assertions.assertTrue(propsLoader.accepts("conf"));
        Assertions.assertTrue(propsLoader.accepts("config"));
        
        // But other suffixes should not be accepted
        Assertions.assertFalse(propsLoader.accepts("yaml"));
        
        // The default suffixes should still be accepted
        Assertions.assertTrue(propsLoader.accepts("properties"));
        Assertions.assertTrue(propsLoader.accepts("props"));
    }
}
