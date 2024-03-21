package org.github.gestalt.config.node;

import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.utils.PathUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Map node holds a map of config nodes we can get by key.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class MapNode implements ConfigNode {

    private final Map<String, ConfigNode> nodes;

    /**
     * Construct the MapNode by providing a map for the current tree.
     *
     * @param mapNode map for the current tree
     */
    public MapNode(Map<String, ConfigNode> mapNode) {
        this.nodes = Objects.requireNonNullElse(mapNode, Collections.emptyMap());
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
        if (nodes.containsKey(key)) {
            return Optional.ofNullable(nodes.get(key));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int size() {
        return nodes.size();
    }

    /**
     * Get the map node.
     *
     * @return Get the map node
     */
    public Map<String, ConfigNode> getMapNode() {
        return nodes;
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
        return Objects.equals(nodes, mapNode1.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes);
    }

    @Override
    public String toString() {
        return printer("", null);
    }

    @Override
    public String printer(String path, SecretConcealer secretConcealer) {
        return "MapNode{" +
            nodes.entrySet().stream()
                .map((it) -> {
                    var printedNode = new StringBuilder().append(it.getKey()).append('=');
                    if (it.getValue() != null) {
                        printedNode.append(it.getValue().printer(PathUtil.pathForKey(path, it.getKey()), secretConcealer));
                    } else {
                        printedNode.append("'null'");
                    }
                    return printedNode.toString();
                })
                .collect(Collectors.joining(", ")) +
            "}";
    }
}
