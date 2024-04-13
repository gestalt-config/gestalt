package org.github.gestalt.config.lexer;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PathLexerBuilderTest {

    @Test
    public void testBuilder() {

        PathLexerBuilder builder = PathLexerBuilder.builder()
            .setDelimiter("*")
            .setNormalizedDelimiter("_")
            .setNormalizedArrayOpenTag("{")
            .setNormalizedArrayCloseTag("}")
            .setNormalizedMapTag(":")
            .setSentenceNormalizer(new LowerCaseSentenceNormalizer())
            .setPathPatternRegex("ABC");

        var lexer = builder.build();
        Assertions.assertEquals("*", lexer.getDeliminator());
        Assertions.assertEquals("_", lexer.getNormalizedDeliminator());
        Assertions.assertEquals("{", lexer.getNormalizedArrayOpenTag());
        Assertions.assertEquals("}", lexer.getNormalizedArrayCloseTag());
        Assertions.assertEquals(":", lexer.getNormalizedMapTag());
    }
}
