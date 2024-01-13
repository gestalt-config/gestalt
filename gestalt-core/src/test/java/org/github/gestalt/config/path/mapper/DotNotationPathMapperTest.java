package org.github.gestalt.config.path.mapper;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.NoResultSentenceLexer;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.github.gestalt.config.entity.ValidationLevel.ERROR;
import static org.github.gestalt.config.entity.ValidationLevel.MISSING_VALUE;

class DotNotationPathMapperTest {

    @Test
    void map() {
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "helloWorld", new PathLexer());

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
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "hello", new PathLexer());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(1, results.results().size());
        Assertions.assertEquals(ObjectToken.class, results.results().get(0).getClass());
        Assertions.assertEquals("hello", ((ObjectToken) results.results().get(0)).getName());
    }

    @Test
    void mapCapital() {
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "HelloWorld", new PathLexer());

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
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "hello9World", new PathLexer());

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
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "HelloWORLDTour", new PathLexer());

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

        Mockito.when(mockLexer.scan("hello")).thenReturn(GResultOf.result(List.of(new ObjectToken("hello"))));
        Mockito.when(mockLexer.scan("World"))
            .thenReturn(GResultOf.errors(new ValidationError.FailedToTokenizeElement("World", "my.path")));
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "helloWorld", mockLexer);

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element World for path: my.path", results.getErrors().get(0).description());
    }

    @Test
    void mapLexingNoResults() {
        SentenceLexer mockLexer = Mockito.mock(PathLexer.class);

        Mockito.when(mockLexer.scan("hello")).thenReturn(GResultOf.result(List.of(new ObjectToken("hello"))));
        Mockito.when(mockLexer.scan("World")).thenReturn(GResultOf.result(null));
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "helloWorld", mockLexer);

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("No results from mapping path: my.path, with next path: helloWorld, during dot notation path mapping",
            results.getErrors().get(0).description());
    }

    @Test
    void mapEmpty() {
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "", new PathLexer());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Mapper: DotNotationPathMapper token was null or empty on path: my.path",
            results.getErrors().get(0).description());
    }

    @Test
    void mapNull() {
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", null, new PathLexer());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Mapper: DotNotationPathMapper token was null or empty on path: my.path",
            results.getErrors().get(0).description());
    }

    @Test
    void mapLexError() {
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "???", new PathLexer());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Unable to tokenize element ??? for path: ???",
            results.getErrors().get(0).description());
    }

    @Test
    void mapLexEmpty() {
        DotNotationPathMapper mapper = new DotNotationPathMapper();
        GResultOf<List<Token>> results = mapper.map("my.path", "???", new NoResultSentenceLexer());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(MISSING_VALUE, results.getErrors().get(0).level());
        Assertions.assertEquals("No results from mapping path: my.path, with next path: ???, during dot notation path mapping",
            results.getErrors().get(0).description());
    }
}
