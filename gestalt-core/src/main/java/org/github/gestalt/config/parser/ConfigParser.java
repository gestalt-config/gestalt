package org.github.gestalt.config.parser;

import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;

import java.util.List;

/**
 * Takes in a tokenized config and returns a config node tree.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface ConfigParser {

    /**
     * Takes in a tokenized config and returns a config node tree.
     *
     * @param lexer        lexer used to get the delimiter to build the path
     * @param configs      configs to parse
     * @param failOnErrors if we want to fail on errors while parsing or try and recover. Results can be unpredictable if it continues
     * @return the config node built
     */
    GResultOf<ConfigNode> parse(SentenceLexer lexer, List<Pair<List<Token>, ConfigValue>> configs, boolean failOnErrors);
}
