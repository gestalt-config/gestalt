package org.config.gestalt.node;

import java.util.Optional;

public interface ConfigNode {
    NodeType getNodeType();

    Optional<String> getValue();

    Optional<ConfigNode> getIndex(int index);

    Optional<ConfigNode> getKey(String key);

    int size();
}
