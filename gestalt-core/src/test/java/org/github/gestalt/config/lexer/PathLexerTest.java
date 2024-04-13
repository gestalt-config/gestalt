package org.github.gestalt.config.lexer;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.github.gestalt.config.lexer.PathLexer.DEFAULT_EVALUATOR;

class PathLexerTest {

    @Test
    public void testTokenizer() {
        PathLexer pathLexer = new PathLexer();

        List<String> result = pathLexer.tokenizer("the.quick[].brown.fox");

        Assertions.assertEquals(4, result.size());
        Assertions.assertEquals("the", result.get(0));
        Assertions.assertEquals("quick[]", result.get(1));
        Assertions.assertEquals("brown", result.get(2));
        Assertions.assertEquals("fox", result.get(3));
    }

    @Test
    public void testTokenizerAlternate() {
        PathLexer pathLexer = new PathLexer("_");

        List<String> result = pathLexer.tokenizer("the_quick[]_brown_fox");

        Assertions.assertEquals(4, result.size());
        Assertions.assertEquals("the", result.get(0));
        Assertions.assertEquals("quick[]", result.get(1));
        Assertions.assertEquals("brown", result.get(2));
        Assertions.assertEquals("fox", result.get(3));
    }

    @Test
    public void testEmptyTokenizer() {
        PathLexer pathLexer = new PathLexer(".");

        List<String> result = pathLexer.tokenizer("");
        Assertions.assertEquals(0, result.size());

        result = pathLexer.tokenizer(null);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testEvaluatorEmptyWord() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.evaluator(null, "the.quick[].brown.fox");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());

        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals(ValidationLevel.WARN, errors.get(0).level());
        Assertions.assertEquals("empty element for path: the.quick[].brown.fox", errors.get(0).description());

        result.getErrors(ValidationLevel.WARN);
        Assertions.assertEquals(ValidationLevel.WARN, errors.get(0).level());
        Assertions.assertEquals("empty element for path: the.quick[].brown.fox", errors.get(0).description());

