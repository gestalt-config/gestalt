package org.github.gestalt.config.lexer;

/**
 * Builder for the path lexer.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class PathLexerBuilder {
    private String normalizedDelimiter;
    private String normalizedArrayOpenTag;
    private String normalizedArrayCloseTag;
    private String normalizedMapTag;
    private String delimiter;
    private SentenceNormalizer sentenceNormalizer;
    private String pathPatternRegex;

    private PathLexerBuilder() {
    }

    public static PathLexerBuilder builder() {
        return new PathLexerBuilder();
    }

    /**
     * the deliminator that we use to represent a normalized path.
     * Ie how we want to display a path being rebuilt from the config tree.
     *
     * @param normalizedDelimiter deliminator that we use to represent a normalized path.
     * @return the builder
     */
    public PathLexerBuilder setNormalizedDelimiter(String normalizedDelimiter) {
        this.normalizedDelimiter = normalizedDelimiter;
        return this;
    }

    /**
     * the deliminator that we use to represent a normalized opening tag for an array.
     *
     * @param normalizedArrayOpenTag the deliminator that we use to represent a normalized opening tag for an array.
     * @return the builder
     */
    public PathLexerBuilder setNormalizedArrayOpenTag(String normalizedArrayOpenTag) {
        this.normalizedArrayOpenTag = normalizedArrayOpenTag;
        return this;
    }

    /**
     * the deliminator that we use to represent a normalized closing tag for an array.
     *
     * @param normalizedArrayCloseTag the deliminator that we use to represent a normalized closing tag for an array.
     * @return the builder
     */
    public PathLexerBuilder setNormalizedArrayCloseTag(String normalizedArrayCloseTag) {
        this.normalizedArrayCloseTag = normalizedArrayCloseTag;
        return this;
    }

    /**
     * Return the deliminator that we use to represent a normalized map separator.
     *
     * @param normalizedMapTag Return the deliminator that we use to represent a normalized map separator.
     * @return the builder
     */
    public PathLexerBuilder setNormalizedMapTag(String normalizedMapTag) {
        this.normalizedMapTag = normalizedMapTag;
        return this;
    }

    /**
     * the character to split the sentence.
     *
     * @param delimiter the character to split the sentence
     * @return the builder
     */
    public PathLexerBuilder setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * defines how to normalize a sentence.
     *
     * @param sentenceNormalizer defines how to normalize a sentence.
     * @return the builder
     */
    public PathLexerBuilder setSentenceNormalizer(SentenceNormalizer sentenceNormalizer) {
        this.sentenceNormalizer = sentenceNormalizer;
        return this;
    }

    /**
     * a regex with capture groups to decide what kind of token this is. The regex should have a capture group.
     *                          name = name of the element
     *                          array = if this element is an array
     *                          index = the index for the array
     *                          
     * @param pathPatternRegex path regex
     * @return the builder
     */
    public PathLexerBuilder setPathPatternRegex(String pathPatternRegex) {
        this.pathPatternRegex = pathPatternRegex;
        return this;
    }

    /**
     * build the path lexer.
     *
     * @return the path lexer
     */
    public PathLexer build() {
        return new PathLexer(normalizedDelimiter, delimiter, pathPatternRegex, sentenceNormalizer,
            normalizedArrayOpenTag, normalizedArrayCloseTag, normalizedMapTag);
    }
}
