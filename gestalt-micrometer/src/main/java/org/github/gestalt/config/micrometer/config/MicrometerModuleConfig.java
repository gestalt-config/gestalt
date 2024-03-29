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


    MicrometerModuleConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public String name() {
        return "micrometer";
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
