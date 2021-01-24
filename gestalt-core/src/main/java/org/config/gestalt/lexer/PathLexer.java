package org.config.gestalt.lexer;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.token.ArrayToken;
import org.config.gestalt.token.ObjectToken;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.StringUtils;
import org.config.gestalt.utils.ValidateOf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of a SentenceLexer that divides a sentence into words using a provided delimiter. The delimiter is a regex.
 * Then parses the words into tokens.
 *
 * <p>By default it tokenizes based on a "." then evaluates each work with a provided regex.
 *
 * @author Colin Redmond
 */
public class PathLexer extends SentenceLexer {

    public static final String DEFAULT_EVALUATOR = "^((?<name>\\w+)(?<array>\\[(?<index>\\d*)])?)$";
    private final Pattern pathPattern;
    private final String delimiter;

    public PathLexer() {
        this.pathPattern = Pattern.compile(DEFAULT_EVALUATOR, Pattern.CASE_INSENSITIVE);
        this.delimiter = "\\.";
    }

    /**
     * construct a Path lexer, remember that the delimiter is a regex, so if you want to use . you need to escape it. "\\."
     *
     * @param delimiter a regex to split the path on.
     */
    public PathLexer(String delimiter) {
        this.pathPattern = Pattern.compile(DEFAULT_EVALUATOR, Pattern.CASE_INSENSITIVE);
        this.delimiter = delimiter;
    }

    /**
     * construct a Path lexer, remember that the delimiter is a regex, so if you want to use . you need to escape it. "\\."
     *
     * @param delimiter        a regex to split the path on.
     * @param pathPatternRegex a regex with capture groups to decide what kind of token this is. The regex should have a capture group
     *                         name = name of the element
     *                         array = if this element is an array
     *                         index = the index for the array
     */
    public PathLexer(String delimiter, String pathPatternRegex) {
        this.pathPattern = Pattern.compile(pathPatternRegex, Pattern.CASE_INSENSITIVE);
        this.delimiter = delimiter;
    }

    @Override
    protected List<String> tokenizer(String sentence) {
        return sentence != null && !sentence.isEmpty() ? Arrays.asList(sentence.split(delimiter)) : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ValidateOf<List<Token>> evaluator(String word, String sentence) {
        if (sentence == null || sentence.isEmpty()) {
            return ValidateOf.inValid(new ValidationError.EmptyPath());
        } else if (word == null || word.isEmpty()) {
            return ValidateOf.inValid(new ValidationError.EmptyElement(sentence));
        }

        Matcher matcher = pathPattern.matcher(word);
        if (!matcher.find()) {
            return ValidateOf.inValid(new ValidationError.FailedToTokenizeElement(word, sentence));
        }

        String name = matcher.group("name");
        if (name == null || name.isEmpty()) {
            return ValidateOf.inValid(new ValidationError.UnableToParseName(sentence));
        }

        String array = matcher.group("array");
        String arrayIndex = matcher.group("index");

        @SuppressWarnings("rawtypes") ValidateOf results;
        if (array != null && arrayIndex != null && !arrayIndex.equals("")) {
            if (StringUtils.isInteger(arrayIndex)) {
                int index = Integer.parseInt(arrayIndex);
                if (index >= 0) {
                    results = ValidateOf.valid(Arrays.asList(new ObjectToken(name), new ArrayToken(index)));
                } else {
                    results = ValidateOf.inValid(new ValidationError.InvalidArrayNegativeIndexToken(word, index, sentence));
                }
            } else {
                results = ValidateOf.inValid(new ValidationError.InvalidArrayToken(word, arrayIndex, sentence));
            }
        } else if (array != null) {
            results = ValidateOf.inValid(new ValidationError.InvalidArrayIndexToken(word, sentence));
        } else {
            ObjectToken object = new ObjectToken(name);
            List<ObjectToken> list = Collections.singletonList(object);
            results = ValidateOf.valid(list);
        }

        return results;
    }
}
