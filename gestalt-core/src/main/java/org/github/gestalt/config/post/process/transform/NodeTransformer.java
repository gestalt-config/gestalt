package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows you to provide a custom map to inject into leaf values that match ${map:key}, where the key is used to lookup into the map.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
@ConfigPriority(300)
public final class NodeTransformer implements Transformer {
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
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (config == null) {
            return GResultOf.errors(new ValidationError.NodePostProcessingConfigMissing(path, key));
        } else if (key == null) {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }

        String normalizedPath = config.getLexer().normalizeSentence(key);
        GResultOf<List<Token>> resultsOf = config.getLexer().scan(normalizedPath);

        List<ValidationError> errors = new ArrayList<>(resultsOf.getErrors());
        if (resultsOf.hasErrors(ValidationLevel.ERROR)) {
            errors.add(new ValidationError.NodePostProcessingBadTokens(path, key));
            return GResultOf.errors(errors);
        }

        if (!resultsOf.hasResults()) {
            errors.add(new ValidationError.NodePostProcessingNoResultsForTokens(path, key));
            return GResultOf.errors(errors);
        }

        // TODO figure out how to support tags in the NodeTransformer. maybe ${node:my.path[tagKey,tagValue]}
        GResultOf<ConfigNode> resultOfConfigNode = config.getConfigNodeService()
            .navigateToNode(path, resultsOf.results(), Tags.of());

        errors.addAll(resultOfConfigNode.getErrors());
        if (resultOfConfigNode.hasErrors(ValidationLevel.MISSING_VALUE)) {
            errors.add(new ValidationError.NodePostProcessingErrorsNavigatingToNode(path, key));
            return GResultOf.errors(errors);
        }

        if (!resultOfConfigNode.hasResults()) {
            errors.add(new ValidationError.NoResultsFoundForNode(path, key, "NodeTransformer"));
            return GResultOf.errors(errors);
        }

        ConfigNode node = resultOfConfigNode.results();
        if (!(node instanceof LeafNode)) {
            errors.add(new ValidationError.NodePostProcessingNodeNotLeaf(path, key));
            return GResultOf.errors(errors);
        }

        var valueOptional = node.getValue();
        if (valueOptional.isEmpty()) {
            errors.add(new ValidationError.NodePostProcessingNodeLeafHasNoValue(path, key));
            return GResultOf.errors(errors);
        }

        return GResultOf.result(valueOptional.get());
    }
}
