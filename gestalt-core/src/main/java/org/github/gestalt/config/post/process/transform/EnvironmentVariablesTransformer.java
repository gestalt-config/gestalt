package org.github.gestalt.config.post.process.transform;

import java.util.Optional;

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
