package org.github.gestalt.config.micrometer.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.github.gestalt.config.entity.GestaltModuleConfig;

/**
 * Micrometer specific configuration.
 * This module requires a meter registry
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class MicrometerModuleConfig implements GestaltModuleConfig {

    private final MeterRegistry meterRegistry;
    private final boolean includePath;
    private final boolean includeClass;
    private final boolean includeOptional;
    private final boolean includeTags;
    private final String prefix;

    public MicrometerModuleConfig(MeterRegistry meterRegistry, Boolean includePath,
                                  Boolean includeClass, Boolean includeOptional, boolean includeTags,
                                  String prefix) {
        this.meterRegistry = meterRegistry;
        this.includePath = includePath;
        this.includeClass = includeClass;
        this.includeOptional = includeOptional;
        this.includeTags = includeTags;
        this.prefix = prefix;
    }

    @Override
    public String name() {
        return "micrometer";
    }

    /**
     * Get the custom micrometer meter registry.
     *
     * @return the custom micrometer meter registry.
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    /**
     * Get if we should be including the path when getting a config as a micrometer tag.
     * Warning this is a high cardinality tag and is not recommended.
     *
     * @return if we should be including the path as a micrometer tag.
     */
    public boolean isIncludePath() {
        return includePath;
    }

    /**
     * Get if we should be including the class we asked for when getting a config as a micrometer tag.
     * Warning this is a high cardinality tag and is not recommended.
     *
     * @return if we should be including the class as a micrometer tag.
     */
    public boolean isIncludeClass() {
        return includeClass;
    }

    /**
     * Get if we should be including if the configuration was optional (ie get Optional or get with a default)
     * when getting a config as a micrometer tag.
     *
     * @return if we should be including if the configuration was optional
     */
    public boolean isIncludeOptional() {
        return includeOptional;
    }

    /**
     * Get if we should be including any tags provided when getting a config as a micrometer tag.
     *
     * @return if we should be including any tags provided when getting a config as a micrometer tag.
     */
    public boolean isIncludeTags() {
        return includeTags;
    }

    /**
     * Get The prefix added to the micrometer metrics.
     *
     * @return The prefix added to the micrometer metrics
     */
    public String getPrefix() {
        return prefix;
    }

}
