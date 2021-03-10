package org.github.gestalt.config.post.process;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.utils.ValidateOf;

public interface PostProcessor {
    ValidateOf<ConfigNode> process(String path, ConfigNode currentNode);
}
