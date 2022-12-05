package org.github.gestalt.config.path.mapper;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.ArrayList;
import java.util.List;

@ConfigPriority(500)
public class CamelCasePathMapper implements PathMapper {
    @Override
    public ValidateOf<List<Token>> map(String path, String sentence, SentenceLexer lexer) {
        String[] camelCaseWords = sentence.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
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
