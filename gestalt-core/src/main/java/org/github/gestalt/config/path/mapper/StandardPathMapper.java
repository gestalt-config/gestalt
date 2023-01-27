package org.github.gestalt.config.path.mapper;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.List;

/**
 * Standard Path mapper looks for an exact match.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
@ConfigPriority(1000)
public class StandardPathMapper implements PathMapper {
    @Override
    public ValidateOf<List<Token>> map(String path, String sentence, SentenceLexer lexer) {
        return lexer.scan(sentence);
    }
}
