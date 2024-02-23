package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.github.gestalt.config.post.process.transform.substitution.SubstitutionNode;
import org.github.gestalt.config.post.process.transform.substitution.SubstitutionTreeBuilder;
import org.github.gestalt.config.utils.GResultOf;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.github.gestalt.config.utils.CollectionUtils.buildOrderedConfigPriorities;

/**
 * A Post Processor used to replace leaf values that have the format ${transform:key} with a new value.
 * The transform represents the source of the data, such as envVar for Environment Variables.
 * The Key is how we look up the data in the data source, such as an Environment Variable JAVA_HOME
 *
 * <p>So you could have a leaf value "hello ${envVar:USER_NAME} you are level ${envVar:USER_LEVEL}!" where USER_NAME=john and USER_LEVEL=6,
 * will get transformed into "hello john you are level 6!"
 *
 * <p>You do not need to specify the transform name, so it will search for the value in all default transformers based on priority.
 * It will then return the first value found.
 * So in the above example you can also use "hello ${USER_NAME} you are level ${USER_LEVEL}!" and it will find the values in the Env Vars.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class TransformerPostProcessor implements PostProcessor {

    public static final String defaultSubstitutionRegex =
        "^((?<transform>\\w+):(?!=))?(?<key>.+?)(:=(?<default>.*))?$";
    private final Map<String, Transformer> transformers;
    private final List<Transformer> orderedDefaultTransformers;
    private Pattern pattern;
    private int maxRecursionDepth = 5;
    private SubstitutionTreeBuilder substitutionTreeBuilder;

    /**
     * By default, use the service loader to load all Transformer classes.
     */
    public TransformerPostProcessor() {
        transformers = new HashMap<>();
        List<Transformer> transformersList = new ArrayList<>();
        ServiceLoader<Transformer> loader = ServiceLoader.load(Transformer.class);
        loader.forEach(it -> {
            transformers.put(it.name(), it);
            transformersList.add(it);
        });

        this.orderedDefaultTransformers = buildOrderedConfigPriorities(transformersList, false);
        this.pattern = Pattern.compile(defaultSubstitutionRegex);
    }

    /**
     * Creates a TransformerPostProcessor with a list of transformers.
     *
     * @param transformers list of transformers to use
     */
    public TransformerPostProcessor(List<Transformer> transformers) {
        if (transformers == null) {
            this.transformers = Collections.emptyMap();
            this.orderedDefaultTransformers = List.of();
        } else {
            this.transformers = transformers.stream().collect(Collectors.toMap(Transformer::name, Function.identity()));
            this.orderedDefaultTransformers = buildOrderedConfigPriorities(transformers, false);
        }

        this.substitutionTreeBuilder = new SubstitutionTreeBuilder("${", "}");
        this.pattern = Pattern.compile(defaultSubstitutionRegex);
    }

    @Override
    public void applyConfig(PostProcessorConfig config) {
        this.transformers.values().forEach(it -> it.applyConfig(config));

        substitutionTreeBuilder = new SubstitutionTreeBuilder(config.getConfig().getSubstitutionOpeningToken(),
            config.getConfig().getSubstitutionClosingToken());

        this.maxRecursionDepth = config.getConfig().getMaxSubstitutionNestedDepth();
        this.pattern = Pattern.compile(defaultSubstitutionRegex);
    }

    @Override
    public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
        var valueOptional = currentNode.getValue();
        if (transformers.isEmpty() || !(currentNode instanceof LeafNode) || valueOptional.isEmpty()) {
            return GResultOf.result(currentNode);
        }

        String leafValue = valueOptional.get();

        GResultOf<List<SubstitutionNode>> substitutionNodes = substitutionTreeBuilder.build(path, leafValue);
        if (substitutionNodes.hasResults()) {
            var results = buildSubstitutedStringList(path, currentNode, substitutionNodes.results(), 0);

            return results.mapWithError(LeafNode::new);

        } else {
            return GResultOf.errors(substitutionNodes.getErrors());
        }
    }

    private GResultOf<String> buildSubstitutedStringList(String path, ConfigNode originalNode, List<SubstitutionNode> nodes, int depth) {
        if (depth > maxRecursionDepth) {
            return GResultOf.errors(new ValidationError.ExceededMaximumNestedSubstitutionDepth(path, depth, originalNode));
        }

        StringBuilder result = new StringBuilder();
        List<ValidationError> errors = new ArrayList<>();
        for (SubstitutionNode resolveNode : nodes) {
            if (resolveNode instanceof SubstitutionNode.TextNode) {
                result.append(((SubstitutionNode.TextNode) resolveNode).getText());
            } else if (resolveNode instanceof SubstitutionNode.TransformNode) {
                List<SubstitutionNode> nodes1 = ((SubstitutionNode.TransformNode) resolveNode).getSubNodes();

                GResultOf<String> recursiveResults = buildSubstitutedStringList(path, originalNode, nodes1, depth + 1);
                errors.addAll(recursiveResults.getErrors());
                if (recursiveResults.hasResults()) {
                    GResultOf<String> transformedString = transformString(path, recursiveResults.results());
                    errors.addAll(transformedString.getErrors());
                    if (transformedString.hasResults()) {
                        GResultOf<List<SubstitutionNode>> substitutionNodes =
                            substitutionTreeBuilder.build(path, transformedString.results());

                        errors.addAll(substitutionNodes.getErrors());
                        if (substitutionNodes.hasResults()) {
                            GResultOf<String> nestedSub =
                                buildSubstitutedStringList(path, originalNode, substitutionNodes.results(), depth + 1);
                            errors.addAll(nestedSub.getErrors());
                            if (nestedSub.hasResults()) {
                                result.append(nestedSub.results());
                            }
                        }
                    }
                }
            } else {
                errors.add(new ValidationError.NotAValidSubstitutionNode(path, resolveNode));
            }
        }

        return GResultOf.resultOf(result.toString(), errors);
    }

    private GResultOf<String> transformString(String path, String input) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder newLeafValue = new StringBuilder();
        boolean foundMatch = false;

        while (matcher.find()) {
            String transformName = matcher.group("transform");
            String key = matcher.group("key");
            String defaultValue = matcher.group("default");

            // if we have a named transform look it up in the map.
            if (transformName != null) {
                if (transformers.containsKey(transformName)) {
                    GResultOf<String> transformValue = transformers.get(transformName).process(path, key, input);
                    if (transformValue.hasResults()) {
                        newLeafValue.append(transformValue.results());
                        foundMatch = true;
                    } else {
                        // if we have no results from the transform but a default value, use the default
                        if (defaultValue != null) {
                            foundMatch = true;
                            newLeafValue.append(defaultValue);
                        } else {
                            if (transformValue.hasErrors()) {
                                return transformValue;
                            } else {
                                return GResultOf.errors(new ValidationError.NoKeyFoundForTransform(path, transformName, key));
                            }
                        }
                    }
                } else {
                    return GResultOf.errors(new ValidationError.NoMatchingTransformFound(path, transformName));
                }
            } else {
                boolean foundTransformer = false;
                // if the transform isn't named look for it in priority order.
                for (Transformer transform : orderedDefaultTransformers) {
                    GResultOf<String> transformValue = transform.process(path, key, input);
                    if (transformValue.hasResults()) {
                        newLeafValue.append(transformValue.results());
                        foundTransformer = true;
                        foundMatch = true;
                        break;
                    }
                }

                if (!foundTransformer) {
                    // if we have no results from the transform but a default value, use the default
                    if (defaultValue != null) {
                        foundMatch = true;
                        newLeafValue.append(defaultValue);
                    } else {
                        return GResultOf.errors(new ValidationError.NoMatchingDefaultTransformFound(path, key));
                    }
                }
            }
        }

        if (foundMatch) {
            return GResultOf.result(newLeafValue.toString());
        } else {
            return GResultOf.errors(new ValidationError.TransformDoesntMatchRegex(path, input));
        }
    }
}
