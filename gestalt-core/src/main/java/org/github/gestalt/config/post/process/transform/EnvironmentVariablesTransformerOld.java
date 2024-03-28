package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Allows you to inject Environment Variables into leaf values that match ${envVar:key},
 * where the key is used to lookup into the Environment Variables.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 * @deprecated use {@link EnvironmentVariablesTransformer}
 */
@ConfigPriority(100)
@Deprecated(since = "0.20.1", forRemoval = true)
public final class EnvironmentVariablesTransformerOld implements Transformer {
    private static final System.Logger logger = System.getLogger(EnvironmentVariablesTransformerOld.class.getName());

    @Override
    public String name() {
        return "envVar";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (key == null) {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        } else if (System.getenv(key) == null) {
            return GResultOf.errors(new ValidationError.NoEnvironmentVariableFoundPostProcess(path, key));
        } else {
            // this class has been depricated a while, however since it is not directly exposed, no one would know that.
            // start logging warnings, so we can comfortably delete it later.
            logger.log(System.Logger.Level.WARNING,
                "String substitutions using \"envVar\" is deprecated for removal, please use \"env\"");
            return GResultOf.result(System.getenv(key));
        }
    }
}
