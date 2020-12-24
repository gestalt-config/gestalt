package org.config.gestalt.lexer;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.ValidateOf;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to convert a sentence to tokens
 * This class is fully customizable by passing in your own tokenizer and evaluator functions.
 */
public abstract class SentenceLexer {

    /**
     * Takes in a string representation of a path. Ie abc.def or ABC_DEF then returns each element separated by the delimiter
     *
     * @param sentence the sentence to tokenize
     */
    protected abstract List<String> tokenizer(String sentence);

    /**
     * Takes in an elements such as abc or def[3] then converts it into a Token.
     *
     * @param word     The First string in the method params is the word in the string we are evaluating
     * @param sentence second string in the method params is the path the word is in.
     */
    protected abstract ValidateOf<List<Token>> evaluator(String word, String sentence);

    public ValidateOf<List<Token>> scan(String sentence) {

        if (sentence == null || sentence.equals("")) {
            return ValidateOf.inValid(new ValidationError.EmptyPath());
        }

        List<String> tokenList = tokenizer(sentence);

        List<ValidateOf<List<Token>>> tokenWithValidations = tokenList
            .stream()
            .map(word -> evaluator(word, sentence))
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
