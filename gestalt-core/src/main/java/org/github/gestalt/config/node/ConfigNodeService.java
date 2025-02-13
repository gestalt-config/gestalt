package org.github.gestalt.config.node;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;

/**
 * Holds and manages config nodes.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
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
    GResultOf<ConfigNode> addNode(ConfigNodeContainer newNode) throws GestaltException;

    /**
     * Apply a list of Config Node Processors on the root node. This allows a Config Node processor to modify the config tree in any way.
     * It will navigate to each node in the tree and pass it to the Config Node processor.
     * The Config Node processors are run in order of priority, with the next Config Node processor getting the results from the previous.
     * The Config Node processor returns a node that is then used to replace the current node.
     *
     * @return if the post process has completed successfully
     * @throws GestaltException any exceptions
     */
    GResultOf<Boolean> processConfigNodes() throws GestaltException;

    /**
     * Reload a node, if there are more than one node it will merge it into the config tree in the same order as the existing node.
     * The container node has an ID so we can identity and know which node to reload to preserve order.
     *
     * @param reloadNode node to reload.
     * @return the new root node
     * @throws GestaltException any exceptions
     */
    GResultOf<ConfigNode> reloadNode(ConfigNodeContainer reloadNode) throws GestaltException;

    /**
     * navigate to a node for a path from the root.
     *
     * @param path   to navigate to
     * @param tokens list of tokens to navigate to.
     * @param tags   list of tags to match
     * @return GResultOf node or any errors.
     */
    GResultOf<ConfigNode> navigateToNode(String path, List<Token> tokens, Tags tags);

    /**
     * From a given node navigate to the next node.
     * If an ArrayToken and the config node is an ArrayNode it will return the next node by index.
     * If a ObjectToken and the config node is a MapNode it will return the next node by key.
     *
     * @param path        to here for logging.
     * @param token       token for the next node
     * @param currentNode current node we want to navigate from
     * @return GResultOf node or any errors.
     */
    GResultOf<ConfigNode> navigateToNextNode(String path, Token token, ConfigNode currentNode);

    /**
     * From a given node navigate to the next node.
     *
     * @param path        to here for logging.
     * @param tokens      list of token for the next node
     * @param currentNode current node we want to navigate from
     * @return GResultOf node or any errors.
     */
    GResultOf<ConfigNode> navigateToNextNode(String path, List<Token> tokens, ConfigNode currentNode);

    /**
     * prints a root node to a string.
     *
     * @param tags tags of the node to print
     * @param secretConcealer utility to conceal secrets
     * @return printout of the root node
     */
    String debugPrintRoot(Tags tags, SecretConcealer secretConcealer);

    /**
     * prints a root node to a string.
     *
     * @param secretConcealer utility to conceal secrets
     * @return printout of the root node
     */
    String debugPrintRoot(SecretConcealer secretConcealer);
}
