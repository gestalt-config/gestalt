package org.github.gestalt.config.lexer;

import java.util.Locale;

public class LowerCaseSentenceNormalizer implements SentenceNormalizer {
    @Override
    public String normalizeSentence(String sentence) {
        return sentence.toLowerCase(Locale.getDefault());
    }
}
