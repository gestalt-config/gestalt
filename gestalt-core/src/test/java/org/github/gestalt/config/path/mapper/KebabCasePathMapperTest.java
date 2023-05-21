package org.github.gestalt.config.path.mapper;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.github.gestalt.config.entity.ValidationLevel.ERROR;
import static org.github.gestalt.config.entity.ValidationLevel.MISSING_VALUE;

/**
 * @author Colin Redmond (c) 2023.
 */
class KebabCasePathMapperTest {

    @Test
    void map() {
        KebabCasePathMapper mapper = new KebabCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path", "helloWorld", new PathLexer());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(1, results.results().size());
        Assertions.assertEquals(ObjectToken.class, results.results().get(0).getClass());
        Assertions.assertEquals("hello-world", ((ObjectToken) results.results().get(0)).getName());
    }

    @Test
    void mapNoChange() {
        KebabCasePathMapper mapper = new KebabCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path", "hello", new PathLexer());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(1, results.results().size());
        Assertions.assertEquals(ObjectToken.class, results.results().get(0).getClass());
        Assertions.assertEquals("hello", ((ObjectToken) results.results().get(0)).getName());
    }

    @Test
    void mapMultiple() {
        KebabCasePathMapper mapper = new KebabCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path", "helloWorldHowAreYou", new PathLexer());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(1, results.results().size());
        Assertions.assertEquals(ObjectToken.class, results.results().get(0).getClass());
        Assertions.assertEquals("hello-world-how-are-you", ((ObjectToken) results.results().get(0)).getName());
    }

    @Test
    void mapEmpty() {
        KebabCasePathMapper mapper = new KebabCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path", "", new PathLexer());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(MISSING_VALUE, results.getErrors().get(0).level());
        Assertions.assertEquals("Unable to find node matching path: my.path, for class: MapNode, during Kebab case path mapping",
            results.getErrors().get(0).description());
    }

    @Test
    void mapNull() {
        KebabCasePathMapper mapper = new KebabCasePathMapper();
        ValidateOf<List<Token>> results = mapper.map("my.path", null, new PathLexer());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Mapper: KebabCasePathMapper key was null on path: my.path",
            results.getErrors().get(0).description());
    }

}
