package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

class EnvironmentLoaderTest {

    @Test
    void accepts() {
        EnvironmentVarsLoader envVarsLoader = new EnvironmentVarsLoader();
        Assertions.assertFalse(envVarsLoader.accepts("properties"));
        Assertions.assertTrue(envVarsLoader.accepts("envVars"));
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

        // setup the input data, instead of loading from a file.
        List<Pair<String, String>> data = new ArrayList<>();
        data.add(new Pair<>("test", "value"));
        data.add(new Pair<>("db.name", "redis"));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(anyList())).thenReturn(ValidateOf.valid(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(ValidateOf.valid(Collections.singletonList(new ObjectToken("test"))));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(ValidateOf.valid(Arrays.asList(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenReturn(data);

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(false, lexer, parser);

        // run the code under test.
        ValidateOf<ConfigNode> validateOfResults = environmentVarsLoader.loadSource(source);
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

        // setup the input data, instead of loading from a file.
        List<Pair<String, String>> data = new ArrayList<>();
        data.add(new Pair<>("test", "value"));
        data.add(new Pair<>("db.name", "redis"));

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
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenReturn(data);
        Mockito.when(source.name()).thenReturn("mock");

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(false, lexer, parser);

        // run the code under test.
        try {
            environmentVarsLoader.loadSource(source);
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

        // setup the input data, instead of loading from a file.
        List<Pair<String, String>> data = new ArrayList<>();
        data.add(new Pair<>("test", "value"));
        data.add(new Pair<>("db.name", "redis"));

        // create the result expected
        ConfigNode node = new LeafNode("test");

        // mock the interactions with the parser and lexer
        Mockito.when(parser.parse(anyList())).thenReturn(ValidateOf.valid(node));
        Mockito.when(lexer.scan("test"))
            .thenReturn(ValidateOf.inValid(new ValidationError.EmptyPath()));
        Mockito.when(lexer.scan("db.name"))
            .thenReturn(ValidateOf.valid(Arrays.asList(new ObjectToken("db"), new ObjectToken("name"))));

        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(true);
        Mockito.when(source.loadList()).thenReturn(data);

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(false, lexer, parser);

        // run the code under test.
        ValidateOf<ConfigNode> validateOfResults = environmentVarsLoader.loadSource(source);
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
    void loadSourceMockDependenciesNoList() {

        // setup the mocks
        SentenceLexer lexer = Mockito.mock(SentenceLexer.class);
        ConfigParser parser = Mockito.mock(ConfigParser.class);
        ConfigSource source = Mockito.mock(ConfigSource.class);

        // mock the source so we return our test data stream.
        Mockito.when(source.hasList()).thenReturn(false);
        Mockito.when(source.name()).thenReturn("mock");

        // create our class to be tested
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(false, lexer, parser);

        // run the code under test.
        try {
            environmentVarsLoader.loadSource(source);
            Assertions.assertTrue(true, "should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("Config source: mock does not have a list to load.", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any());

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
        EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader(false, lexer, parser);

        // run the code under test.
        try {
            environmentVarsLoader.loadSource(source);
            Assertions.assertTrue(true, "should not hit this");
        } catch (GestaltException e) {
            Assertions.assertEquals("bad stream", e.getMessage());
        }

        // verify we get the correct number of calls and capture the parsers arguments.
        Mockito.verify(lexer, Mockito.times(0)).scan(anyString());
        Mockito.verify(parser, Mockito.times(0)).parse(any());
    }

}
