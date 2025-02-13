package org.github.gestalt.config.path.mapper;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Splits the sentence by camel case and converts it to Snake Case.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@ConfigPriority(550)
public final class SnakeCasePathMapper implements PathMapper {
    private final Pattern regex = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

    @SuppressWarnings("StringSplitter")
    @Override
    public GResultOf<List<Token>> map(String path, String sentence, SentenceLexer lexer) {
        if (sentence == null || sentence.isEmpty()) {
            return GResultOf.errors(new ValidationError.MappingPathEmpty(path, "SnakeCasePathMapper"));
        }

        String[] camelCaseWords = regex.split(sentence);
        String kebebCase = Arrays.stream(camelCaseWords)
            .map(it -> it.toLowerCase(Locale.getDefault()))
            .collect(Collectors.joining("_"));

        GResultOf<List<Token>> lexedGResultOf = lexer.scan(kebebCase);

        return lexedGResultOf.mapWithError(ArrayList::new,
            new ValidationError.NoResultsMappingPath(path, sentence, "Snake case path mapping"));
    }
}
