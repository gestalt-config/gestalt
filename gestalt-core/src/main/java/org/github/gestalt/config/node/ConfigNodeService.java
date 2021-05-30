package org.github.gestalt.config.node;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;

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
     * Apply a list of Post Processors on the root node. This allows a post processor to modify the config tree in any way.
     * It will navigate to each node in the tree and pass it to the post processor.
     * The post processors are run in order of priority, with the next post processor getting the results from the previous.
     * The post processor returns a node that is then used to replace the current node.
     *
     * @param postProcessors list of post processors to apply.
     * @return newly processed node
     * @throws GestaltException any exceptions
     */
    ValidateOf<ConfigNode> postProcess(List<PostProcessor> postProcessors) throws GestaltException;

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
     * If an ArrayToken and the config node is an ArrayNode it will return the next node by index.
     * If a ObjectToken and the config node is a MapNode it will return the next node by key.
     *
     * @param path to here for logging.
     * @param token token for the next node
     * @param currentNode current node we want to navigate from
     * @return ValidateOf node or any errors.
     */
    ValidateOf<ConfigNode> navigateToNextNode(String path, Token token, ConfigNode currentNode);
}
