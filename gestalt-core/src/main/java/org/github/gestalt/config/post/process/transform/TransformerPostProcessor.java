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

/**
 * A Post Processor used to replace leaf values that have the format ${transform:key} with a new value.
 * The transform represents the source of the data, such as envVar for Environment Variables.
 * The Key is how we look up the data in the data source, such as an Environment Variable JAVA_HOME
 *
 * <p>So you could have a leaf value "hello ${envVar:USER_NAME} you are level ${envVar:USER_LEVEL}!" where USER_NAME=john and USER_LEVEL=6,
 * will get transformed into "hello john you are level 6!"
 *
 * @author Colin Redmond
 */
public class TransformerPostProcessor implements PostProcessor {
    private static final Pattern pattern = Pattern.compile("\\$\\{(?<transform>\\w+):(?<key>\\S+)}");

    private final Map<String, Transformer> transformers;

    // by default use the service loader to
    public TransformerPostProcessor() {
        this.transformers = new HashMap<>();
        ServiceLoader<Transformer> loader = ServiceLoader.load(Transformer.class);
        loader.forEach(it -> transformers.put(it.name(), it));
    }

    public TransformerPostProcessor(List<Transformer> transformers) {
        if (transformers == null) {
            this.transformers = Collections.emptyMap();
        } else {
            this.transformers = transformers.stream().collect(Collectors.toMap(Transformer::name, Function.identity()));
        }
    }

    public void addTransformers(List<Transformer> transformers) {
        if (transformers != null) {
            transformers.forEach(it -> this.transformers.put(it.name(), it));
        }
    }

    public void applyConfig(PostProcessorConfig config) {
        this.transformers.values().forEach(it -> it.applyConfig(config));
    }

    @Override
    public ValidateOf<ConfigNode> process(String path, ConfigNode currentNode) {
        if (!(currentNode instanceof LeafNode) || !currentNode.getValue().isPresent()) {
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

            if (transformers.containsKey(transformName)) {
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
        }
        // add any text at the end of the sentence
        if (lastIndex < leafValue.length()) {
            newLeafValue.append(leafValue.subSequence(lastIndex, leafValue.length()));
        }

        return ValidateOf.valid(new LeafNode(newLeafValue.toString()));
    }
}
