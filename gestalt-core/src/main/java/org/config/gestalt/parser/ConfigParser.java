package org.config.gestalt.parser;

import org.config.gestalt.entity.ConfigValue;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.Pair;
import org.config.gestalt.utils.ValidateOf;

import java.util.List;

public interface ConfigParser {
    ValidateOf<ConfigNode> parse(List<Pair<List<Token>, ConfigValue>> configs);
}
