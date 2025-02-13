package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.SystemWrapper;

/**
 * Allows you to inject System Properties into leaf values that match ${envVar:key},
 * where the key is used to lookup into the Environment Variables.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@ConfigPriority(200)
public final class SystemPropertiesTransformer implements Transformer {
    @Override
    public String name() {
        return "sys";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (key == null) {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        } else if (!SystemWrapper.getProperties().containsKey(key)) {
            return GResultOf.errors(new ValidationError.NoSystemPropertyFoundPostProcess(path, key));
        } else {
            return GResultOf.result(SystemWrapper.getProperties().get(key).toString());
        }
    }
}
