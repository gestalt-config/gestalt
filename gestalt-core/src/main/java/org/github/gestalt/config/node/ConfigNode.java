package org.github.gestalt.config.node;

import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.secret.rules.SecretConcealer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for a config node.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
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
     * return true if the node has a value. False if it has no value.
     *
     * @return true if the node has a value. False if it has no value.
     */
    boolean hasValue();

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

    /**
     * Get the metadata for a specific key.
     *
     * @param key the key to search for.
     * @return the optional metadata for the key
     */
    List<MetaDataValue<?>> getMetadata(String key);

    /**
     * Get the backing map of metadata.
     *
     * @return the backing map of metadata.
     */
    Map<String, List<MetaDataValue<?>>> getMetadata();


    /**
     * Test if the metadata has a specific key.
     *
     * @param key key to check if it exists.
     * @return true if the key exists.
     */
    boolean hasMetadata(String key);

    /**
     * We are rolling up a metadata from a child node to the parent.
     * This will return any metadata that the parent should inherit.
     *
     * @return This will return any metadata that the parent should inherit.
     */
    Map<String, List<MetaDataValue<?>>> getRolledUpMetadata();

    /**
     * Safely prints out the config tree at this path.
     *
     * @param path the current path.
     * @param secretConcealer used to conceal any secrets.
     * @param lexer lexer used to get the normalized path delimiters.
     *
     * @return the safe string with secrets concealed.
     */
    String printer(String path, SecretConcealer secretConcealer, SentenceLexer lexer);
}
