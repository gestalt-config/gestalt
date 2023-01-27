package org.github.gestalt.config.path.mapper;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Splits the sentence by camel case. So each capitalized word is a new token.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
@ConfigPriority(500)
public class CamelCasePathMapper implements PathMapper {
    private final Pattern regex = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

    @SuppressWarnings("StringSplitter")
    @Override
    public ValidateOf<List<Token>> map(String path, String sentence, SentenceLexer lexer) {
        String[] camelCaseWords = regex.split(sentence);
        List<Token> tokens = new ArrayList<>();
        for (String word : camelCaseWords) {
            ValidateOf<List<Token>> lexedValidateOf = lexer.scan(word);
            // if there are errors, add them to the error list abd do not add the merge results
            if (lexedValidateOf.hasErrors()) {
                return ValidateOf.inValid(lexedValidateOf.getErrors());
            }

            if (!lexedValidateOf.hasResults()) {
                return ValidateOf.inValid(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "decoding"));
            }
            tokens.addAll(lexedValidateOf.results());
        }
        return ValidateOf.valid(tokens);
    }
}
