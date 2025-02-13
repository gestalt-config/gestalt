package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows you to provide a custom map to inject into leaf values that match ${map:key}, where the key is used to lookup into the map.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class TestCustomMapTransformer implements Transformer {
    private final Map<String, String> replacementVars;

    /**
     * Default CustomMapTransformer that will not replace any values as it will have an empty map.
     */
    public TestCustomMapTransformer() {
        this.replacementVars = new HashMap<>();
    }

    /**
     * CustomMapTransformer that will replace any values in the map while processing.
     *
     * @param replacementVars values to replace
     */
    public TestCustomMapTransformer(Map<String, String> replacementVars) {
        this.replacementVars = replacementVars;
    }

    @Override
    public String name() {
        return "map";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (replacementVars.containsKey(key)) {
            return GResultOf.result(replacementVars.get(key));
        } else {
            return GResultOf.errors(new ValidationError.NoCustomPropertyFoundPostProcess(path, key));
        }
    }
}
