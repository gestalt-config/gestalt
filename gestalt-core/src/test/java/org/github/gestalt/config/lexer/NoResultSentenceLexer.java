package org.github.gestalt.config.lexer;

import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;

public class NoResultSentenceLexer extends SentenceLexer {
    @Override
    public String getNormalizedDeliminator() {
        return null;
    }

    @Override
    public String getNormalizedArrayOpenTag() {
        return "";
    }

    @Override
    public String getNormalizedArrayCloseTag() {
        return "";
    }

    @Override
    public String getNormalizedMapTag() {
        return "";
    }

    @Override
    public String getDeliminator() {
        return "";
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
    public String normalizeSentence(String sentence) {
        return "";
    }

    @Override
    public GResultOf<List<Token>> scan(String sentence) {
        return GResultOf.result(null);
    }
}
