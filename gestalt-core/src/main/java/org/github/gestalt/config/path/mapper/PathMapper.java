package org.github.gestalt.config.path.mapper;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.List;

public interface PathMapper {
    /**
     * Apply the GestaltConfig to the PathMapper. Needed when building via the ServiceLoader
     * It is a default method as most Path Mapper don't need to apply configs.
     *
     * @param config GestaltConfig to update the PathMapper
     */
    default void applyConfig(GestaltConfig config) {
    }

    /**
     * Takes a sentance and converts it into a set of tokens to navigate.
     *
     * @param path the current path we are looking in
     * @param sentence the next segment of the path to check
     * @param lexer the sentence lexer used to tokenize the sentence.
     * @return the list of tokens to check in the path
     */
    ValidateOf<List<Token>> map(String path, String sentence, SentenceLexer lexer);
}
