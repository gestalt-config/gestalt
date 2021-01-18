package org.config.gestalt.entity;

import org.config.gestalt.node.ConfigNode;

import java.util.Objects;
import java.util.UUID;

public class ConfigNodeContainer {
    private final ConfigNode configNode;
    private final UUID id;

    public ConfigNodeContainer(ConfigNode configNode, UUID id) {
        this.configNode = configNode;
        this.id = id;
    }

    public ConfigNode getConfigNode() {
        return configNode;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigNodeContainer)) return false;
        ConfigNodeContainer that = (ConfigNodeContainer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
