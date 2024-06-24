package org.github.gestalt.config.node;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.secret.rules.SecretConcealer;

import java.util.Objects;
import java.util.Optional;

/**
 * leaf node that holds a value.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class LeafNode implements ConfigNode {
    private final String value;

    /**
     * Construct a leaf node that holds a single value.
     *
     * @param value string value for current leaf node
     */
    public LeafNode(String value) {
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
    public Optional<ConfigNode> getIndex(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigNode> getKey(String key) {
        return Optional.empty();
    }

    @Override
    public int size() {
        return 1;
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
            nodeValue = secretConcealer.concealSecret(path, nodeValue);
        }
        return "LeafNode{" +
            "value='" + nodeValue + '\'' +
            "}";
    }
}
