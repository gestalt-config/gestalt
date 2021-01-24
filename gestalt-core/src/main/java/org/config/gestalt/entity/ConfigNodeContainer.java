package org.config.gestalt.entity;

import org.config.gestalt.node.ConfigNode;

import java.util.Objects;
import java.util.UUID;

/**
 * Holds a config node and the UUID related to the source.
 *
 * @author Colin Redmond
 */
public class ConfigNodeContainer {
    private final ConfigNode configNode;
    private final UUID id;

    public ConfigNodeContainer(ConfigNode configNode, UUID id) {
        this.configNode = configNode;
        this.id = id;
    }

    /**
     * Return the config node.
     *
     * @return config node
     */
    public ConfigNode getConfigNode() {
        return configNode;
    }

    /**
     * unique ID for the config node related to the source.
     *
     * @return unique ID for the config node related to the source
     */
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigNodeContainer)) {
            return false;
        }
        ConfigNodeContainer that = (ConfigNodeContainer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
