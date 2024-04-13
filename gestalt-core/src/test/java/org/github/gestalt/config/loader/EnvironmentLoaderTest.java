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
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.parser.MapConfigParser;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;

@SuppressWarnings("removal")
class EnvironmentLoaderTest {

    @Test
    void accepts() {
        EnvironmentVarsLoader envVarsLoader = new EnvironmentVarsLoader();
        Assertions.assertFalse(envVarsLoader.accepts("properties"));
        Assertions.assertTrue(envVarsLoader.accepts("envVars"));
    }

    @Test
    void loadSourceMockDependenciesAllOk() throws GestaltException {
        // setup the input data, instead of loading from a file.
        List<Pair<String, String>> data = new ArrayList<>();
        data.add(new Pair<>("test", "value"));
        data.add(new Pair<>("db.name", "redis"));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(any(), anyList(), eq(false))).thenReturn(GResultOf.result(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.result(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(GResultOf.result(List.of(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source, so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenReturn(data);
        Mockito.when(source.getTags()).thenReturn(Tags.of());

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(lexer, parser);

        // run the code under test.
        var results = environmentVarsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
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
    void loadSourceMockDependenciesAllOkWithTags() throws GestaltException {
        // setup the input data, instead of loading from a file.
        List<Pair<String, String>> data = new ArrayList<>();
        data.add(new Pair<>("test", "value"));
        data.add(new Pair<>("db.name", "redis"));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(any(), anyList(), eq(false))).thenReturn(GResultOf.result(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.result(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(GResultOf.result(List.of(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenReturn(data);
        Mockito.when(source.getTags()).thenReturn(Tags.of());

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(lexer, parser);

        // run the code under test.
        var results = environmentVarsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of("toy", "ball")));
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
        // setup the input data, instead of loading from a file.
        List<Pair<String, String>> data = new ArrayList<>();
        data.add(new Pair<>("test", "value"));
        data.add(new Pair<>("db.name", "redis"));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(any(), anyList(), eq(false))).thenReturn(GResultOf.result(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.result(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(GResultOf.errors(
                List.of(new ValidationError.FailedToTokenizeElement("name", "db.name"),
                    new ValidationError.EmptyPath())));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenReturn(data);
        Mockito.when(source.name()).thenReturn("mock");
        Mockito.when(source.failOnErrors()).thenReturn(true);

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(lexer, parser);

        // run the code under test.
        var results = environmentVarsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(2, results.getErrors().size());
        Assertions.assertTrue("empty path provided".equals(results.getErrors().get(0).description()) ||
            "Unable to tokenize element name for path: db.name".equals(results.getErrors().get(0).description()));
        Assertions.assertTrue("empty path provided".equals(results.getErrors().get(1).description()) ||
            "Unable to tokenize element name for path: db.name".equals(results.getErrors().get(1).description()));


        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any(), any(), eq(true));
    }

    @Test
    void loadSourceMockDependenciesValidationAsWarnings() throws GestaltException {
        // setup the input data, instead of loading from a file.
        List<Pair<String, String>> data = new ArrayList<>();
        data.add(new Pair<>("test", "value"));
        data.add(new Pair<>("db.name", "redis"));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(any(), anyList(), anyBoolean())).thenReturn(GResultOf.result(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.result(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(GResultOf.errors(
                List.of(new ValidationError.FailedToTokenizeElement("name", "db.name"),
                    new ValidationError.EmptyPath())));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenReturn(data);
        Mockito.when(source.name()).thenReturn("mock");
        Mockito.when(source.failOnErrors()).thenReturn(false);
        Mockito.when(source.getTags()).thenReturn(Tags.of());

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(lexer, parser);

        // run the code under test.
        try {
            var results = environmentVarsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

            Assertions.assertTrue(results.hasResults());
            Assertions.assertTrue(results.hasErrors());

            Assertions.assertEquals(2, results.getErrors().size());
            Assertions.assertTrue("empty path provided".equals(results.getErrors().get(0).description()) ||
                "Unable to tokenize element name for path: db.name".equals(results.getErrors().get(0).description()));
            Assertions.assertTrue("empty path provided".equals(results.getErrors().get(1).description()) ||
                "Unable to tokenize element name for path: db.name".equals(results.getErrors().get(1).description()));

            List<ConfigNodeContainer> result = results.results();
            // result we got back the expected result. Not a of value testing this as it is only a mock.
            Assertions.assertEquals(1, result.size());
            Assertions.assertEquals(node, result.get(0).getConfigNode());

        } catch (GestaltException e) {
            Assertions.fail("should not hit this");
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(any(), any(), anyBoolean());

    }

    @Test

    void loadSourceMockDependenciesValidationWarn() throws GestaltException {
        // setup the input data, instead of loading from a file.
        List<Pair<String, String>> data = new ArrayList<>();
        data.add(new Pair<>("test", "value"));
        data.add(new Pair<>("db.name", "redis"));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(any(), anyList(), eq(false))).thenReturn(GResultOf.result(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(GResultOf.errors(new ValidationError.EmptyPath()));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(GResultOf.result(List.of(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenReturn(data);
        Mockito.when(source.getTags()).thenReturn(Tags.of());

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(lexer, parser);

        // run the code under test.
        var results = environmentVarsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
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
    void loadSourceMockDependenciesNoList() {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(false);
        Mockito.when(source.name()).thenReturn("mock");

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(lexer, parser);

        // run the code under test.
        GestaltException e = Assertions.assertThrows(GestaltException.class,
            () -> environmentVarsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of())));
        Assertions.assertEquals("Config source: mock does not have a list to load.", e.getMessage());


        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any(), any(), eq(false));

    }


    @Test
    void loadSourceMockDependenciesExceptionLoadList() throws GestaltException {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenThrow(new GestaltException("bad stream"));
        Mockito.when(source.name()).thenReturn("mock");

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(lexer, parser);

        // run the code under test.
        GestaltException e = Assertions.assertThrows(GestaltException.class,
            () -> environmentVarsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of())));
        Assertions.assertEquals("bad stream", e.getMessage());

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any(), any(), eq(false));
    }

    @Test
    void loadSourceBadSource() throws GestaltException {

        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(lexer, parser);

        StringConfigSource source = new StringConfigSource(
            "path : ${DB_IDLETIMEOUT}", "conf");

        GestaltException e = Assertions.assertThrows(GestaltException.class,
            () -> environmentVarsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of())));
        Assertions.assertEquals("Config source: String format: conf does not have a list to load.", e.getMessage());
    }

    @Test
    void loadSourceMockDependenciesEmpty() throws GestaltException {
        // setup the input data, instead of loading from a file.
        List<Pair<String, String>> data = new ArrayList<>();

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);


        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenReturn(data);

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(lexer, parser);

        // run the code under test.
        var results = environmentVarsLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(1, results.results().size());

        Assertions.assertInstanceOf(MapNode.class, results.results().get(0).getConfigNode());
        Assertions.assertEquals(0, ((MapNode) results.results().get(0).getConfigNode()).size());

    }

    @Test
    void loadSource() throws GestaltException {
        MapConfigSource source = getConfigSource();

        EnvironmentVarsLoader loader = new EnvironmentVarsLoader();

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
    void loadSourceModuleConfig() throws GestaltException {
        MapConfigSource source = getConfigSource();

        var configParser = new MapConfigParser();
        var lexer = new PathLexer("_");
        var moduleConfig = EnvironmentVarsLoaderModuleConfigBuilder.builder()
            .setConfigParser(configParser)
            .setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        EnvironmentVarsLoader loader = new EnvironmentVarsLoader();
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
    void loadSourceModuleConfigConstructor() throws GestaltException {
        MapConfigSource source = getConfigSource();

        var configParser = new MapConfigParser();
        var lexer = new PathLexer(".");
        var moduleConfig = EnvironmentVarsLoaderModuleConfigBuilder.builder()
            .setConfigParser(configParser)
            .setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        EnvironmentVarsLoader loader = new EnvironmentVarsLoader(new PathLexer("_"), new MapConfigParser());
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

    private static MapConfigSource getConfigSource() {
        Map<String, String> configMap = new HashMap<>();

        configMap.put("NAME", "Steve");
        configMap.put("AGE", "42");
        configMap.put("CARS[0]_NAME", "Ford");
        configMap.put("CARS[0]_MODELS", "Fiesta, Focus, Mustang");
        configMap.put("CARS[1]_NAME", "BMW");
        configMap.put("CARS[1]_MODELS", "320, X3, X5");
        configMap.put("CARS[2]_NAME", "Fiat");
        configMap.put("CARS[2]_MODELS", "500, Panda");

        return new MapConfigSource(configMap);
    }

    @Test
    void loadSourceModuleConfigDontFallbackToGestaltLexer() throws GestaltException {
        Map<String, String> configMap = new HashMap<>();

        configMap.put("NAME", "Steve");
        configMap.put("AGE", "42");
        configMap.put("CARS[0]_NAME", "Ford");
        configMap.put("CARS[0]_MODELS", "Fiesta, Focus, Mustang");
        configMap.put("CARS[1]_NAME", "BMW");
        configMap.put("CARS[1]_MODELS", "320, X3, X5");
        configMap.put("CARS[2]_NAME", "Fiat");
        configMap.put("CARS[2]_MODELS", "500, Panda");

        var configParser = new MapConfigParser();
        var moduleConfig = EnvironmentVarsLoaderModuleConfigBuilder.builder()
            .setConfigParser(configParser)
            //.setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);
        config.setSentenceLexer(new PathLexer());

        EnvironmentVarsLoader loader = new EnvironmentVarsLoader();
        loader.applyConfig(config);

        MapConfigSource source = new MapConfigSource(configMap);
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

}
