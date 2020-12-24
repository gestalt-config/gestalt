package org.config.gestalt.node;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ArrayNode implements ConfigNode {
    private final List<ConfigNode> values;

    public ArrayNode(List<ConfigNode> values) {
        if (values != null) {
            this.values = values;
        } else {
            this.values = Collections.emptyList();
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ARRAY;
    }

    @Override
    public Optional<String> getValue() {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigNode> getIndex(int index) {
        if (values.size() > index) {
            return Optional.ofNullable(values.get(index));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ConfigNode> getKey(String key) {
        return Optional.empty();
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayNode)) {
            return false;
        }
        ArrayNode arrayNode = (ArrayNode) o;
        return Objects.equals(values, arrayNode.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return "ArrayNode{" +
            "values=" + values +
            '}';
    }
}
