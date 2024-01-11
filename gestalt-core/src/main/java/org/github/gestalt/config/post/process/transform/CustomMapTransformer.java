package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows you to provide a custom map to inject into leaf values that match ${map:key}, where the key is used to lookup into the map.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
@ConfigPriority(400)
public final class CustomMapTransformer implements Transformer {
    private final Map<String, String> replacementVars;

    /**
     * Default CustomMapTransformer that will not replace any values as it will have an empty map.
     */
    public CustomMapTransformer() {
        this.replacementVars = new HashMap<>();
    }

    /**
     * CustomMapTransformer that will replace any values in the map while processing.
     *
     * @param replacementVars values to replace
     */
    public CustomMapTransformer(Map<String, String> replacementVars) {
        this.replacementVars = replacementVars;
    }

    @Override
    public String name() {
        return "map";
    }

    @Override
    public ValidateOf<String> process(String path, String key, String rawValue) {
        if (replacementVars.containsKey(key)) {
            return ValidateOf.valid(replacementVars.get(key));
        } else {
            return ValidateOf.inValid(new ValidationError.NoCustomPropertyFoundPostProcess(path, key));
        }
    }
}
