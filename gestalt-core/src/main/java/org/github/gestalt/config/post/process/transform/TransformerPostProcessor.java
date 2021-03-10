package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.post.process.PostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Post Processor used to replace leaf values that have the format ${transform:key} with a new value.
 * The transform represents the source of the data, such as envVar for Environment Variables.
 * The Key is how we look up the data in the data source, such as an Environment Variable JAVA_HOME
 * <p>
 * So you could have a leaf value "hello ${envVar:USER_NAME}" where USER_NAME is colin, will get transformed into "hello Colin"
 *
 * @author Colin Redmond
 */
public class TransformerPostProcessor implements PostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TransformerPostProcessor.class.getName());

    private static final Pattern pattern = Pattern.compile("\\${(?<transform>\\w*):(?<key>\\w*)}");

    private final Map<String, Transformer> transformers;

    public TransformerPostProcessor(List<Transformer> transformers) {
        if (transformers == null) {
            this.transformers = Collections.emptyMap();
        } else {
            this.transformers = transformers.stream().collect(Collectors.toMap(Transformer::name, Function.identity()));
        }
    }

    @Override
    public ConfigNode process(String path, ConfigNode currentNode) {
        if (!(currentNode instanceof LeafNode) || !currentNode.getValue().isPresent()) {
            return currentNode;
        }

        String leafValue = currentNode.getValue().get();
        Matcher matcher = pattern.matcher(leafValue);
        if (matcher.matches()) {
            String transformName = matcher.group("transform");
            String key = matcher.group("key");

            if (transformers.containsKey(transformName)) {
                String value = transformers.get(transformName).process(path, key);

                String newLeafValue = matcher.replaceAll(value);

                return new LeafNode(newLeafValue);
            } else {
                logger.info("Unable to find matching transform for " + path + " with leaf value " + leafValue +
                    " make sure you registered all expected transforms");
            }
        }

        return currentNode;
    }
}
