package org.github.gestalt.config.post.process.transform;

import java.util.Optional;

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
    public Optional<String> process(String path, String key) {
        return Optional.ofNullable(System.getenv(key));
    }
}
