package org.github.gestalt.config.entity;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.tag.Tags;

import java.util.Objects;

/**
 * Holds a config node and the UUID related to the source.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class ConfigNodeContainer {
    private final ConfigNode configNode;
    private final ConfigSource source;

    /**
     * Constructor to hold a ConfigNode and a id.
     *
     * @param configNode node to hold
     * @param source the source of the configs
     */
    public ConfigNodeContainer(ConfigNode configNode, ConfigSource source) {
        this.configNode = configNode;
        this.source = source;
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
    public ConfigSource getSource() {
        return source;
    }

    /**
     * Get all tags associated with a node.
     *
     * @return Tags
     */
    public Tags getTags() {
        return source.getTags();
    }

    /**
     * returns true if the tokens for the Config Node match the input.
     *
     * @param match tokens to match
     * @return true if the tokens for the Config Node match the input
     */
    public boolean matchesTokens(Tags match) {
        return source.getTags().equals(match);
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
        return Objects.equals(configNode, that.configNode) && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configNode, source);
    }
}
