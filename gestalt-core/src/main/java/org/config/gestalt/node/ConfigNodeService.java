package org.config.gestalt.node;

import org.config.gestalt.entity.ConfigNodeContainer;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.ValidateOf;

import java.util.List;

/**
 * Holds and manages config nodes.
 *
 * @author Colin Redmond
 */
public interface ConfigNodeService {

    /**
     * Add a new node, if a root already exists merge the nodes.
     * When adding a node and merging, the new node always takes precedence.
     *
     * @param newNode node to be added
     * @return newly merged node
     * @throws GestaltException any exceptions
     */
    ValidateOf<ConfigNode> addNode(ConfigNodeContainer newNode) throws GestaltException;

    /**
     * Reload a node, if there are more than one node it will merge it into the config tree in the same order as the existing node.
     * The container node has an ID so we can identity and know which node to reload to preserve order.
     *
     * @param reloadNode node to reload.
     * @return the new root node
     * @throws GestaltException any exceptions
     */
    ValidateOf<ConfigNode> reloadNode(ConfigNodeContainer reloadNode) throws GestaltException;

    /**
     * navigate to a node for a path from the root.
     *
     * @param path to navigate to
     * @param tokens list of tokens to navigate to.
     * @return ValidateOf node or any errors.
     */
    ValidateOf<ConfigNode> navigateToNode(String path, List<Token> tokens);

    /**
     * From a given node navigate to the next node.
     *
     * @param path to navigate to
     * @param token token for the next node
     * @param currentNode current node we want to navigate from
     * @return ValidateOf node or any errors.
     */
    ValidateOf<ConfigNode> navigateToNextNode(String path, Token token, ConfigNode currentNode);
}
