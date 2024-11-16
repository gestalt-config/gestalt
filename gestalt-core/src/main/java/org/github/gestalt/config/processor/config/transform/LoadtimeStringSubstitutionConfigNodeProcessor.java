package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;
import java.util.Objects;

/**
 * A Config Node Processor used to replace leaf values that have the format ${transform:key} with a new value.
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
@ConfigPriority(200)
public final class LoadtimeStringSubstitutionConfigNodeProcessor implements ConfigNodeProcessor {

    private StringSubstitutionProcessor stringSubstitutionProcessor;

    public LoadtimeStringSubstitutionConfigNodeProcessor() {

    }


    /**
     * Creates a TransformerPostProcessor with a list of transformers.
     *
     * @param transformers list of transformers to use
     */
    public LoadtimeStringSubstitutionConfigNodeProcessor(List<Transformer> transformers) {
        this.stringSubstitutionProcessor = new StringSubstitutionProcessor(Objects.requireNonNullElseGet(transformers, List::of));
    }

    @Override
    public void applyConfig(ConfigNodeProcessorConfig config) {
        stringSubstitutionProcessor = new StringSubstitutionProcessor(config, config.getConfig().getSubstitutionOpeningToken(),
            config.getConfig().getSubstitutionClosingToken());
    }

    @Override
    public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
        var valueOptional = currentNode.getValue();
        if (!(currentNode instanceof LeafNode) || valueOptional.isEmpty()) {
            return GResultOf.result(currentNode);
        }

        if (stringSubstitutionProcessor != null) {
            return stringSubstitutionProcessor.process(path, currentNode);
        } else {
            return GResultOf.result(currentNode);
        }
    }
}
