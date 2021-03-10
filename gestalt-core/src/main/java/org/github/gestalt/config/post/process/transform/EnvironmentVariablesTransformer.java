package org.github.gestalt.config.post.process.transform;

public class EnvironmentVariablesTransformer implements Transformer {
    @Override
    public String name() {
        return "envVar";
    }

    @Override
    public String process(String path, String key) {
        return System.getenv(key);
    }
}
