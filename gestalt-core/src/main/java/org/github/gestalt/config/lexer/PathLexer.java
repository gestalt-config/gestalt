package org.github.gestalt.config.lexer;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of a SentenceLexer that divides a sentence into words using a provided delimiter. The delimiter is a regex.
 * Then parses the words into tokens.
 *
 * <p>By default it tokenizes based on a "." then evaluates each word with a provided regex.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class PathLexer extends SentenceLexer {

    /**
     * default pattern to represent a path.
     * Should allow most characters, even the delimiter. Although the string will be split with the delimiter so will not be seen
     */
    public static final String DEFAULT_EVALUATOR = "^((?<name>[\\w .,+=\\-;:\"'`~!@#$%^&*()\\<>]+)(?<array>\\[(?<index>\\d*)])?)$";
    public static final String DELIMITER_DEFAULT = ".";
    private final Pattern pathPattern;
    private final String normalizedDelimiter;
    private final String normalizedArrayOpenTag;
    private final String normalizedArrayCloseTag;
    private final String normalizedMapTag;
    private final String delimiter;
    private final String delimiterRegex;
    private final SentenceNormalizer sentenceNormalizer;

    /**
     * Build a path lexer to tokenize a path.
     */
    public PathLexer() {
        this(DELIMITER_DEFAULT, DELIMITER_DEFAULT, DEFAULT_EVALUATOR, new LowerCaseSentenceNormalizer(), "[", "]", "=");
    }

    /**
     * construct a Path lexer, remember that the delimiter is a regex, so if you want to use . you need to escape it. ".".
     *
     * @param delimiter the character to split the sentence
     */
    public PathLexer(String delimiter) {
        this(delimiter, delimiter, DEFAULT_EVALUATOR, new LowerCaseSentenceNormalizer(), "[", "]", "=");
    }

    /**
     * construct a Path lexer, remember that the delimiter is a regex, so if you want to use . you need to escape it. "."
     *
     * @param delimiter        the character to split the sentence
     * @param pathPatternRegex a regex with capture groups to decide what kind of token this is. The regex should have a capture group
     *                         name = name of the element
     *                         array = if this element is an array
     *                         index = the index for the array
     */
    public PathLexer(String delimiter, String pathPatternRegex) {
        this(delimiter, delimiter, pathPatternRegex, new LowerCaseSentenceNormalizer(), "[", "]", "=");
    }

    /**
     * construct a Path lexer, remember that the delimiter is a regex, so if you want to use . you need to escape it. "."
     *
     * @param delimiter          the character to split the sentence
     * @param pathPatternRegex   a regex with capture groups to decide what kind of token this is. The regex should have a capture group
     *                           name = name of the element
     *                           array = if this element is an array
     *                           index = the index for the array
     * @param sentenceNormalizer defines how to normalize a sentence.
     */
    public PathLexer(String delimiter, String pathPatternRegex, SentenceNormalizer sentenceNormalizer) {
        this(delimiter, delimiter, pathPatternRegex, sentenceNormalizer, "[", "]", "=");
    }

    /**
     * construct a Path lexer, remember that the delimiter is a regex, so if you want to use . you need to escape it. "."
     *
     * @param normalizedDelimiter how we want to represent the path when we rebuild it from the config tree.
     * @param delimiter           the character to split the sentence
     * @param pathPatternRegex    a regex with capture groups to decide what kind of token this is. The regex should have a capture group
     *                            name = name of the element
     *                            array = if this element is an array
     *                            index = the index for the array
     * @param sentenceNormalizer  defines how to normalize a sentence.
     */
    public PathLexer(String normalizedDelimiter, String delimiter, String pathPatternRegex, SentenceNormalizer sentenceNormalizer) {
        this(normalizedDelimiter, delimiter, pathPatternRegex, sentenceNormalizer, "[", "]", "=");
    }

    public PathLexer(String normalizedDelimiter, String delimiter, String pathPatternRegex, SentenceNormalizer sentenceNormalizer,
                     String normalizedArrayOpenTag, String normalizedArrayCloseTag, String normalizedMapTag) {
        this.pathPattern = Pattern.compile(pathPatternRegex, Pattern.CASE_INSENSITIVE);
        this.normalizedDelimiter = normalizedDelimiter;
        this.delimiter = delimiter;
        if (delimiter.length() == 1) {
            this.delimiterRegex = Pattern.quote(delimiter);
        } else {
            this.delimiterRegex = delimiter;
        }
        this.sentenceNormalizer = sentenceNormalizer;
        this.normalizedArrayOpenTag = normalizedArrayOpenTag;
        this.normalizedArrayCloseTag = normalizedArrayCloseTag;
        this.normalizedMapTag = normalizedMapTag;
    }

    @Override
    public String getNormalizedDeliminator() {
        return normalizedDelimiter;
    }

    @Override
    public String getNormalizedArrayOpenTag() {
        return normalizedArrayOpenTag;
    }

    @Override
    public String getNormalizedArrayCloseTag() {
        return normalizedArrayCloseTag;
    }

    @Override
    public String getNormalizedMapTag() {
        return normalizedMapTag;
    }

    @Override
    public String getDeliminator() {
        return delimiter;
    }

    @Override
    protected List<String> tokenizer(String sentence) {
        return sentence != null && !sentence.isEmpty() ? List.of(sentence.split(delimiterRegex)) : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected GResultOf<List<Token>> evaluator(String word, String sentence) {
        if (sentence == null || sentence.isEmpty()) {
            return GResultOf.errors(new ValidationError.EmptyPath());
        } else if (word == null || word.isEmpty()) {
            return GResultOf.errors(new ValidationError.EmptyElement(sentence));
        }

        Matcher matcher = pathPattern.matcher(word);
        if (!matcher.find()) {
            return GResultOf.errors(new ValidationError.FailedToTokenizeElement(word, sentence));
        }

        String name = matcher.group("name");
        if (name == null || name.isEmpty()) {
            return GResultOf.errors(new ValidationError.UnableToParseName(sentence));
        }

        String array = matcher.group("array");
        String arrayIndex = matcher.group("index");

        @SuppressWarnings("rawtypes") GResultOf results;
        if (array != null && arrayIndex != null && !arrayIndex.isEmpty()) {
            if (StringUtils.isInteger(arrayIndex)) {
                int index = Integer.parseInt(arrayIndex);
                if (index >= 0) {
                    results = GResultOf.result(List.of(new ObjectToken(name), new ArrayToken(index)));
                } else {
                    results = GResultOf.errors(new ValidationError.InvalidArrayNegativeIndexToken(word, index, sentence));
                }
            } else {
                results = GResultOf.errors(new ValidationError.InvalidArrayToken(word, arrayIndex, sentence));
            }
        } else if (array != null) {
            results = GResultOf.errors(new ValidationError.InvalidArrayIndexToken(word, sentence));
        } else {
            ObjectToken object = new ObjectToken(name);
            List<ObjectToken> list = Collections.singletonList(object);
            results = GResultOf.result(list);
        }

        return results;
    }

    @Override
    public String normalizeSentence(String sentence) {
        return sentenceNormalizer.normalizeSentence(sentence);
    }
}
