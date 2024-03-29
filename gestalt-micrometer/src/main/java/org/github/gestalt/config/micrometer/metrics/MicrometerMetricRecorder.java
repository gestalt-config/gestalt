package org.github.gestalt.config.micrometer.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.github.gestalt.config.decoder.ObjectDecoder;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.metrics.MetricsRecord;
import org.github.gestalt.config.metrics.MetricsRecorder;
import org.github.gestalt.config.micrometer.config.MicrometerModuleConfig;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;

public final class MicrometerMetricRecorder implements MetricsRecorder {
    private static final System.Logger logger = System.getLogger(MicrometerMetricRecorder.class.getName());

    private MicrometerModuleConfig micrometerModuleConfig;
    private MeterRegistry meterRegistry;
    @Override
    public String recorderId() {
        return "MicrometerMetricRecorder";
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        micrometerModuleConfig = config.getModuleConfig(MicrometerModuleConfig.class);
        if(micrometerModuleConfig == null) {
            logger.log(System.Logger.Level.WARNING, "Micrometer metrics will be unavailable, as " +
                "MicrometerModuleConfig has not be registered with Gestalt though the builder");
        } else {
            meterRegistry = micrometerModuleConfig.getMeterRegistry();
        }
    }

    @Override
    public <T> MetricsRecord startGetConfig(String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {
        return null;
    }

    @Override
    public void finalizeGetConfig(MetricsRecord marker, Tags tags) {

    }

    @Override
    public <T> MetricsRecord startMetric(String metric, Tags tags) {
        return null;
    }

    @Override
    public void finalizeMetric(MetricsRecord marker, Tags tags) {

    }

    @Override
    public void recordMetric(String metric, int count, Tags tags) {

    }
}
