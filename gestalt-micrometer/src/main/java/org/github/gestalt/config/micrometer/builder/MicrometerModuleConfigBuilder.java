package org.github.gestalt.config.micrometer.builder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.micrometer.config.MicrometerModuleConfig;

/**
 * Module config for micrometer. If you wish to customize micrometer you need to register the results of this builder with
 * the {@link org.github.gestalt.config.builder.GestaltBuilder#addModuleConfig(GestaltModuleConfig)}.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
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

    /**
     * Get the custom micrometer meter registry.
     *
     * @return the custom micrometer meter registry.
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    /**
     * Set the custom micrometer meter registry.
     *
     * @param meterRegistry  the custom micrometer meter registry.
     * @return the builder.
     */
    public MicrometerModuleConfigBuilder setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        return this;
    }

    /**
     * Get if we should be including the path when getting a config as a micrometer tag.
     * Warning this is a high cardinality tag and is not recommended.
     *
     * @return if we should be including the path as a micrometer tag.
     */
    public Boolean getIncludePath() {
        return includePath;
    }

    /**
     * Set if we should be including the path as a micrometer tag.
     * Warning this is a high cardinality tag and is not recommended.
     *
     * @param includePath if we should be including the path as a micrometer tag.
     * @return the builder
     */
    public MicrometerModuleConfigBuilder setIncludePath(Boolean includePath) {
        this.includePath = includePath;
        return this;
    }

    /**
     * Get if we should be including the class we asked for when getting a config as a micrometer tag.
     * Warning this is a high cardinality tag and is not recommended.
     *
     * @return if we should be including the class as a micrometer tag.
     */
    public Boolean getIncludeClass() {
        return includeClass;
    }

    /**
     * Set if we should be including the class we asked for when getting a config as a micrometer tag.
     * Warning this is a high cardinality tag and is not recommended.
     *
     * @param includeClass Set if we should be including the class as a micrometer tag.
     * @return the builder
     */
    public MicrometerModuleConfigBuilder setIncludeClass(Boolean includeClass) {
        this.includeClass = includeClass;
        return this;
    }

    /**
     * Get if we should be including if the configuration was optional (ie get Optional or get with a default)
     * when getting a config as a micrometer tag.
     *
     * @return if we should be including if the configuration was optional
     */
    public Boolean getIncludeOptional() {
        return includeOptional;
    }

    /**
     * Set if we should be including if the configuration was optional (ie get Optional or get with a default)
     * when getting a config as a micrometer tag.
     *
     * @param includeOptional if we should be including if the configuration was optional
     * @return  the builder
     */
    public MicrometerModuleConfigBuilder setIncludeOptional(Boolean includeOptional) {
        this.includeOptional = includeOptional;
        return this;
    }

    /**
     * Get if we should be including any tags provided when getting a config as a micrometer tag.
     *
     * @return if we should be including any tags provided when getting a config as a micrometer tag.
     */
    public Boolean getIncludeTags() {
        return includeTags;
    }

    /**
     * Set if we should be including any tags provided when getting a config as a micrometer tag.
     *
     * @param includeTags  of we should be including any tags as a micrometer tag.
     * @return the builder
     */
    public MicrometerModuleConfigBuilder setIncludeTags(Boolean includeTags) {
        this.includeTags = includeTags;
        return this;
    }

    /**
     * Get The prefix added to the micrometer metrics.
     *
     * @return The prefix added to the micrometer metrics
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set The prefix added to the micrometer metrics.
     *
     * @param prefix The prefix added to the micrometer metrics.
     * @return The builder
     */
    public MicrometerModuleConfigBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Build the micrometer configuration to be registered with the
     * {@link org.github.gestalt.config.builder.GestaltBuilder#addModuleConfig(GestaltModuleConfig)}.
     *
     * @return the micrometer configuration.
     */
    public MicrometerModuleConfig build() {
        return new MicrometerModuleConfig(meterRegistry, includePath, includeClass, includeOptional, includeTags, prefix);
    }
}