        result = pathLexer.evaluator(null, "the.quick[0].brown.fox");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());

        errors = result.getErrors();
        Assertions.assertEquals("empty element for path: the.quick[0].brown.fox", errors.get(0).description());
    }

    @Test
    public void testEvaluatorEmptySentence() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.evaluator("the", null);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());

        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals("empty path provided", errors.get(0).description());

        result = pathLexer.evaluator("the", "");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());

        errors = result.getErrors();
        Assertions.assertEquals("empty path provided", errors.get(0).description());
    }

    @Test
    public void testEvaluatorObject() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.evaluator("the", "the.quick[].brown.fox");

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        List<Token> tokens = result.results();
        Assertions.assertEquals(1, tokens.size());
        Assertions.assertEquals(ObjectToken.class, tokens.get(0).getClass());
        Assertions.assertEquals("the", ((ObjectToken) tokens.get(0)).getName());
    }

    @Test
    public void testEvaluatorArrayNoIndex() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.evaluator("quick[0]", "the.quick[0].brown.fox");

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        List<Token> tokens = result.results();
        Assertions.assertEquals(2, tokens.size());
        Assertions.assertEquals(ObjectToken.class, tokens.get(0).getClass());
        Assertions.assertEquals("quick", ((ObjectToken) tokens.get(0)).getName());
        Assertions.assertEquals(ArrayToken.class, tokens.get(1).getClass());
        Assertions.assertEquals(0, ((ArrayToken) tokens.get(1)).getIndex());
    }

    @Test
    public void testEvaluatorArrayIndex() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.evaluator("quick[2]", "the.quick[2].brown.fox");

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        List<Token> tokens = result.results();
        Assertions.assertEquals(2, tokens.size());
        Assertions.assertEquals(ObjectToken.class, tokens.get(0).getClass());
        Assertions.assertEquals("quick", ((ObjectToken) tokens.get(0)).getName());
        Assertions.assertEquals(ArrayToken.class, tokens.get(1).getClass());
        Assertions.assertEquals(2, ((ArrayToken) tokens.get(1)).getIndex());
    }

    @Test
    public void testEvaluatorNegativeArrayIndex() {
        PathLexer pathLexer = new PathLexer(PathLexer.DELIMITER_DEFAULT, "^((?<name>\\w+)(?<array>\\[(?<index>-\\d*)])?)$");

        GResultOf<List<Token>> result = pathLexer.evaluator("quick[-2]", "the.quick[-2].brown.fox");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("Array index can not be negative: -2 provided provided for element: quick[-2] " +
            "for path: the.quick[-2].brown.fox", result.getErrors().get(0).description());
    }

    @Test
    public void testEvaluatorBadWord() {
        PathLexer pathLexer = new PathLexer(PathLexer.DELIMITER_DEFAULT, "^((?<name>\\w+)(?<array>\\[(?<index>\\d*)])?)$");

        GResultOf<List<Token>> result = pathLexer.evaluator("$%#@%", "the.$%#@%.brown.fox");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());

        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals("Unable to tokenize element $%#@% for path: the.$%#@%.brown.fox", errors.get(0).description());
    }

    @Test
    public void testEvaluatorNoName() {
        // we can only test a bad name if we provide a bad regex, otherwise it wont match.
        PathLexer pathLexer = new PathLexer(PathLexer.DELIMITER_DEFAULT, "((?<name>\\w?)(?<array>\\[(?<index>\\d*)])?)$");

        GResultOf<List<Token>> result = pathLexer.evaluator("[0]", "the.quick[0].brown.fox");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());

        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals("unable to parse the name for path: the.quick[0].brown.fox", errors.get(0).description());
    }

    @Test
    public void testEvaluatorBadArrayIndex() {
        // we can only test a bad array index if we provide a bad regex.
        PathLexer pathLexer = new PathLexer(PathLexer.DELIMITER_DEFAULT, "^((?<name>\\w+)(?<array>\\[(?<index>\\w*)])?)$");

        GResultOf<List<Token>> result = pathLexer.evaluator("quick[a]", "the.quick[a].brown.fox");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());

        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals("Array index provided: a for element quick[a] but unable to parse as int for path: " +
            "the.quick[a].brown.fox", errors.get(0).description());
    }

    @Test
    public void testEvaluatorBadArrayIndexMissing() {
        // we can only test a bad array index if we provide a bad regex.
        PathLexer pathLexer = new PathLexer(PathLexer.DELIMITER_DEFAULT, "^((?<name>\\w+)(?<array>\\[(?<index>\\w*)])?)$");

        GResultOf<List<Token>> result = pathLexer.evaluator("quick[]", "the.quick[].brown.fox");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());

        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals("Array index not provided provided for element quick[] for path: " +
            "the.quick[].brown.fox", errors.get(0).description());
    }

    @Test
    public void testDefaultTokenizer() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.scan("the.quick[0].brown.fox");

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        List<Token> tokens = result.results();
        Assertions.assertEquals(5, tokens.size());
        Assertions.assertEquals(ObjectToken.class, tokens.get(0).getClass());
        Assertions.assertEquals("the", ((ObjectToken) tokens.get(0)).getName());
        Assertions.assertEquals(ObjectToken.class, tokens.get(1).getClass());
        Assertions.assertEquals("quick", ((ObjectToken) tokens.get(1)).getName());
        Assertions.assertEquals(ArrayToken.class, tokens.get(2).getClass());
        Assertions.assertEquals(0, ((ArrayToken) tokens.get(2)).getIndex());
        Assertions.assertEquals(ObjectToken.class, tokens.get(3).getClass());
        Assertions.assertEquals("brown", ((ObjectToken) tokens.get(3)).getName());
        Assertions.assertEquals(ObjectToken.class, tokens.get(4).getClass());
        Assertions.assertEquals("fox", ((ObjectToken) tokens.get(4)).getName());
    }

    @Test
    public void testDefaultTokenizerCaseChange() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.scan("The.Quick[0].BROWN.fox");

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        List<Token> tokens = result.results();
        Assertions.assertEquals(5, tokens.size());
        Assertions.assertEquals(ObjectToken.class, tokens.get(0).getClass());
        Assertions.assertEquals("the", ((ObjectToken) tokens.get(0)).getName());
        Assertions.assertEquals(ObjectToken.class, tokens.get(1).getClass());
        Assertions.assertEquals("quick", ((ObjectToken) tokens.get(1)).getName());
        Assertions.assertEquals(ArrayToken.class, tokens.get(2).getClass());
        Assertions.assertEquals(0, ((ArrayToken) tokens.get(2)).getIndex());
        Assertions.assertEquals(ObjectToken.class, tokens.get(3).getClass());
        Assertions.assertEquals("brown", ((ObjectToken) tokens.get(3)).getName());
        Assertions.assertEquals(ObjectToken.class, tokens.get(4).getClass());
        Assertions.assertEquals("fox", ((ObjectToken) tokens.get(4)).getName());
    }

    @Test
    public void testScanNull() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.scan(null);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertFalse(result.hasResults());
        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals("empty path provided", errors.get(0).description());
    }

    @Test
    public void testScanEmptyElement() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.scan("the.quick..brown");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals("empty element for path: the.quick..brown", errors.get(0).description());

        List<Token> tokens = result.results();
        Assertions.assertEquals(3, tokens.size());
        Assertions.assertEquals(ObjectToken.class, tokens.get(0).getClass());
        Assertions.assertEquals("the", ((ObjectToken) tokens.get(0)).getName());
        Assertions.assertEquals(ObjectToken.class, tokens.get(1).getClass());
        Assertions.assertEquals("quick", ((ObjectToken) tokens.get(1)).getName());
        Assertions.assertEquals(ObjectToken.class, tokens.get(2).getClass());
        Assertions.assertEquals("brown", ((ObjectToken) tokens.get(2)).getName());
    }

    @Test
    public void testInvalidSentence() {
        PathLexer pathLexer = new PathLexer(PathLexer.DELIMITER_DEFAULT, "^((?<name>\\w+)(?<array>\\[(?<index>\\d*)])?)$");

        GResultOf<List<Token>> result = pathLexer.scan("the.@#*&");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals("Unable to tokenize element @#*& for path: the.@#*&", errors.get(0).description());

        List<Token> tokens = result.results();
        Assertions.assertEquals(1, tokens.size());
    }

    @Test
    public void testArrayNegativeIndex() {
        PathLexer pathLexer = new PathLexer();

        GResultOf<List<Token>> result = pathLexer.scan("the.test[-2]");

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        List<ValidationError> errors = result.getErrors();
        Assertions.assertEquals("Unable to tokenize element test[-2] for path: the.test[-2]", errors.get(0).description());

        List<Token> tokens = result.results();
        Assertions.assertEquals(1, tokens.size());
    }

    @Test
    public void testTokenizerFullConstructor() {
        PathLexer pathLexer = new PathLexer(".", "_", DEFAULT_EVALUATOR, new LowerCaseSentenceNormalizer());

        List<String> result = pathLexer.tokenizer("the_quick[]_brown_fox");

        Assertions.assertEquals(".", pathLexer.getNormalizedDeliminator());
        Assertions.assertEquals("_", pathLexer.getDeliminator());

        Assertions.assertEquals(4, result.size());
        Assertions.assertEquals("the", result.get(0));
        Assertions.assertEquals("quick[]", result.get(1));
        Assertions.assertEquals("brown", result.get(2));
        Assertions.assertEquals("fox", result.get(3));
    }
}
