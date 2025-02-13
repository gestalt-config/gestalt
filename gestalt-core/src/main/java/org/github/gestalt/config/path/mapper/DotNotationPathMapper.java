package org.github.gestalt.config.path.mapper;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Splits the sentence by camel case and converts it to dot notation. So each capitalized word is a new token.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@ConfigPriority(500)
public final class DotNotationPathMapper implements PathMapper {
    private final Pattern regex = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

    @SuppressWarnings("StringSplitter")
    @Override
    public GResultOf<List<Token>> map(String path, String sentence, SentenceLexer lexer) {
        if (sentence == null || sentence.isEmpty()) {
            return GResultOf.errors(new ValidationError.MappingPathEmpty(path, "DotNotationPathMapper"));
        }

        String[] camelCaseWords = regex.split(sentence);
        List<Token> tokens = new ArrayList<>();
        for (String word : camelCaseWords) {
            GResultOf<List<Token>> lexedGResultOf = lexer.scan(word);

            // if there are errors, add them to the error list and do not add the merge results
            if (lexedGResultOf.hasErrors()) {
                return GResultOf.errors(lexedGResultOf.getErrors());
            }

            if (!lexedGResultOf.hasResults()) {
                return GResultOf.errors(new ValidationError.NoResultsMappingPath(path, sentence, "dot notation path mapping"));
            }
            tokens.addAll(lexedGResultOf.results());
        }
        return GResultOf.result(tokens);
    }
}
