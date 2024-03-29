package org.github.gestalt.config.micrometer.builder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MicrometerModuleConfigBuilder {

   private MeterRegistry meterRegistry = new SimpleMeterRegistry();

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

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
}
