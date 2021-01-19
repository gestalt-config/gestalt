package org.config.gestalt.node;

import org.config.gestalt.entity.ConfigNodeContainer;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.ValidateOf;

import java.util.List;

public interface ConfigNodeService {
    ValidateOf<ConfigNode> addNode(ConfigNodeContainer newNode) throws GestaltException;

    ValidateOf<ConfigNode> reloadNode(ConfigNodeContainer newNode) throws GestaltException;

    ValidateOf<ConfigNode> navigateToNode(String path, List<Token> tokens);

    ValidateOf<ConfigNode> navigateToNextNode(String path, Token token, ConfigNode currentNode);
}
