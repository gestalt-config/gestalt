package org.github.gestalt.config.post.process.transform.substitution;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Substitution tree tests.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class SubstitutionTreeBuilderTest {

    @Test
    public void noSubstitution() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "test");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(1, result.results().size());
        Assertions.assertEquals("test", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
    }

    @Test
    public void emptySubstitution() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(1, result.results().size());
        Assertions.assertEquals("", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
    }

    @Test
    public void oneSubstitution() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello ${name} welcome to ${location}.");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(5, result.results().size());

        Assertions.assertEquals("hello ", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("name",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(" welcome to ", ((SubstitutionNode.TextNode) result.results().get(2)).getText());

        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().size());
        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(".", ((SubstitutionNode.TextNode) result.results().get(4)).getText());
    }

    @Test
    public void onlySubstitution() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "${name}");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(1, result.results().size());

        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(0)).getSubNodes().size());
        Assertions.assertEquals("name",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(0)).getSubNodes().get(0)).getText());
    }

    @Test
    public void backToBackSubstitution() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "${name}${location}");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(2, result.results().size());

        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(0)).getSubNodes().size());
        Assertions.assertEquals("name",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(0)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

    }

    @Test
    public void nestedSubstitution() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello ${name.${location}}");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(2, result.results().size());

        Assertions.assertEquals("hello ", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
        Assertions.assertEquals(2, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("name.",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(1,
            ((SubstitutionNode.TransformNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(1))
                .getSubNodes().size());

        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) ((SubstitutionNode.TransformNode) result.results().get(1))
                .getSubNodes().get(1)).getSubNodes().get(0)).getText());
    }

    @Test
    public void nestedMultipleSubstitution() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello ${name.${location}} today is ${weather}");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(4, result.results().size());

        Assertions.assertEquals("hello ", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
        Assertions.assertEquals(2, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("name.",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(1,
            ((SubstitutionNode.TransformNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(1))
                .getSubNodes().size());

        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) ((SubstitutionNode.TransformNode) result.results().get(1))
                .getSubNodes().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(" today is ", ((SubstitutionNode.TextNode) result.results().get(2)).getText());

        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().size());
        Assertions.assertEquals("weather",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().get(0)).getText());
    }

    @Test
    public void nestedSubstitutionEscaped() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello \\${name.${location}\\}");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(3, result.results().size());

        Assertions.assertEquals("hello ${name.", ((SubstitutionNode.TextNode) result.results().get(0)).getText());

        Assertions.assertEquals(1,
            ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());

        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals("}", ((SubstitutionNode.TextNode) result.results().get(2)).getText());
    }

    @Test
    public void nestedSubstitutionEscaped2() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello \\${name.${location\\}}");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(2, result.results().size());

        Assertions.assertEquals("hello ${name.", ((SubstitutionNode.TextNode) result.results().get(0)).getText());

        Assertions.assertEquals(1,
            ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());

        Assertions.assertEquals("location}",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());
    }

    @Test
    public void unexpectedClosingTokenAtMiddle() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello ${name} welcome } to ${location}.");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.DEBUG, result.getErrors().get(0).level());
        Assertions.assertEquals("Unexpected closing token: } found in string: hello ${name} welcome } to ${location}., " +
            "at location: 22 on path: db.host", result.getErrors().get(0).description());

        Assertions.assertEquals(5, result.results().size());

        Assertions.assertEquals("hello ", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("name",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(" welcome } to ", ((SubstitutionNode.TextNode) result.results().get(2)).getText());

        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().size());
        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(".", ((SubstitutionNode.TextNode) result.results().get(4)).getText());
    }

    @Test
    public void unexpectedClosingTokenAtBeginning() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "}hello ${name} welcome to ${location}.");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.DEBUG, result.getErrors().get(0).level());
        Assertions.assertEquals("Unexpected closing token: } found in string: }hello ${name} welcome to ${location}., " +
            "at location: 0 on path: db.host", result.getErrors().get(0).description());

        Assertions.assertEquals(5, result.results().size());

        Assertions.assertEquals("}hello ", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("name",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(" welcome to ", ((SubstitutionNode.TextNode) result.results().get(2)).getText());

        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().size());
        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(".", ((SubstitutionNode.TextNode) result.results().get(4)).getText());
    }

    @Test
    public void unexpectedClosingTokenAtEnding() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello ${name} welcome to ${location}.}");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.DEBUG, result.getErrors().get(0).level());
        Assertions.assertEquals("Unexpected closing token: } found in string: hello ${name} welcome to ${location}.}, " +
            "at location: 37 on path: db.host", result.getErrors().get(0).description());

        Assertions.assertEquals(5, result.results().size());

        Assertions.assertEquals("hello ", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("name",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(" welcome to ", ((SubstitutionNode.TextNode) result.results().get(2)).getText());

        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().size());
        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(".}", ((SubstitutionNode.TextNode) result.results().get(4)).getText());
    }

    @Test
    public void unClosingTokenAtEnding() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello ${name} welcome to ${location");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Reached the end of a string hello ${name} welcome to ${location with an unclosed " +
            "substitution on path: db.host", result.getErrors().get(0).description());

        Assertions.assertEquals(4, result.results().size());

        Assertions.assertEquals("hello ", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("name",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(" welcome to ", ((SubstitutionNode.TextNode) result.results().get(2)).getText());

        Assertions.assertEquals(1, ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().size());
        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(3)).getSubNodes().get(0)).getText());

    }

    @Test
    public void unClosingTokenAtBeginning() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello ${name welcome to ${location");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Reached the end of a string hello ${name welcome to ${location with an unclosed substitution " +
            "on path: db.host", result.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(1).level());
        Assertions.assertEquals("Reached the end of a string hello ${name welcome to ${location with an unclosed substitution " +
            "on path: db.host", result.getErrors().get(1).description());

        Assertions.assertEquals(2, result.results().size());
    }

    @Test
    public void unClosingTokenAtBeginning2() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("${", "}");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello ${name welcome to ${location}");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Reached the end of a string hello ${name welcome to ${location} with an unclosed substitution " +
            "on path: db.host", result.getErrors().get(0).description());

        Assertions.assertEquals(2, result.results().size());

        Assertions.assertEquals("hello ", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
        Assertions.assertEquals(2, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("name welcome to ",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode)
                result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(1,
            ((SubstitutionNode.TransformNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(1))
                .getSubNodes().size());
        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) ((SubstitutionNode.TransformNode)
                result.results().get(1)).getSubNodes().get(1)).getSubNodes().get(0)).getText());

    }

    @Test
    public void nestedSubstitutionCustomTokens() {
        SubstitutionTreeBuilder builder = new SubstitutionTreeBuilder("aaa", "ooo");

        GResultOf<List<SubstitutionNode>> result = builder.build("db.host", "hello aaaname.aaalocationoooooo");
        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals(2, result.results().size());

        Assertions.assertEquals("hello ", ((SubstitutionNode.TextNode) result.results().get(0)).getText());
        Assertions.assertEquals(2, ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().size());
        Assertions.assertEquals("name.",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(0)).getText());

        Assertions.assertEquals(1,
            ((SubstitutionNode.TransformNode) ((SubstitutionNode.TransformNode) result.results().get(1)).getSubNodes().get(1))
                .getSubNodes().size());

        Assertions.assertEquals("location",
            ((SubstitutionNode.TextNode) ((SubstitutionNode.TransformNode) ((SubstitutionNode.TransformNode) result.results().get(1))
                .getSubNodes().get(1)).getSubNodes().get(0)).getText());
    }
}
