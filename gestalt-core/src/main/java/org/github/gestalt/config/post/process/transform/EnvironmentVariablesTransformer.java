package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Allows you to inject Environment Variables into leaf values that match ${envVar:key},
 * where the key is used to lookup into the Environment Variables.
 *
 * @author Colin Redmond
 */
public class EnvironmentVariablesTransformer implements Transformer {
    @Override
    public String name() {
        return "envVar";
    }

    @Override
    public ValidateOf<String> process(String path, String key) {
        if (System.getenv(key) == null) {
            return ValidateOf.inValid(new ValidationError.NoEnvironmentVariableFoundPostProcess(path, key));
        } else {
            return ValidateOf.valid(System.getenv(key));
        }
    }
}
