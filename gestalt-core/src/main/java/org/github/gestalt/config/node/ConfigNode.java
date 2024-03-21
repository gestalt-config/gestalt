package org.github.gestalt.config.node;

import org.github.gestalt.config.secret.rules.SecretConcealer;

import java.util.Optional;

/**
 * Interface for a config node.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface ConfigNode {

    /**
     * Get the node type.
     *
     * @return the node type
     */
    NodeType getNodeType();

    /**
     * get the optional value for a leaf node, empty otherwise.
     *
     * @return optional value
     */
    Optional<String> getValue();

    /**
     * Get the optional config node by index for arrays, empty otherwise.
     *
     * @param index for arrays the index we want
     * @return optional config
     */
    Optional<ConfigNode> getIndex(int index);

    /**
     * Get the optional config node by key for objects, empty otherwise.
     *
     * @param key for node we are looking for.
     * @return optional config node
     */
    Optional<ConfigNode> getKey(String key);

    /**
     * side of the node, or 1 if a leaf.
     *
     * @return side of the node
     */
    int size();

    String printer(String path, SecretConcealer secretConcealer);
}
