package org.github.gestalt.config.processor.config;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.github.gestalt.config.utils.GResultOf.resultOf;

public final class ConfigNodeProcessorManager implements ConfigNodeProcessorService {

    private List<ConfigNodeProcessor> configNodeProcessors;
    // Sentence Lexer used to build a normalized path.
    private final SentenceLexer lexer;

    public ConfigNodeProcessorManager(List<ConfigNodeProcessor> configNodeProcessors, SentenceLexer lexer) {
        Objects.requireNonNull(lexer, "Lexer provided to the ConfigNodeProcessorManager should not be null");
        Objects.requireNonNull(configNodeProcessors,
            "configNodeProcessors provided to the ConfigNodeProcessorManager should not be null");

        this.lexer = lexer;
        this.configNodeProcessors = new ArrayList<>(configNodeProcessors);
        this.configNodeProcessors = orderedConfigNodeProcessor();
    }

    @Override
    public void addConfigNodeProcessors(List<ConfigNodeProcessor> configNodeProcessorsToAdd) {
        Objects.requireNonNull(configNodeProcessors,
            "configNodeProcessors added to the ConfigNodeProcessorManager should not be null");

        this.configNodeProcessors.addAll(configNodeProcessorsToAdd);
        this.configNodeProcessors = orderedConfigNodeProcessor();
    }

    /**
     * the result processors in order.
     *
     * @return the result processors in order.
     */
    private List<ConfigNodeProcessor> orderedConfigNodeProcessor() {
        return configNodeProcessors.stream().sorted((to, from) -> {
            var toAnnotation = to.getClass().getAnnotationsByType(ConfigPriority.class);
            var toValue = toAnnotation.length > 0 ? toAnnotation[0].value() : 1000;
            var fromAnnotation = from.getClass().getAnnotationsByType(ConfigPriority.class);
            var fromValue = fromAnnotation.length > 0 ? fromAnnotation[0].value() : 1000;

            return toValue - fromValue;
        }).collect(Collectors.toList());
    }

    @Override
    public GResultOf<ConfigNode> processConfigNodes(String path, ConfigNode node) {
        if (configNodeProcessors.isEmpty()) {
            return GResultOf.result(node);
        }

        ConfigNode currentNode = node;
        List<ValidationError> errors = new ArrayList<>();

        // apply the post processors to the node.
        // If there are multiple post processors, apply them in order, each post processor operates on the result node from the
        // last post processor.
        for (ConfigNodeProcessor it : configNodeProcessors) {
            GResultOf<ConfigNode> processedNode = it.process(path, currentNode);

            errors.addAll(processedNode.getErrors());
            if (processedNode.hasResults()) {
                currentNode = processedNode.results();
            } else {
                errors.add(new ValidationError.NoResultsFoundForNode(path, node.getClass(), "post processing"));
            }
        }

        // recursively apply post processing to children nodes. If this is a leaf, we can return.
        if (currentNode instanceof ArrayNode) {
            return postProcessArray(path, (ArrayNode) currentNode);
        } else if (currentNode instanceof MapNode) {
            return postProcessMap(path, (MapNode) currentNode);
        } else if (currentNode instanceof LeafNode) {
            return resultOf(currentNode, errors);
        } else {
            return GResultOf.errors(new ValidationError.UnknownNodeTypePostProcess(path, node.getClass().getName()));
        }
    }

    private GResultOf<ConfigNode> postProcessArray(String path, ArrayNode node) {
        int size = node.size();
        List<ValidationError> errors = new ArrayList<>();
        ConfigNode[] processedNode = new ConfigNode[size];

        for (int i = 0; i < size; i++) {
            Optional<ConfigNode> currentNodeOption = node.getIndex(i);
            if (currentNodeOption.isPresent()) {
                String nextPath = PathUtil.pathForIndex(lexer, path, i);
                GResultOf<ConfigNode> newNode = processConfigNodes(nextPath, currentNodeOption.get());

                errors.addAll(newNode.getErrors());
                if (newNode.hasResults()) {
                    processedNode[i] = newNode.results();
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode(path, ArrayNode.class, "post processing"));
                }
            }
        }

        return resultOf(new ArrayNode(Arrays.asList(processedNode)), errors);
    }

    private GResultOf<ConfigNode> postProcessMap(String path, MapNode node) {
        Map<String, ConfigNode> processedNode = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();

        for (Map.Entry<String, ConfigNode> entry : node.getMapNode().entrySet()) {
            String key = entry.getKey();
            String nextPath = PathUtil.pathForKey(lexer, path, key);
            GResultOf<ConfigNode> newNode = processConfigNodes(nextPath, entry.getValue());

            errors.addAll(newNode.getErrors());
            if (newNode.hasResults()) {
                processedNode.put(key, newNode.results());
            } else {
                errors.add(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "post processing"));
            }
        }

        return resultOf(new MapNode(processedNode), errors);
    }
}
