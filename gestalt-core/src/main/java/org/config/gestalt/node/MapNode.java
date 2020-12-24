package org.config.gestalt.node;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MapNode implements ConfigNode {

    private final Map<String, ConfigNode> mapNode;

    public MapNode(Map<String, ConfigNode> mapNode) {
        if (mapNode != null) {
            this.mapNode = mapNode;
        } else {
            this.mapNode = Collections.emptyMap();
        }
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
