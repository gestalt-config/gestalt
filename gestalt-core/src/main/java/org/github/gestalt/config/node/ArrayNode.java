package org.github.gestalt.config.node;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Array config node that holds a list of configs.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ArrayNode extends AbstractConfigNode {

    private final List<ConfigNode> values;

    /**
     * Construct an Array node by providing a list of nodes.
     *
     * @param values list of nodes
     */
    public ArrayNode(List<ConfigNode> values) {
        this(values, Map.of());
    }

    public ArrayNode(List<ConfigNode> values, Map<String, List<MetaDataValue<?>>> metadata) {
        super(metadata);
        this.values = Collections.unmodifiableList(Objects.requireNonNullElse(values, Collections.emptyList()));
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ARRAY;
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
    public int size() {
        return values.size();
    }

    @Override
    public Map<String, List<MetaDataValue<?>>> getRolledUpMetadata() {

        // for each entry in the map, try and roll it up. It will return the rolled up map.
        // Then we continue for each entry passing the previously rolled up map into the next metadata.
        Map<String, List<MetaDataValue<?>>> rolledUpMetadata = new HashMap<>();
        for (ConfigNode configNode : values) {
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
        // should not be used.
        return printer("", null, new PathLexer());
    }

    @Override
    public String printer(String path, SecretConcealer secretConcealer, SentenceLexer lexer) {
        return "ArrayNode{" +
            "values=[" +
            IntStream.range(0, values.size())
                .mapToObj(n -> values.get(n).printer(PathUtil.pathForIndex(lexer, path, n), secretConcealer, lexer))
                .collect(Collectors.joining(", ")) +
            "]}";
    }
}
