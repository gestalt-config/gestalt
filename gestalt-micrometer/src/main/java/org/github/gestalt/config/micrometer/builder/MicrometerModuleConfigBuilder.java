package org.github.gestalt.config.micrometer.builder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.github.gestalt.config.micrometer.config.MicrometerModuleConfig;

public final class MicrometerModuleConfigBuilder {

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private Boolean includePath = false;
    private Boolean includeClass = false;
    private Boolean includeOptional = false;
    private Boolean includeTags = false;
    private String prefix = "gestalt";

    private MicrometerModuleConfigBuilder() {

    }

    /**
     * Create a builder to create the micrometer config.
     *
     * @return a builder to create the micrometer config.
     */
    public static MicrometerModuleConfigBuilder builder() {
        return new MicrometerModuleConfigBuilder();
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    public MicrometerModuleConfigBuilder setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        return this;
    }

    public Boolean getIncludePath() {
        return includePath;
    }

    public MicrometerModuleConfigBuilder setIncludePath(Boolean includePath) {
        this.includePath = includePath;
        return this;
    }

    public Boolean getIncludeClass() {
        return includeClass;
    }

    public MicrometerModuleConfigBuilder setIncludeClass(Boolean includeClass) {
        this.includeClass = includeClass;
        return this;
    }

    public Boolean getIncludeOptional() {
        return includeOptional;
    }

    public MicrometerModuleConfigBuilder setIncludeOptional(Boolean includeOptional) {
        this.includeOptional = includeOptional;
        return this;
    }

    public Boolean getIncludeTags() {
        return includeTags;
    }

    public MicrometerModuleConfigBuilder setIncludeTags(Boolean includeTags) {
        this.includeTags = includeTags;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public MicrometerModuleConfigBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public MicrometerModuleConfig build() {
        return new MicrometerModuleConfig(meterRegistry, includePath, includeClass, includeOptional, includeTags, prefix);
    }
}
