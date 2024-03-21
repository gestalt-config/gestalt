package org.github.gestalt.config.node;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.CollectionUtils;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.github.gestalt.config.utils.GResultOf.resultOf;


/**
 * Holds and manages config nodes.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ConfigNodeManager implements ConfigNodeService {
    private final List<ConfigNodeContainer> configNodes = new ArrayList<>();
    // We store the node roots by tags. The default will be an empty Tags.
    private final Map<Tags, ConfigNode> roots = new HashMap<>();

    @Override
    public GResultOf<ConfigNode> addNode(ConfigNodeContainer newNode) throws GestaltException {
        if (newNode == null) {
            throw new GestaltException("No node provided");
        }
        List<ValidationError> errors = new ArrayList<>();

        configNodes.add(newNode);

        // If the root is empty or the root doesn't contain the tags, add it to the root without merging with existing node.
        if (roots.isEmpty() || !roots.containsKey(newNode.getTags())) {
            roots.put(newNode.getTags(), newNode.getConfigNode());
        } else {
            // If there is already a config node in the root, merge the nodes together then save them.
            ConfigNode rootForTokens = roots.get(newNode.getTags());
            GResultOf<ConfigNode> mergedNode = MergeNodes.mergeNodes("", rootForTokens, newNode.getConfigNode());

            if (mergedNode.hasResults()) {
                roots.put(newNode.getTags(), mergedNode.results());
            }

            errors.addAll(mergedNode.getErrors());
        }

        errors.addAll(validateNode(roots.get(newNode.getTags())));
        errors = errors.stream().filter(CollectionUtils.distinctBy(ValidationError::description)).collect(Collectors.toList());

        return resultOf(roots.get(newNode.getTags()), errors);
    }

    @Override
    @SuppressWarnings("ModifyCollectionInEnhancedForLoop")
    public GResultOf<Boolean> postProcess(List<PostProcessor> postProcessors) throws GestaltException {
        if (postProcessors == null) {
            throw new GestaltException("No postProcessors provided");
        }

        if (postProcessors.isEmpty()) {
            return GResultOf.result(true);
        }

        boolean ppSuccessful = true;
        List<ValidationError> errors = new ArrayList<>();

        for (Map.Entry<Tags, ConfigNode> entry : roots.entrySet()) {
            Tags tags = entry.getKey();
            ConfigNode root = entry.getValue();
            GResultOf<ConfigNode> results = postProcess("", root, postProcessors);

            // If we have results we want to update the root to the new post processed config tree.
            errors.addAll(results.getErrors());
            if (results.hasResults()) {
                roots.put(tags, results.results());
            } else {
                ppSuccessful = false;
                errors.add(new ValidationError.NodePostProcessingNoResults());
            }
        }

        return resultOf(ppSuccessful, errors);
    }

    private GResultOf<ConfigNode> postProcess(String path, ConfigNode node, List<PostProcessor> postProcessors) {
        ConfigNode currentNode = node;
        List<ValidationError> errors = new ArrayList<>();

        // apply the post processors to the node.
        // If there are multiple post processors, apply them in order, each post processor operates on the result node from the
        // last post processor.
        for (PostProcessor it : postProcessors) {
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
            return postProcessArray(path, (ArrayNode) currentNode, postProcessors);
        } else if (currentNode instanceof MapNode) {
            return postProcessMap(path, (MapNode) currentNode, postProcessors);
        } else if (currentNode instanceof LeafNode) {
            return resultOf(currentNode, errors);
        } else {
            return GResultOf.errors(new ValidationError.UnknownNodeTypePostProcess(path, node.getClass().getName()));
        }
    }

    private GResultOf<ConfigNode> postProcessArray(String path, ArrayNode node, List<PostProcessor> postProcessors) {
        int size = node.size();
        List<ValidationError> errors = new ArrayList<>();
        ConfigNode[] processedNode = new ConfigNode[size];

        for (int i = 0; i < size; i++) {
            Optional<ConfigNode> currentNodeOption = node.getIndex(i);
            if (currentNodeOption.isPresent()) {
                String nextPath = PathUtil.pathForIndex(path, i);
                GResultOf<ConfigNode> newNode = postProcess(nextPath, currentNodeOption.get(), postProcessors);

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

    private GResultOf<ConfigNode> postProcessMap(String path, MapNode node, List<PostProcessor> postProcessors) {
        Map<String, ConfigNode> processedNode = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();

        for (Map.Entry<String, ConfigNode> entry : node.getMapNode().entrySet()) {
            String key = entry.getKey();
            String nextPath = PathUtil.pathForKey(path, key);
            GResultOf<ConfigNode> newNode = postProcess(nextPath, entry.getValue(), postProcessors);

            errors.addAll(newNode.getErrors());
            if (newNode.hasResults()) {
                processedNode.put(key, newNode.results());
            } else {
                errors.add(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "post processing"));
            }
        }

        return resultOf(new MapNode(processedNode), errors);
    }


    @Override
    public GResultOf<ConfigNode> reloadNode(ConfigNodeContainer reloadNode) throws GestaltException {
        ConfigNode newRoot = null;
        List<ValidationError> errors = new ArrayList<>();

        if (reloadNode == null) {
            throw new GestaltException("Null value provided for Node to be reloaded");
        }

        int index = 0;
        for (ConfigNodeContainer nodePair : configNodes) {

            ConfigNode currentNode = nodePair.getConfigNode();
            if (nodePair.getSource().equals(reloadNode.getSource())) {
                configNodes.set(index, reloadNode);
                currentNode = reloadNode.getConfigNode();
            }

            // only merge with other nodes of the same tags.
            if (!nodePair.matchesTags(reloadNode.getTags())) {
                continue;
            }

            if (newRoot == null) {
                newRoot = currentNode;
            } else {
                GResultOf<ConfigNode> mergedNode = MergeNodes.mergeNodes("", newRoot, currentNode);

                errors.addAll(mergedNode.getErrors());
                if (mergedNode.hasResults()) {
                    newRoot = mergedNode.results();
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode("", "reload node"));
                }
            }
            index++;
        }

        errors.addAll(validateNode(newRoot));
        errors = errors.stream().filter(CollectionUtils.distinctBy(ValidationError::description)).collect(Collectors.toList());

        roots.put(reloadNode.getTags(), newRoot);
        return resultOf(newRoot, errors);
    }

    private List<ValidationError> validateNode(ConfigNode node) {
        return validateNode("", node);
    }

    private List<ValidationError> validateNode(String path, ConfigNode node) {
        if (node instanceof ArrayNode) {
            return validateArrayNode(path, (ArrayNode) node);
        } else if (node instanceof MapNode) {
            return validateMapNode(path, (MapNode) node);
        } else if (node instanceof LeafNode) {
            return validateLeafNode(path, (LeafNode) node);
        } else {
            return Collections.singletonList(new ValidationError.UnknownNodeType(path, node.getClass().getName()));
        }
    }

    private List<ValidationError> validateArrayNode(String path, ArrayNode node) {
        int size = node.size();
        List<ValidationError> errors = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            var valueOptional = node.getIndex(i);
            if (valueOptional.isEmpty()) {
                errors.add(new ValidationError.ArrayMissingIndex(i, path));
            } else {
                String nextPath = PathUtil.pathForIndex(path, i);
                errors.addAll(validateNode(nextPath, valueOptional.get()));
            }
        }
        return errors;
    }

    private List<ValidationError> validateMapNode(String path, MapNode node) {
        List<ValidationError> errors = new ArrayList<>();

        node.getMapNode().forEach((key, value) -> {
            if (key == null) {
                errors.add(new ValidationError.EmptyNodeNameProvided(path));
            } else if (value == null) {
                errors.add(new ValidationError.EmptyNodeValueProvided(path, key));
            } else {
                String nextPath = PathUtil.pathForKey(path, key);
                errors.addAll(validateNode(nextPath, value));
            }
        });

        return errors;
    }

    private List<ValidationError> validateLeafNode(String path, LeafNode node) {
        List<ValidationError> errors = new ArrayList<>();
        if (node == null) {
            errors.add(new ValidationError.LeafNodesIsNull(path));
        } else if (node.getValue().isEmpty()) {
            errors.add(new ValidationError.LeafNodesHaveNoValues(path));
        }

        return errors;
    }

    @Override
    public GResultOf<ConfigNode> navigateToNode(String path, List<Token> tokens, Tags tags) {
        GResultOf<ConfigNode> results;
        // first check with the tags provided.
        GResultOf<ConfigNode> resultsForTags = navigateToNodeInternal(path, tokens, tags);

        // if the current set of tags are the default empty tags: Tags.of()
        // then return the current resultsForTags.
        // otherwise try the default root node, then merge the results.
        if (Tags.of().equals(tags)) {
            results = resultsForTags;
        } else {
            GResultOf<ConfigNode> resultsForDefault = navigateToNodeInternal(path, tokens, Tags.of());

            if (!resultsForTags.hasResults()) {
                results = resultsForDefault;
            } else if (!resultsForDefault.hasResults()) {
                results = resultsForTags;
            } else {
                results = MergeNodes.mergeNodes(path, resultsForDefault.results(), resultsForTags.results());
            }
        }

        return results;
    }

    private GResultOf<ConfigNode> navigateToNodeInternal(String path, List<Token> tokens, Tags tags) {
        ConfigNode currentNode = roots.get(tags);
        List<ValidationError> errors = new ArrayList<>();

        for (Token token : tokens) {
            GResultOf<ConfigNode> result = navigateToNextNode(path, token, currentNode);

            // if there are errors, add them to the error list and do not add the merge results
            if (result.hasErrors()) {
                return GResultOf.errors(result.getErrors());
            }

            if (result.hasResults()) {
                currentNode = result.results();
            } else {
                errors.add(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "navigating to node"));
            }
        }

        return resultOf(currentNode, errors);
    }

    @Override
    public GResultOf<ConfigNode> navigateToNextNode(String path, Token token, ConfigNode currentNode) {

        ConfigNode node = currentNode;

        if (node == null) {
            return GResultOf.errors(new ValidationError.NullNodeForPath(path));
        } else if (token == null) {
            return GResultOf.errors(new ValidationError.NullTokenForPath(path));
        } else if (token instanceof ArrayToken) {
            if (node instanceof ArrayNode) {
                Optional<ConfigNode> nextNode = node.getIndex(((ArrayToken) token).getIndex());
                if (nextNode.isPresent()) {
                    node = nextNode.get();
                } else {
                    return GResultOf.errors(new ValidationError.NoResultsFoundForNode(path, token.getClass(), "navigating to next node"));
                }
            } else {
                return GResultOf.errors(
                    new ValidationError.MismatchedObjectNodeForPath(path, ArrayNode.class, node.getClass()));
            }
        } else if (token instanceof ObjectToken) {
            if (node instanceof MapNode) {
                Optional<ConfigNode> nextNode = node.getKey(((ObjectToken) token).getName());
                if (nextNode.isPresent()) {
                    node = nextNode.get();
                } else {
                    return GResultOf.errors(new ValidationError.NoResultsFoundForNode(path, token.getClass(), "navigating to next node"));
                }
            } else {
                return GResultOf.errors(new ValidationError.MismatchedObjectNodeForPath(path, MapNode.class, node.getClass()));
            }
        } else {
            return GResultOf.errors(new ValidationError.UnsupportedTokenType(path, token));
        }

        return GResultOf.result(node);
    }

    @Override
    public GResultOf<ConfigNode> navigateToNextNode(String path, List<Token> tokens, final ConfigNode currentNode) {
        if (currentNode == null) {
            return GResultOf.errors(new ValidationError.NullNodeForPath(path));
        } else if (tokens == null) {
            return GResultOf.errors(new ValidationError.NullTokenForPath(path));
        } else {
            ConfigNode node = currentNode;
            List<ValidationError> errors = new ArrayList<>();

            for (Token token : tokens) {
                GResultOf<ConfigNode> result = navigateToNextNode(path, token, node);

                // if there are errors, add them to the error list abd do not add the merge results
                if (result.hasErrors()) {
                    return GResultOf.errors(result.getErrors());
                }

                if (result.hasResults()) {
                    node = result.results();
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "navigating to node"));
                }
            }
            return resultOf(node, errors);
        }
    }

    @Override
    public String debugPrintRoot(Tags tags, SecretConcealer secretConcealer) {
        return roots.get(tags).printer("", secretConcealer);
    }

    @Override
    public String debugPrintRoot(SecretConcealer secretConcealer) {
        return roots.entrySet()
            .stream()
            .map((it) -> "tags: " + it.getKey() + " = " + it.getValue().printer("", secretConcealer) )
            .collect(Collectors.joining("\n"));
    }
}
