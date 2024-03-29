package org.github.gestalt.config.lexer;

import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;

public class NoResultSentenceLexer extends SentenceLexer {
    @Override
    public String getDeliminator() {
        return null;
    }

    @Override
    protected List<String> tokenizer(String sentence) {
        return null;
    }

    @Override
    protected GResultOf<List<Token>> evaluator(String word, String sentence) {
        return null;
    }

    @Override
    public GResultOf<List<Token>> scan(String sentence) {
        return GResultOf.result(null);
    }
}
