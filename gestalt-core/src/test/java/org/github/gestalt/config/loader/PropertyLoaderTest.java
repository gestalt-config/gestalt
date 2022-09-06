package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        Mockito.when(parser.parse(anyList(), eq(false))).thenReturn(ValidateOf.valid(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(ValidateOf.valid(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(ValidateOf.valid(Arrays.asList(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenReturn(inputStream);

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        ValidateOf<List<ConfigNodeContainer>> validateOfResults = propsLoader.loadSource(source);
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());

        //setup the argument captor
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<List<Token>, ConfigValue>>> argument = ArgumentCaptor.forClass(List.class);

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(argument.capture(), eq(false));

        // validate the parser was sent the correct arguments.
        Assertions.assertEquals(2, argument.getValue().size());

        Assertions.assertEquals(1, argument.getValue().get(0).getFirst().size());
        Assertions.assertEquals("test", ((ObjectToken) argument.getValue().get(0).getFirst().get(0)).getName());
        Assertions.assertEquals("value", argument.getValue().get(0).getSecond().getValue());

        Assertions.assertEquals(2, argument.getValue().get(1).getFirst().size());
        Assertions.assertEquals("db", ((ObjectToken) argument.getValue().get(1).getFirst().get(0)).getName());
        Assertions.assertEquals("name", ((ObjectToken) argument.getValue().get(1).getFirst().get(1)).getName());
        Assertions.assertEquals("redis", argument.getValue().get(1).getSecond().getValue());

        List<ConfigNodeContainer> result = validateOfResults.results();
        // validate we got back the expected result. Not a of value testing this as it is only a mock.
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
        Mockito.when(parser.parse(anyList(), eq(false))).thenReturn(ValidateOf.valid(node));
        Mockito.when(lexer.scan("test"))
               .thenReturn(ValidateOf.valid(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
               .thenReturn(ValidateOf.valid(Arrays.asList(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenReturn(inputStream);
        Mockito.when(source.getTags()).thenReturn(Tags.of("toy", "ball"));

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        ValidateOf<List<ConfigNodeContainer>> validateOfResults = propsLoader.loadSource(source);
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());

        //setup the argument captor
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<List<Token>, ConfigValue>>> argument = ArgumentCaptor.forClass(List.class);

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(argument.capture(), eq(false));

        // validate the parser was sent the correct arguments.
        Assertions.assertEquals(2, argument.getValue().size());

        Assertions.assertEquals(1, argument.getValue().get(0).getFirst().size());
        Assertions.assertEquals("test", ((ObjectToken) argument.getValue().get(0).getFirst().get(0)).getName());
        Assertions.assertEquals("value", argument.getValue().get(0).getSecond().getValue());

        Assertions.assertEquals(2, argument.getValue().get(1).getFirst().size());
        Assertions.assertEquals("db", ((ObjectToken) argument.getValue().get(1).getFirst().get(0)).getName());
        Assertions.assertEquals("name", ((ObjectToken) argument.getValue().get(1).getFirst().get(1)).getName());
        Assertions.assertEquals("redis", argument.getValue().get(1).getSecond().getValue());

        List<ConfigNodeContainer> result = validateOfResults.results();
        // validate we got back the expected result. Not a of value testing this as it is only a mock.
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
        Mockito.when(parser.parse(anyList(), eq(false))).thenReturn(ValidateOf.valid(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(ValidateOf.valid(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(ValidateOf.inValid(
                Arrays.asList(new ValidationError.FailedToTokenizeElement("name", "db.name"),
                    new ValidationError.EmptyPath())));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenReturn(inputStream);
        Mockito.when(source.name()).thenReturn("mock");

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        ValidateOf<List<ConfigNodeContainer>> validateOfResults = propsLoader.loadSource(source);
        Assertions.assertTrue("empty path provided".equals(validateOfResults.getErrors().get(0).description()) ||
            "Unable to tokenize element name for path: db.name".equals(validateOfResults.getErrors().get(0).description()));
        Assertions.assertTrue("empty path provided".equals(validateOfResults.getErrors().get(1).description()) ||
            "Unable to tokenize element name for path: db.name".equals(validateOfResults.getErrors().get(1).description()));


        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(any(), eq(false));
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
        Mockito.when(parser.parse(anyList(), eq(false))).thenReturn(ValidateOf.valid(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(ValidateOf.inValid(new ValidationError.EmptyPath()));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(ValidateOf.valid(Arrays.asList(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasStream()).thenReturn(true);
        Mockito.when(source.loadStream()).thenReturn(inputStream);

        // create our class to be tested
        PropertyLoader propsLoader = new PropertyLoader(lexer, parser);

        // run the code under test.
        ValidateOf<List<ConfigNodeContainer>> validateOfResults = propsLoader.loadSource(source);
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        //setup the argument captor
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<List<Token>, ConfigValue>>> argument = ArgumentCaptor.forClass(List.class);

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(argument.capture(), eq(false));

        // validate the parser was sent the correct arguments.
        Assertions.assertEquals(1, argument.getValue().size());

        Assertions.assertEquals(2, argument.getValue().get(0).getFirst().size());
        Assertions.assertEquals("db", ((ObjectToken) argument.getValue().get(0).getFirst().get(0)).getName());
        Assertions.assertEquals("name", ((ObjectToken) argument.getValue().get(0).getFirst().get(1)).getName());
        Assertions.assertEquals("redis", argument.getValue().get(0).getSecond().getValue());

        List<ConfigNodeContainer> result = validateOfResults.results();
        // validate we got back the expected result. Not a of value testing this as it is only a mock.
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(node, result.get(0).getConfigNode());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("empty path provided", validateOfResults.getErrors().get(0).description());
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
            propsLoader.loadSource(source);
            Assertions.fail("should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("Config source: mock does not have a stream to load.", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any(), eq(false));

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
            propsLoader.loadSource(source);
            Assertions.fail("should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("bad stream", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any(), eq(false));
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
            propsLoader.loadSource(source);
            Assertions.fail("should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("Exception loading source: mock", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any(), eq(false));
    }
}
