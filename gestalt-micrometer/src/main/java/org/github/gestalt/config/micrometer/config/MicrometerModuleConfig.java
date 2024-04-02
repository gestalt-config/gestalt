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

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    public boolean isIncludePath() {
        return includePath;
    }

    public boolean isIncludeClass() {
        return includeClass;
    }

    public boolean isIncludeOptional() {
        return includeOptional;
    }

    public boolean isIncludeTags() {
        return includeTags;
    }

    public String getPrefix() {
        return prefix;
    }

}
