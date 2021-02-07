package org.github.gestalt.config.parser;

import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.List;

/**
 * Takes in a tokenized config and returns a config node tree.
 *
 * @author Colin Redmond
 */
public interface ConfigParser {

    /**
     * Takes in a tokenized config and returns a config node tree.
     *
     * @param configs configs to parse
     * @return the config node built
     */
    ValidateOf<ConfigNode> parse(List<Pair<List<Token>, ConfigValue>> configs);
}
