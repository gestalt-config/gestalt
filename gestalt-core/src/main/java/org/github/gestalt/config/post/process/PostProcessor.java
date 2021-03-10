package org.github.gestalt.config.post.process;

import org.github.gestalt.config.node.ConfigNode;

public interface PostProcessor {
    ConfigNode process(String path, ConfigNode currentNode);
}
