package org.github.gestalt.config.path.mapper;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class CamelCasePathMapperTest {

    @Test
    void map() {
        CamelCasePathMapper mapper = new CamelCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path","helloWorld", new PathLexer());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(2, results.results().size());
        Assertions.assertEquals(ObjectToken.class, results.results().get(0).getClass());
        Assertions.assertEquals("hello", ((ObjectToken) results.results().get(0)).getName());
        Assertions.assertEquals(ObjectToken.class, results.results().get(1).getClass());
        Assertions.assertEquals("world", ((ObjectToken) results.results().get(1)).getName());
    }

    @Test
    void mapSingle() {
        CamelCasePathMapper mapper = new CamelCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path","hello", new PathLexer());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(1, results.results().size());
        Assertions.assertEquals(ObjectToken.class, results.results().get(0).getClass());
        Assertions.assertEquals("hello", ((ObjectToken) results.results().get(0)).getName());
    }

    @Test
    void mapCapital() {
        CamelCasePathMapper mapper = new CamelCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path","HelloWorld", new PathLexer());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(2, results.results().size());
        Assertions.assertEquals(ObjectToken.class, results.results().get(0).getClass());
        Assertions.assertEquals("hello", ((ObjectToken) results.results().get(0)).getName());
        Assertions.assertEquals(ObjectToken.class, results.results().get(1).getClass());
        Assertions.assertEquals("world", ((ObjectToken) results.results().get(1)).getName());
    }
    @Test
    void mapNumber() {
        CamelCasePathMapper mapper = new CamelCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path","hello9World", new PathLexer());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(2, results.results().size());
        Assertions.assertEquals(ObjectToken.class, results.results().get(0).getClass());
        Assertions.assertEquals("hello9", ((ObjectToken) results.results().get(0)).getName());
        Assertions.assertEquals(ObjectToken.class, results.results().get(1).getClass());
        Assertions.assertEquals("world", ((ObjectToken) results.results().get(1)).getName());
    }

    @Test
    void mapMULTICapital() {
        CamelCasePathMapper mapper = new CamelCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path","HelloWORLDTour", new PathLexer());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(3, results.results().size());
        Assertions.assertEquals(ObjectToken.class, results.results().get(0).getClass());
        Assertions.assertEquals("hello", ((ObjectToken) results.results().get(0)).getName());
        Assertions.assertEquals(ObjectToken.class, results.results().get(1).getClass());
        Assertions.assertEquals("world", ((ObjectToken) results.results().get(1)).getName());
        Assertions.assertEquals(ObjectToken.class, results.results().get(2).getClass());
        Assertions.assertEquals("tour", ((ObjectToken) results.results().get(2)).getName());
    }

    @Test
    void mapLexingError() {
        SentenceLexer mockLexer = Mockito.mock(PathLexer.class);

        Mockito.when(mockLexer.scan("hello")).thenReturn(ValidateOf.valid(List.of(new ObjectToken("hello"))));
        Mockito.when(mockLexer.scan("World"))
               .thenReturn(ValidateOf.inValid(new ValidationError.FailedToTokenizeElement("World", "my.path")));
        CamelCasePathMapper mapper = new CamelCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path","helloWorld", mockLexer);

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element World for path: my.path", results.getErrors().get(0).description());
    }

    @Test
    void mapLexingNoResults() {
        SentenceLexer mockLexer = Mockito.mock(PathLexer.class);

        Mockito.when(mockLexer.scan("hello")).thenReturn(ValidateOf.valid(List.of(new ObjectToken("hello"))));
        Mockito.when(mockLexer.scan("World")).thenReturn(ValidateOf.valid(null));
        CamelCasePathMapper mapper = new CamelCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path","helloWorld", mockLexer);

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: my.path, for class: MapNode, during decoding", results.getErrors().get(0).description());
    }
}
