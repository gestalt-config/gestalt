package org.config.gestalt.node;

import java.util.Objects;
import java.util.Optional;

public class LeafNode implements ConfigNode {
    private final String value;

    public LeafNode(String value) {
        this.value = value;
    }

    @Override
    public Optional<String> getValue() {
        return Optional.ofNullable(value);
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
        return "LeafNode{" +
            "value='" + value + '\'' +
            '}';
    }
}
