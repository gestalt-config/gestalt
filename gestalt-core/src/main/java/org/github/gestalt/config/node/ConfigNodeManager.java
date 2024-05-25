package org.github.gestalt.config.node;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.CollectionUtils;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;
import java.util.concurrent.locks.StampedLock;
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
    private final LinkedHashMap<Tags, ConfigNode> roots = new LinkedHashMap<>();
    // lock to ensure we are thread safe.
    private final StampedLock lock = new StampedLock();
    private final ConfigNodeTagResolutionStrategy configNodeTagResolutionStrategy;
    // Sentence Lexer used to build a normalized path.
    private SentenceLexer lexer;

    public ConfigNodeManager() {
        this(new EqualTagsWithDefaultTagResolutionStrategy(), new PathLexer());
    }

    /**
     * Constructor that takes a sentence Lexer to build a normalized path.
     *
     * @param lexer sentence Lexer to build a normalized path
     */
    public ConfigNodeManager(SentenceLexer lexer) {
        this(new EqualTagsWithDefaultTagResolutionStrategy(), lexer);
    }

    /**
     * Constructor that takes a sentence Lexer to build a normalized path. Allows an override of the configNodeResolutionStrategy.
     *
     * @param configNodeTagResolutionStrategy how to resolve the config nodes to search.
     * @param lexer                        sentence Lexer to build a normalized path.
     */
    public ConfigNodeManager(ConfigNodeTagResolutionStrategy configNodeTagResolutionStrategy, SentenceLexer lexer) {
        this.configNodeTagResolutionStrategy = configNodeTagResolutionStrategy;
        this.lexer = lexer;
    }

    @Override
    public void setLexer(SentenceLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public GResultOf<ConfigNode> addNode(ConfigNodeContainer newNode) throws GestaltException {
        if (newNode == null) {
            throw new GestaltException("No node provided");
        }
        List<ValidationError> errors = new ArrayList<>();
        long stamp = lock.writeLock();
        try {
            configNodes.add(newNode);

            // If the root is empty or the root doesn't contain the tags, add it to the root without merging with existing node.
            if (roots.isEmpty() || !roots.containsKey(newNode.getTags())) {
                roots.put(newNode.getTags(), newNode.getConfigNode());
            } else {
                // If there is already a config node in the root, merge the nodes together then save them.
                ConfigNode rootForTokens = roots.get(newNode.getTags());
                GResultOf<ConfigNode> mergedNode = MergeNodes.mergeNodes("", lexer, rootForTokens, newNode.getConfigNode());

                if (mergedNode.hasResults()) {
                    roots.put(newNode.getTags(), mergedNode.results());
                }

                errors.addAll(mergedNode.getErrors());
            }

            errors.addAll(validateNode(roots.get(newNode.getTags())));
            errors = errors.stream().filter(CollectionUtils.distinctBy(ValidationError::description)).collect(Collectors.toList());


            return resultOf(roots.get(newNode.getTags()), errors);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    @SuppressWarnings("ModifyCollectionInEnhancedForLoop")
    public GResultOf<Boolean> postProcess(List<ConfigNodeProcessor> configNodeProcessors) throws GestaltException {
        if (configNodeProcessors == null) {
            throw new GestaltException("No postProcessors provided");
        }

        if (configNodeProcessors.isEmpty()) {
            return GResultOf.result(true);
        }

        long stamp = lock.readLock();
        try {
            boolean ppSuccessful = true;
            List<ValidationError> errors = new ArrayList<>();

            for (Map.Entry<Tags, ConfigNode> entry : roots.entrySet()) {
                Tags tags = entry.getKey();
                ConfigNode root = entry.getValue();
                GResultOf<ConfigNode> results = postProcess("", root, configNodeProcessors);

                // If we have results we want to update the root to the new post processed config tree.
                errors.addAll(results.getErrors());
                if (results.hasResults()) {
                    boolean tryUpgradeSuccess = false;
                    while (!tryUpgradeSuccess) {
                        long ws = lock.tryConvertToWriteLock(stamp);
                        if (ws != 0L) {
                            stamp = ws;
                            tryUpgradeSuccess = true;
                            roots.put(tags, results.results());
                        } else {
                            lock.unlockRead(stamp);
                            stamp = lock.writeLock();
                        }
                    }
                } else {
                    ppSuccessful = false;
                    errors.add(new ValidationError.NodePostProcessingNoResults());
                }
            }

            return resultOf(ppSuccessful, errors);
        } finally {
            lock.unlock(stamp);
        }
    }

    private GResultOf<ConfigNode> postProcess(String path, ConfigNode node, List<ConfigNodeProcessor> configNodeProcessors) {
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
            return postProcessArray(path, (ArrayNode) currentNode, configNodeProcessors);
        } else if (currentNode instanceof MapNode) {
            return postProcessMap(path, (MapNode) currentNode, configNodeProcessors);
        } else if (currentNode instanceof LeafNode) {
            return resultOf(currentNode, errors);
        } else {
            return GResultOf.errors(new ValidationError.UnknownNodeTypePostProcess(path, node.getClass().getName()));
        }
    }

    private GResultOf<ConfigNode> postProcessArray(String path, ArrayNode node, List<ConfigNodeProcessor> configNodeProcessors) {
        int size = node.size();
        List<ValidationError> errors = new ArrayList<>();
        ConfigNode[] processedNode = new ConfigNode[size];

        for (int i = 0; i < size; i++) {
            Optional<ConfigNode> currentNodeOption = node.getIndex(i);
            if (currentNodeOption.isPresent()) {
                String nextPath = PathUtil.pathForIndex(lexer, path, i);
                GResultOf<ConfigNode> newNode = postProcess(nextPath, currentNodeOption.get(), configNodeProcessors);

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

    private GResultOf<ConfigNode> postProcessMap(String path, MapNode node, List<ConfigNodeProcessor> configNodeProcessors) {
        Map<String, ConfigNode> processedNode = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();

        for (Map.Entry<String, ConfigNode> entry : node.getMapNode().entrySet()) {
            String key = entry.getKey();
            String nextPath = PathUtil.pathForKey(lexer, path, key);
            GResultOf<ConfigNode> newNode = postProcess(nextPath, entry.getValue(), configNodeProcessors);

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

        long stamp = lock.readLock();
        try {
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
                    GResultOf<ConfigNode> mergedNode = MergeNodes.mergeNodes("", lexer, newRoot, currentNode);

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

            boolean tryUpgradeSuccess = false;
            while (!tryUpgradeSuccess) {
                long ws = lock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    stamp = ws;
                    tryUpgradeSuccess = true;
                    roots.put(reloadNode.getTags(), newRoot);
                } else {
                    lock.unlockRead(stamp);
                    stamp = lock.writeLock();
                }
            }

            return resultOf(newRoot, errors);
        } finally {
            lock.unlock(stamp);
        }
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
                String nextPath = PathUtil.pathForIndex(lexer, path, i);
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
                String nextPath = PathUtil.pathForKey(lexer, path, key);
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
        long stamp = lock.tryOptimisticRead();
        GResultOf<ConfigNode> value = navigateToNodeInternal(path, tokens, tags);

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                return navigateToNodeInternal(path, tokens, tags);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return value;
    }

    private GResultOf<ConfigNode> navigateToNodeInternal(String path, List<Token> tokens, Tags tags) {
        List<GResultOf<ConfigNode>> rootNodes = configNodeTagResolutionStrategy.rootsToSearch(roots, tags);

        // if there is only one root node.
        if (rootNodes.isEmpty()) {
            return GResultOf.errors(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "navigating to node"));
        } else if (rootNodes.size() == 1) {
            if (rootNodes.get(0).hasResults()) {
                // return the node found for the root.
                return navigateToPathForNode(path, tokens, rootNodes.get(0).results());
            } else {
                return rootNodes.get(0);
            }
        }

        GResultOf<ConfigNode> firstNode = navigateToPathForNode(path, tokens, rootNodes.get(0).results());

        return rootNodes.subList(1, rootNodes.size()).stream()
            .reduce(firstNode, (partial, element) -> {
                if (element.hasResults()) {
                    var currentNode = navigateToPathForNode(path, tokens, element.results());

                    if (currentNode.hasResults() && partial != null && partial.hasResults()) {
                        return MergeNodes.mergeNodes(path, lexer, partial.results(), currentNode.results());
                    } else if (partial != null && partial.hasResults()) {
                        return partial;
                    } else if (currentNode.hasResults()) {
                        return currentNode;
                    }
                }
                return partial;
            });
    }


    private GResultOf<ConfigNode> navigateToPathForNode(String path, List<Token> tokens, ConfigNode currentNode) {
        List<ValidationError> errors = new ArrayList<>();

        ConfigNode nextNode = currentNode;

        for (Token token : tokens) {
            GResultOf<ConfigNode> result = navigateToNextNode(path, token, nextNode);

            // if there are errors, add them to the error list and do not add the merge results
            if (result.hasErrors()) {
                return GResultOf.errors(result.getErrors());
            }

            if (result.hasResults()) {
                nextNode = result.results();
            } else {
                errors.add(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "navigating to node"));
            }
        }

        return resultOf(nextNode, errors);
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
        long stamp = lock.readLock();
        try {
            return roots.get(tags).printer("", secretConcealer, lexer);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public String debugPrintRoot(SecretConcealer secretConcealer) {
        long stamp = lock.readLock();
        try {
            return roots.entrySet()
                .stream()
                .map((it) -> "tags: " + it.getKey() + " = " + it.getValue().printer("", secretConcealer, lexer))
                .collect(Collectors.joining("\n"));
        } finally {
            lock.unlockRead(stamp);
        }
    }
}
