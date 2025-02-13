package org.github.gestalt.config.node;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;

import static org.github.gestalt.config.utils.GResultOf.resultOf;

/**
 * Utility class for merging nodes.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class MergeNodes {

    /**
     * Utility class has no constructor.
     */
    private MergeNodes() {

    }

    /**
     * Merge two nodes and return the results of the merge. The values in node1 will be overridden by the values in node2
     *
     * @param path  the path of the nodes we are merging.
     * @param lexer lexer used to get the delimiter to build the path
     * @param node1 the base node, its properties will be overridden by the node2
     * @param node2 the node to override the values of.
     * @return the merged nodes.
     */
    public static GResultOf<ConfigNode> mergeNodes(String path, SentenceLexer lexer, ConfigNode node1, ConfigNode node2) {
        if (node1.getClass() != node2.getClass()) {
            return GResultOf.errors(
                new ValidationError.UnableToMergeDifferentNodes(node1.getClass(), node2.getClass()));
        } else {
            if (node1 instanceof ArrayNode) {
                return mergeArrayNodes(path, lexer, (ArrayNode) node1, (ArrayNode) node2);
            } else if (node1 instanceof MapNode) {
                return mergeMapNodes(path, lexer, (MapNode) node1, (MapNode) node2);
            } else if (node1 instanceof LeafNode) {
                return mergeLeafNodes(path, (LeafNode) node1, (LeafNode) node2);
            } else {
                return GResultOf.errors(new ValidationError.UnknownNodeType(path, node1.getClass().getName()));
            }
        }
    }

    private static GResultOf<ConfigNode> mergeArrayNodes(String path, SentenceLexer lexer, ArrayNode arrayNode1, ArrayNode arrayNode2) {
        // get the maximum array size of both the nodes.
        int maxSize = Math.max(arrayNode1.size(), arrayNode2.size());
        ConfigNode[] values = new ConfigNode[maxSize];
        List<ValidationError> errors = new ArrayList<>();

        // loop though the array to the max size.
        // for each index check if exists in both, then merge the nodes.
        // if it only exists in one or the other, add which ever it exists in.
        // if it exists in neither add a validation error.
        for (int i = 0; i < maxSize; i++) {
            Optional<ConfigNode> array1AtIndex = arrayNode1.getIndex(i);
            Optional<ConfigNode> array2AtIndex = arrayNode2.getIndex(i);
            if (array1AtIndex.isPresent() && array2AtIndex.isPresent()) {
                String nextPath = PathUtil.pathForIndex(lexer, path, i);
                GResultOf<ConfigNode> result = mergeNodes(nextPath, lexer, array1AtIndex.get(), array2AtIndex.get());

                // if there are errors, add them to the error list abd do not add the merge results
                errors.addAll(result.getErrors());
                if (result.hasResults()) {
                    values[i] = result.results();
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode(path, ArrayNode.class, "merging arrays"));
                }
            } else if (array1AtIndex.isPresent()) {
                values[i] = array1AtIndex.get();
            } else if (array2AtIndex.isPresent()) {
                values[i] = array2AtIndex.get();
            } else {
                errors.add(new ValidationError.ArrayMissingIndex(i, path));
            }
        }

        ArrayNode results = new ArrayNode(Arrays.asList(values));
        return resultOf(results, errors);
    }

    private static GResultOf<ConfigNode> mergeMapNodes(String path, SentenceLexer lexer, MapNode mapNode1, MapNode mapNode2) {
        Map<String, ConfigNode> mergedNode = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();

        // First we check all the nodes in mapNode1.
        // If the node also exists in mapNode2, it exists in both. So we need to merge them.
        // if it only exists in mapNode1 then we can add it to the merged Node, as it is not in mapNode2.
        for (Map.Entry<String, ConfigNode> entry : mapNode1.getMapNode().entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                errors.add(new ValidationError.EmptyNodeNameProvided(path));
            } else if (entry.getValue() == null) {
                errors.add(new ValidationError.EmptyNodeValueProvided(path, key));
            } else if (mapNode2.getKey(key).isPresent()) {
                String nextPath = PathUtil.pathForKey(lexer, path, key);
                GResultOf<ConfigNode> result = mergeNodes(nextPath, lexer, entry.getValue(), mapNode2.getKey(key).get());

                // if there are errors, add them to the error list abd do not add the merge results
                errors.addAll(result.getErrors());
                if (result.hasResults()) {
                    mergedNode.putIfAbsent(key, result.results());
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "merging maps"));
                }
            } else {
                mergedNode.putIfAbsent(key, entry.getValue());
            }
        }

        // Do a pass on mapNode2 and add any nodes that were not a intersection with mapNode1.
        // this is simply adding all nodes in mapNode2 using putIfAbsent, so they will only be added if they were missing.
        for (Map.Entry<String, ConfigNode> entry : mapNode2.getMapNode().entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                errors.add(new ValidationError.EmptyNodeNameProvided(path));
            } else if (entry.getValue() == null) {
                errors.add(new ValidationError.EmptyNodeValueProvided(path, key));
            } else {
                mergedNode.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        return resultOf(new MapNode(mergedNode), errors);
    }

    private static GResultOf<ConfigNode> mergeLeafNodes(String path, LeafNode node1, LeafNode node2) {
        if (node2.getValue().isPresent()) {
            return GResultOf.result(node2);
        } else if (node1.getValue().isPresent()) {
            return GResultOf.result(node1);
        } else {
            return GResultOf.errors(new ValidationError.LeafNodesHaveNoValues(path));
        }
    }
}
