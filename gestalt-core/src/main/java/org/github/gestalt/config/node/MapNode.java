package org.github.gestalt.config.node;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Map node holds a map of config nodes we can get by key.
 *
 * @author Colin Redmond
 */
public class MapNode implements ConfigNode {

    private final Map<String, ConfigNode> mapNode;

    /**
     * Construct the MapNode by providing a map for the current tree.
     *
     * @param mapNode map for the current tree
     */
    public MapNode(Map<String, ConfigNode> mapNode) {
        this.mapNode = Objects.requireNonNullElse(mapNode, Collections.emptyMap());
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.MAP;
    }

    @Override
    public Optional<String> getValue() {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigNode> getIndex(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigNode> getKey(String key) {
        if (mapNode.containsKey(key)) {
            return Optional.ofNullable(mapNode.get(key));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int size() {
        return mapNode.size();
    }

    /**
     * Get the map node.
     *
     * @return Get the map node
     */
    public Map<String, ConfigNode> getMapNode() {
        return mapNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapNode)) {
            return false;
        }
        MapNode mapNode1 = (MapNode) o;
        return Objects.equals(mapNode, mapNode1.mapNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapNode);
    }

    @Override
    public String toString() {
        return "MapNode{" +
            "mapNode=" + mapNode +
            '}';
    }
}
