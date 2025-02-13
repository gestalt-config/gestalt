package org.github.gestalt.config.node.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating a finding a config node from a set of parameters.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class ConfigNodeImportFactory implements ConfigNodeFactory {

    public static final String SOURCE_TYPE = "node";
    public static final String PARAMETER_RESOURCE = "path";

    private ConfigNodeService configNodeService;
    private SentenceLexer lexer;

    @Override
    public void applyConfig(ConfigNodeFactoryConfig config) {
        this.configNodeService = config.getConfigNodeService();
        this.lexer = config.getLexer();
    }

    @Override
    public Boolean supportsType(String type) {
        return SOURCE_TYPE.equalsIgnoreCase(type);
    }

    @Override
    public GResultOf<List<ConfigNode>> build(Map<String, String> parameters) {

        List<ValidationError> errors = new ArrayList<>();
        String path = null;
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (PARAMETER_RESOURCE.equalsIgnoreCase(entry.getKey())) {
                    path = entry.getValue();
                } else {
                    errors.add(
                        new ValidationError.ConfigSourceFactoryUnknownParameter(SOURCE_TYPE, entry.getKey(), entry.getValue()));
                }
            }
            if (path == null) {
                errors.add(new ValidationError.ConfigNodeImportNodeEmpty(path));
                return GResultOf.errors(errors);
            }

            var pathTokens = lexer.scan(path);

            if (!pathTokens.hasResults()) {
                errors.add(new ValidationError.ConfigNodeImportNodeEmpty(path));
                return GResultOf.errors(errors);
            }

            var foundNode = configNodeService.navigateToNode(path, pathTokens.results(), Tags.of());

            return foundNode.mapWithError(List::of);

        } catch (Exception ex) {
            errors.add(new ValidationError.ConfigSourceFactoryException(SOURCE_TYPE, ex));
            return GResultOf.errors(errors);
        }
    }
}
