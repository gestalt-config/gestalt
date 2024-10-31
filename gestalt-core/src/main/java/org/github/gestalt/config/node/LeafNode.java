package org.github.gestalt.config.node;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.secret.rules.SecretConcealer;

import java.util.*;

/**
 * leaf node that holds a value.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class LeafNode extends AbstractConfigNode {
    private final String value;

    /**
     * Construct a leaf node that holds a single value.
     *
     * @param value string value for current leaf node
     */
    public LeafNode(String value) {
        this(value, Map.of());
    }

    /**
     * Constructs a lead node that holds a single value as well as some metadata.
     *
     * @param value    string value for current leaf node
     * @param metadata metadata associated with the node.
     */
    public LeafNode(String value, Map<String, List<MetaDataValue<?>>> metadata) {
        super(metadata);
        this.value = value;
    }

    @Override
    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public boolean hasValue() {
        return value != null;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LEAF;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Map<String, List<MetaDataValue<?>>> getRolledUpMetadata() {
        if (metadata.isEmpty()) {
            return metadata;
        }

        // for each entry in the map, try and roll it up. It will return the rolled up map.
        // Then we continue for each entry passing the previously rolled up map into the next metadata.
        Map<String, List<MetaDataValue<?>>> rolledUpMetadata = new HashMap<>();
        for (Map.Entry<String, List<MetaDataValue<?>>> entries : metadata.entrySet()) {
            for (MetaDataValue<?> metadata : entries.getValue()) {
                rolledUpMetadata = metadata.rollup(rolledUpMetadata);
            }
        }

        return rolledUpMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LeafNode)) {
            return false;
        }
        LeafNode leafNode = (LeafNode) o;
        return Objects.equals(value, leafNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        // should not be used.
        return printer("", null, new PathLexer());
    }

    @Override
    public String printer(String path, SecretConcealer secretConcealer, SentenceLexer lexer) {
        String nodeValue = value;
        if (secretConcealer != null) {
            nodeValue = secretConcealer.concealSecret(path, nodeValue, metadata);
        }
        return "LeafNode{" +
            "value='" + nodeValue + '\'' +
            "}";
    }
}
