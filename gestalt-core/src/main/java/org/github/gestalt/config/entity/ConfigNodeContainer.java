package org.github.gestalt.config.entity;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.tag.Tags;

import java.util.Objects;

/**
 * Holds a config node and the UUID related to the source.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ConfigNodeContainer {
    private final ConfigNode configNode;
    private final ConfigSource source;
    private final Tags tags;

    /**
     * Constructor to hold a ConfigNode and a id.
     *
     * @param configNode node to hold
     * @param source     the source of the configs
     * @param tags       tags associated with the source.
     */
    public ConfigNodeContainer(ConfigNode configNode, ConfigSource source, Tags tags) {
        this.configNode = configNode;
        this.source = source;
        this.tags = tags;
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
        return tags;
    }

    /**
     * returns true if the tags for the Config Node match the input.
     *
     * @param match tokens to match
     * @return true if the tags for the Config Node match the input
     */
    public boolean matchesTags(Tags match) {
        return tags.equals(match);
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
        return Objects.equals(configNode, that.configNode) && Objects.equals(source, that.source) && Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configNode, source, tags);
    }
}
