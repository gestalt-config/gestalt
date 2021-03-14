package org.github.gestalt.config.post.process.transform;

import java.util.Optional;

public class SystemTransformer implements Transformer {
    public SystemTransformer() {
    }

    @Override
    public String name() {
        return "sys";
    }

    @Override
    public Optional<String> process(String path, String key) {
        if(!System.getProperties().containsKey(key)) {
            return Optional.empty();
        } else {
            return Optional.of(System.getProperties().get(key).toString());
        }
    }
}
