package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.github.gestalt.config.utils.ValidateOf;

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
 * @author Colin Redmond
 */
public class TransformerPostProcessor implements PostProcessor {
    private static final Pattern pattern = Pattern.compile("\\$\\{((?<transform>\\w+):)?(?<key>\\S+)}");

    private final Map<String, Transformer> transformers;
    private final List<Transformer> orderedDefaultTransformers;

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
    }


    @Override
    public void applyConfig(PostProcessorConfig config) {
        this.transformers.values().forEach(it -> it.applyConfig(config));
    }

    @Override
    public ValidateOf<ConfigNode> process(String path, ConfigNode currentNode) {
        if (!(currentNode instanceof LeafNode) || currentNode.getValue().isEmpty()) {
            return ValidateOf.valid(currentNode);
        }

        String leafValue = currentNode.getValue().get();
        Matcher matcher = pattern.matcher(leafValue);
        StringBuilder newLeafValue = new StringBuilder();
        int lastIndex = 0;
        while (matcher.find()) {
            String transformName = matcher.group("transform");
            String key = matcher.group("key");
            int startOfMatch = matcher.start();
            int endOfMatch = matcher.end();

            // if we have a named transform look it up in the map.
            if (transformName != null) {
                if(transformers.containsKey(transformName)) {
                    ValidateOf<String> value = transformers.get(transformName).process(path, key);
                    if (value.hasResults()) {
                        newLeafValue.append(leafValue.subSequence(lastIndex, startOfMatch)).append(value.results());
                        lastIndex = endOfMatch;

                    } else {
                        return ValidateOf.inValid(new ValidationError.NoKeyFoundForTransform(path, transformName, key));
                    }
                } else {
                    return ValidateOf.inValid(new ValidationError.NoMatchingTransformFound(path, transformName));
                }
            } else {
                boolean foundTransformer = false;
                // if the transform isn't named look for it in priority order.
                for(Transformer transform: orderedDefaultTransformers) {
                    ValidateOf<String> value = transform.process(path, key);
                    if (value.hasResults()) {
                        newLeafValue.append(leafValue.subSequence(lastIndex, startOfMatch)).append(value.results());
                        lastIndex = endOfMatch;
                        foundTransformer = true;
                        break;
                    }
                }

                if(!foundTransformer) {
                    return ValidateOf.inValid(new ValidationError.NoMatchingDefaultTransformFound(path));
                }
            }
        }
        // add any text at the end of the sentence
        if (lastIndex < leafValue.length()) {
            newLeafValue.append(leafValue.subSequence(lastIndex, leafValue.length()));
        }

        return ValidateOf.valid(new LeafNode(newLeafValue.toString()));
    }
}
