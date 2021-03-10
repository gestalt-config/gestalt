package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.node.ConfigNode;

public interface Transformer {
    String name();
    String process(String path, String key);
}
