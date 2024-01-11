package org.github.gestalt.config.node;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Array config node that holds a list of configs.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ArrayNode implements ConfigNode {

    private final List<ConfigNode> values;

    /**
     * Construct an Array node by providing a list of nodes.
     *
     * @param values list of nodes
     */
    public ArrayNode(List<ConfigNode> values) {
        this.values = Objects.requireNonNullElse(values, Collections.emptyList());
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

    /**
     * get the underlying array for the node.
     *
     * @return the underlying array
     */
    public List<ConfigNode> getArray() {
        return values;
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
