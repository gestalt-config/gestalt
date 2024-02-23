package org.github.gestalt.config.builder;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reload.ConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.tag.Tag;
import org.github.gestalt.config.tag.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all source builders.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public abstract class SourceBuilder<SELF extends SourceBuilder<SELF, T>, T extends ConfigSource> { //NOPMD
    protected final List<ConfigReloadStrategy> configReloadStrategies = new ArrayList<>();
    protected ConfigSource source;
    protected Tags tags = Tags.of();

    /**
     * Get the tags for the builder.
     *
     * @return the tags for the builder
     */
    public Tags getTags() {
        return tags;
    }

    /**
     * Set the tags for the builder.
     *
     * @param tags the tags for the builder.
     * @return the builder
     */
    public SELF setTags(Tags tags) {
        Objects.requireNonNull(tags, "tags must not be null");
        this.tags = tags;
        return self();
    }

    /**
     * Add a tag to the  builder.
     *
     * @param tag tag to add to the builder
     * @return the builder
     */
    public SELF addTag(Tag tag) {
        Objects.requireNonNull(tag, "tag must not be null");
        if (tags == null) {
            tags = Tags.of(tag);
        } else {
            var existingTags = tags.getInternalTags();
            existingTags.add(tag);
            tags = Tags.of(existingTags);
        }
        return self();
    }

    /**
     * Get the list of ConfigReloadStrategyBuilder for the builder.
     *
     * @return list of ConfigReloadStrategyBuilder for the builder.
     */
    public List<ConfigReloadStrategy> getConfigReloadStrategies() {
        return configReloadStrategies;
    }

    /**
     * Add a ConfigReloadStrategyBuilder to the builder.
     *
     * @param configReloadStrategy ConfigReloadStrategyBuilder to add to the builder
     * @return the builder
     */
    public SELF addConfigReloadStrategy(ConfigReloadStrategy configReloadStrategy) {
        Objects.requireNonNull(configReloadStrategy, "Config reloads strategy builder must not be null");
        configReloadStrategies.add(configReloadStrategy);
        return self();
    }

    /**
     * Build the ConfigSourcePackage with the config source, tags and any reload strategies.
     *
     * @return the ConfigSourcePackage with the config source, tags and any reload strategies.
     * @throws GestaltException exceptions if any of the required properties are not set.
     */
    public abstract ConfigSourcePackage build() throws GestaltException;

    protected ConfigSourcePackage buildPackage(ConfigSource source) throws GestaltException {

        List<ConfigReloadStrategy> reloadStrategies = new ArrayList<>(configReloadStrategies.size());
        for (var reloadStrategy : configReloadStrategies) {
            reloadStrategy.setSource(source);
            reloadStrategies.add(reloadStrategy);
        }
        return new ConfigSourcePackage(source, reloadStrategies);
    }

    @SuppressWarnings("unchecked")
    protected final SELF self() {
        return (SELF) this;
    }
}
