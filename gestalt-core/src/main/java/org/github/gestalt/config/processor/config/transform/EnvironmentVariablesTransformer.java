package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.SystemWrapper;

/**
 * Allows you to inject Environment Variables into leaf values that match ${env:key},
 * where the key is used to lookup into the Environment Variables.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@ConfigPriority(100)
public final class EnvironmentVariablesTransformer implements Transformer {
    @Override
    public String name() {
        return "env";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (key == null) {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        } else if (SystemWrapper.getEnvVars(key) == null) {
            return GResultOf.errors(new ValidationError.NoEnvironmentVariableFoundPostProcess(path, key));
        } else {
            return GResultOf.result(SystemWrapper.getEnvVars(key));
        }
    }
}
