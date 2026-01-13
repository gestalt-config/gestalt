package org.github.gestalt.dotenv.transform;

import io.github.cdimascio.dotenv.Dotenv;
import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.processor.config.transform.Transformer;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.dotenv.config.DotenvModuleConfig;
import org.github.gestalt.dotenv.errors.DotenvErrors;

/**
 * Allows you to inject Dot Env values into leaf values that match ${dotEnv:key},
 * where the key is used to lookup into the Dot Environment Variables.
 * <p>
 * If you want to use the dot env transformer, you need to setup the module config with gestalt.
 * </p>
 * Has i higher priority than System Properties transformer to allow overriding of system properties.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@ConfigPriority(210)
public final class DotenvPropertiesTransformer implements Transformer {

    private Dotenv dotenv;

    @Override
    public String name() {
        return "dotEnv";
    }

    @Override
    public void applyConfig(ConfigNodeProcessorConfig config) {
        DotenvModuleConfig moduleConfig = config.getConfig().getModuleConfig(DotenvModuleConfig.class);

        // get the project id from the module config, or use the default
        if (moduleConfig != null) {
            dotenv = moduleConfig.getDotenv();
        }
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (key == null) {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        } else if (dotenv == null) {
            return GResultOf.errors(new DotenvErrors.DotenvNotConfiguredPostProcess(path, key));
        } else if (dotenv.get(key) == null) {
            return GResultOf.errors(new DotenvErrors.NoDotenvPropertyFoundPostProcess(path, key));
        } else {
            return GResultOf.result(dotenv.get(key));
        }
    }
}
