package org.config.gestalt.parser;

import org.config.gestalt.entity.ConfigValue;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.Pair;
import org.config.gestalt.utils.ValidateOf;

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
