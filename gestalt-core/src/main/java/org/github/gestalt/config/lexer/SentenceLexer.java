package org.github.gestalt.config.lexer;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Class to convert a sentence to tokens
 * This class is fully customizable by passing in your own tokenizer and evaluator functions.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public abstract class SentenceLexer {

    /**
     * Return the deliminator.
     *
     * @return the deliminator
     */
    public abstract String getDeliminator();

    /**
     * Takes in a string representation of a path. Ie abc.def or ABC_DEF then returns each element separated by the delimiter
     *
     * @param sentence the sentence to tokenize
     * @return list of tokenized strings from sentance.
     */
    protected abstract List<String> tokenizer(String sentence);

    /**
     * Takes in an elements such as abc or def[3] then converts it into a Token.
     *
     * @param word The First string in the method params is the word in the string we are evaluating
     * @param sentence second string in the method params is the path the word is in.
     * @return ValidateOf list of tokens from a word.
     */
    protected abstract ValidateOf<List<Token>> evaluator(String word, String sentence);

    /**
     * Takes a sentence and normalize it so we can match tokens from all various systems.
     *
     * @param sentence input sentence to normalize
     * @return a normalized sentence.
     */
    public String normalizeSentence(String sentence) {
        return sentence.toLowerCase(Locale.getDefault());
    }

    /**
     * Scan a string a provide a list of tokens.
     *
     * @param sentence sentence to scan
     * @return list of token
     */
    public ValidateOf<List<Token>> scan(String sentence) {

        if (sentence == null || sentence.equals("")) {
            return ValidateOf.inValid(new ValidationError.EmptyPath());
        }

        String normalizedSentence = normalizeSentence(sentence);

        List<String> tokenList = tokenizer(normalizedSentence);

        List<ValidateOf<List<Token>>> tokenWithValidations = tokenList
            .stream()
            .map(word -> evaluator(word, normalizedSentence))
            .collect(Collectors.toList());

        List<Token> tokens = tokenWithValidations.stream().filter(ValidateOf::hasResults)
                                                 .map(ValidateOf::results)
                                                 .flatMap(List::stream)
                                                 .collect(Collectors.toList());

        List<ValidationError> validations = tokenWithValidations.stream().filter(ValidateOf::hasErrors)
                                                                .map(ValidateOf::getErrors)
                                                                .flatMap(List::stream)
                                                                .collect(Collectors.toList());

        return ValidateOf.validateOf(tokens, validations);
    }
}
