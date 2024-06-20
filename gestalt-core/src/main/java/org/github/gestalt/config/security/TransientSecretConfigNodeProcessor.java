package org.github.gestalt.config.security;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.utils.GResultOf;

@ConfigPriority(200)
public class TransientSecretConfigNodeProcessor implements ConfigNodeProcessor {
    @Override
    public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
        return null;
    }
}
