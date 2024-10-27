package org.github.gestalt.config.node;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Map node holds a map of config nodes we can get by key.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class MapNode extends AbstractConfigNode {

    private final Map<String, ConfigNode> nodes;

    /**
     * Construct the MapNode by providing a map for the current tree.
     *
     * @param mapNode map for the current tree
     */
    public MapNode(Map<String, ConfigNode> mapNode) {
        this(mapNode, Map.of());
    }

    public MapNode(Map<String, ConfigNode> mapNode, Map<String, List<MetaDataValue<?>>> metadata) {
        super(metadata);
        this.nodes = Collections.unmodifiableMap(Objects.requireNonNullElse(mapNode, Collections.emptyMap()));
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.MAP;
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

    @Override
    public Map<String, List<MetaDataValue<?>>> getRolledUpMetadata() {

        // for each entry in the map, try and roll it up. It will return the rolled up map.
        // Then we continue for each entry passing the previously rolled up map into the next metadata.
        Map<String, List<MetaDataValue<?>>> rolledUpMetadata = new HashMap<>();
        for (ConfigNode configNode : nodes.values()) {
            if (configNode != null) {
                Map<String, List<MetaDataValue<?>>> nodeMetadata = configNode.getRolledUpMetadata();
                for (Map.Entry<String, List<MetaDataValue<?>>> entry : nodeMetadata.entrySet()) {
                    for (MetaDataValue<?> metadata : entry.getValue()) {
                        rolledUpMetadata = metadata.rollup(rolledUpMetadata);
                    }
                }
            }
        }

        //now rollup the array nodes metadata
        for (Map.Entry<String, List<MetaDataValue<?>>> entry : metadata.entrySet()) {
            for (MetaDataValue<?> metadata : entry.getValue()) {
                rolledUpMetadata = metadata.rollup(rolledUpMetadata);
            }
        }

        return rolledUpMetadata;
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
        // should not be used.
        return printer("", null, new PathLexer());
    }

    @Override
    public String printer(String path, SecretConcealer secretConcealer, SentenceLexer lexer) {
        return "MapNode{" +
            nodes.entrySet().stream()
                .map((it) -> {
                    var printedNode = new StringBuilder().append(it.getKey()).append('=');
                    if (it.getValue() != null) {
                        printedNode.append(it.getValue().printer(PathUtil.pathForKey(lexer, path, it.getKey()), secretConcealer, lexer));
                    } else {
                        printedNode.append("'null'");
                    }
                    return printedNode.toString();
                })
                .collect(Collectors.joining(", ")) +
            "}";
    }
}
