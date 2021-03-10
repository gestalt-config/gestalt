package org.github.gestalt.config.post.process.transform;

import java.util.Optional;

public interface Transformer {
    String name();

    Optional<String> process(String path, String key);
}
