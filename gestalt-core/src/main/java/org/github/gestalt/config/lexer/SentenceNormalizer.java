package org.github.gestalt.config.lexer;

public interface SentenceNormalizer {
    /**
     * Takes a sentence and normalize it so we can match tokens from all various systems.
     *
     * @param sentence input sentence to normalize
     * @return a normalized sentence.
     */
    String normalizeSentence(String sentence);
}
