package org.config.gestalt.loader;

import org.config.gestalt.entity.ConfigValue;
import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.parser.ConfigParser;
import org.config.gestalt.source.ConfigSource;
import org.config.gestalt.token.ObjectToken;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.Pair;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
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

        //setup the argument captor
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<List<Token>, ConfigValue>>> argument = ArgumentCaptor.forClass(List.class);

        // setup the input stream, instead of loading from a file.
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test = value\ndb.name = redis".getBytes(UTF_8));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(anyList())).thenReturn(ValidateOf.valid(node));
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
        ValidateOf<ConfigNode> validateOfResults = propsLoader.loadSource(source);
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        ConfigNode result = validateOfResults.results();

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(argument.capture());

        // validate the parser was sent the correct arguments.
        Assertions.assertEquals(2, argument.getValue().size());

        Assertions.assertEquals(1, argument.getValue().get(0).getFirst().size());
        Assertions.assertEquals("test", ((ObjectToken) argument.getValue().get(0).getFirst().get(0)).getName());
        Assertions.assertEquals("value", argument.getValue().get(0).getSecond().getValue());

        Assertions.assertEquals(2, argument.getValue().get(1).getFirst().size());
        Assertions.assertEquals("db", ((ObjectToken) argument.getValue().get(1).getFirst().get(0)).getName());
        Assertions.assertEquals("name", ((ObjectToken) argument.getValue().get(1).getFirst().get(1)).getName());
        Assertions.assertEquals("redis", argument.getValue().get(1).getSecond().getValue());

        // validate we got back the expected result. Not a of value testing this as it is only a mock.
        Assertions.assertEquals(node, result);
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
        Mockito.when(parser.parse(anyList())).thenReturn(ValidateOf.valid(node));
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
        try {
            propsLoader.loadSource(source);
            Assertions.assertTrue(true);
        } catch (GestaltException e) {
            assertThat(e.getMessage())
                .contains("Exception loading config source mock")
                .contains("level: WARN, message: empty path provided")
                .contains("level: ERROR, message: Unable to tokenize element name for path: db.name");
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any());
    }

    @Test
    void loadSourceMockDependenciesValidationWarn() throws GestaltException {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        //setup the argument captor
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<List<Token>, ConfigValue>>> argument = ArgumentCaptor.forClass(List.class);

        // setup the input stream, instead of loading from a file.
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test = value\ndb.name = redis".getBytes(UTF_8));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(anyList())).thenReturn(ValidateOf.valid(node));
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
        ValidateOf<ConfigNode> validateOfResults = propsLoader.loadSource(source);
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());
        ConfigNode result = validateOfResults.results();

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(2)).scan(anyString());
        Mockito.verify(parser, Mockito.times(1)).parse(argument.capture());

        // validate the parser was sent the correct arguments.
        Assertions.assertEquals(1, argument.getValue().size());

        Assertions.assertEquals(2, argument.getValue().get(0).getFirst().size());
        Assertions.assertEquals("db", ((ObjectToken) argument.getValue().get(0).getFirst().get(0)).getName());
        Assertions.assertEquals("name", ((ObjectToken) argument.getValue().get(0).getFirst().get(1)).getName());
        Assertions.assertEquals("redis", argument.getValue().get(0).getSecond().getValue());

        // validate we got back the expected result. Not a of value testing this as it is only a mock.
        Assertions.assertEquals(node, result);
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
            Assertions.assertTrue(true, "should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("Config source: mock does not have a stream to load.", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any());

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
            Assertions.assertTrue(true, "should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("bad stream", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any());
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
            Assertions.assertTrue(true, "should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("Exception loading source: mock", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any());
    }
}
