package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.metadata.IsRunTimeStringSubstitutionMetadata;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.processor.config.RunTimeConfigNodeProcessor;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;
import java.util.Objects;

/**
 * A Config Node Processor that is executed at runtime used to replace leaf values that have the format #{transform:key} with a new value.
 * When using runtime string replacement it will scan each time a config is retrieved. The results will not be cached.
 * So there is a performance overhead to this feature.
 *
 * <p>The transform represents the source of the data, such as envVar for Environment Variables.
 * The Key is how we look up the data in the data source, such as an Environment Variable JAVA_HOME
 *
 * <p>So you could have a leaf value "hello #{envVar:USER_NAME} you are level #{envVar:USER_LEVEL}!" where USER_NAME=john and USER_LEVEL=6,
 * will get transformed into "hello john you are level 6!"
 *
 * <p>You do not need to specify the transform name, so it will search for the value in all default transformers based on priority.
 * It will then return the first value found.
 * So in the above example you can also use "hello #{USER_NAME} you are level #{USER_LEVEL}!" and it will find the values in the Env Vars.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
@ConfigPriority(200)
public final class RunTimeStringSubstitutionConfigNodeProcessor implements RunTimeConfigNodeProcessor {

    private StringSubstitutionProcessor stringSubstitutionProcessor;

    public RunTimeStringSubstitutionConfigNodeProcessor() {

    }

    /**
     * Creates a TransformerPostProcessor with a list of transformers.
     *
     * @param transformers list of transformers to use
     */
    public RunTimeStringSubstitutionConfigNodeProcessor(List<Transformer> transformers) {
        this.stringSubstitutionProcessor = new StringSubstitutionProcessor(Objects.requireNonNullElseGet(transformers, List::of));
    }

    @Override
    public void applyConfig(ConfigNodeProcessorConfig config) {
        stringSubstitutionProcessor = new StringSubstitutionProcessor(config, config.getConfig().getRunTimeSubstitutionOpeningToken(),
            config.getConfig().getRunTimeSubstitutionClosingToken());
    }

    @Override
    public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
        if (!(currentNode instanceof LeafNode) ||
            !currentNode.getMetadata().containsKey(IsRunTimeStringSubstitutionMetadata.RUN_TIME_STRING_SUBSTITUTION)) {
            return GResultOf.result(currentNode);
        }

        var valueOptional = ((LeafNode) currentNode).getValueInternal();
        if (valueOptional.isEmpty()) {
            return GResultOf.result(currentNode);
        }

        if (stringSubstitutionProcessor != null) {
            return stringSubstitutionProcessor.process(path, currentNode);
        } else {
            return GResultOf.result(currentNode);
        }
    }
}
