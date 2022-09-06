package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows you to provide a custom map to inject into leaf values that match ${map:key}, where the key is used to lookup into the map.
 *
 * @author Colin Redmond
 */
@ConfigPriority(300)
public class NodeTransformer implements Transformer {
    private PostProcessorConfig config;

    @Override
    public String name() {
        return "node";
    }

    @Override
    public void applyConfig(PostProcessorConfig config) {
        this.config = config;
    }

    @Override
    public ValidateOf<String> process(String path, String key) {
        if (config == null) {
            return ValidateOf.inValid(new ValidationError.NodePostProcessingConfigMissing(path, key));
        }

        String normalizedPath = config.getLexer().normalizeSentence(key);
        ValidateOf<List<Token>> validateOfTokens = config.getLexer().scan(normalizedPath);

        List<ValidationError> errors = new ArrayList<>(validateOfTokens.getErrors());
        if (validateOfTokens.hasErrors(ValidationLevel.ERROR)) {
            errors.add(new ValidationError.NodePostProcessingBadTokens(path, key));
            return ValidateOf.inValid(errors);
        }

        if (!validateOfTokens.hasResults()) {
            errors.add(new ValidationError.NodePostProcessingNoResultsForTokens(path, key));
            return ValidateOf.inValid(errors);
        }

        // TODO figure out how to support tags in the NodeTransformer. maybe ${node:my.path[tagKey,tagValue]}
        ValidateOf<ConfigNode> validateOfConfigNode = config.getConfigNodeService()
                                                            .navigateToNode(path, validateOfTokens.results(), Tags.of());

        errors.addAll(validateOfConfigNode.getErrors());
        if (validateOfConfigNode.hasErrors(ValidationLevel.ERROR)) {
            errors.add(new ValidationError.NodePostProcessingErrorsNavigatingToNode(path, key));
            return ValidateOf.inValid(errors);
        }

        if (!validateOfConfigNode.hasResults()) {
            errors.add(new ValidationError.NoResultsFoundForNode(path, key, "NodeTransformer"));
            return ValidateOf.inValid(errors);
        }

        ConfigNode node = validateOfConfigNode.results();
        if (!(node instanceof LeafNode)) {
            errors.add(new ValidationError.NodePostProcessingNodeNotLeaf(path, key));
            return ValidateOf.inValid(errors);
        }

        if (node.getValue().isEmpty()) {
            errors.add(new ValidationError.NodePostProcessingNodeLeafHasNoValue(path, key));
            return ValidateOf.inValid(errors);
        }

        return ValidateOf.valid(node.getValue().get());
    }
}
