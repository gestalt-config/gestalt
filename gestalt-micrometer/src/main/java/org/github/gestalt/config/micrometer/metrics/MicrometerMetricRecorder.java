package org.github.gestalt.config.micrometer.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.metrics.MetricsRecord;
import org.github.gestalt.config.metrics.MetricsRecorder;
import org.github.gestalt.config.micrometer.builder.MicrometerModuleConfigBuilder;
import org.github.gestalt.config.micrometer.config.MicrometerModuleConfig;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;

import java.util.stream.Collectors;

/**
 * Micrometer implementation of the MetricsRecorder. Allows you to submit metrics to your meterRegistry.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class MicrometerMetricRecorder implements MetricsRecorder {
    private static final System.Logger logger = System.getLogger(MicrometerMetricRecorder.class.getName());

    private MicrometerModuleConfig micrometerModuleConfig;
    private MeterRegistry meterRegistry;

    @Override
    public String recorderId() {
        return "MicrometerMetricRecorder";
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        micrometerModuleConfig = config.getModuleConfig(MicrometerModuleConfig.class);
        if (micrometerModuleConfig == null) {
            meterRegistry = new SimpleMeterRegistry();
            micrometerModuleConfig = MicrometerModuleConfigBuilder.builder().build();
            logger.log(System.Logger.Level.WARNING, "Micrometer metrics will be set to the defaults and use a " +
                "SimpleMeterRegistry, please register a MicrometerModuleConfig with gestalt builder to customize the metrics.");
        } else {
            meterRegistry = micrometerModuleConfig.getMeterRegistry();
        }
    }

    @Override
    public <T> MetricsRecord startGetConfig(String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {
        io.micrometer.core.instrument.Tags metricTags = io.micrometer.core.instrument.Tags.empty();

        if (micrometerModuleConfig.isIncludePath()) {
            metricTags = metricTags.and(Tag.of("path", path));
        }

        if (micrometerModuleConfig.isIncludeClass()) {
            metricTags = metricTags.and(Tag.of("class", klass.getRawType().getSimpleName()));
        }

        if (micrometerModuleConfig.isIncludeOptional()) {
            metricTags = metricTags.and(Tag.of("optional", Boolean.toString(isOptional)));
        }

        if (micrometerModuleConfig.isIncludeTags()) {
            metricTags = metricTags.and(tags.getTags()
                .stream()
                .map(it -> Tag.of(it.getKey(), it.getValue()))
                .collect(Collectors.toList()));
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        return new MicrometerMetricsRecord("config.get", sample, metricTags);
    }

    @Override
    public MetricsRecord startMetric(String metric, Tags tags) {
        io.micrometer.core.instrument.Tags metricTags = io.micrometer.core.instrument.Tags.empty();

        if (micrometerModuleConfig.isIncludeTags()) {
            metricTags = metricTags.and(tags.getTags()
                .stream()
                .map(it -> Tag.of(it.getKey(), it.getValue()))
                .collect(Collectors.toList()));
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        return new MicrometerMetricsRecord(metric, sample, metricTags);
    }

    @Override
    public void finalizeMetric(MetricsRecord marker, Tags tags) {
        if (marker instanceof MicrometerMetricsRecord) {
            MicrometerMetricsRecord micrometerMetricsRecord = (MicrometerMetricsRecord) marker;

            io.micrometer.core.instrument.Tags metricTags = micrometerMetricsRecord.getTags();

            metricTags = metricTags.and(tags.getTags()
                .stream()
                .map(it -> Tag.of(it.getKey(), it.getValue()))
                .collect(Collectors.toList()));

            Timer recordTimer = meterRegistry.timer(
                micrometerModuleConfig.getPrefix() + "." + micrometerMetricsRecord.metric(), metricTags);
            Timer.Sample sample = micrometerMetricsRecord.getSample();

            sample.stop(recordTimer);

        } else {
            logger.log(System.Logger.Level.WARNING, "wrong metric recorder received. ");
        }

    }

    @Override
    public void recordMetric(String metric, double count, Tags tags) {
        io.micrometer.core.instrument.Tags metricTags = io.micrometer.core.instrument.Tags.empty();

        metricTags = metricTags.and(tags.getTags()
            .stream()
            .map(it -> Tag.of(it.getKey(), it.getValue()))
            .collect(Collectors.toList()));

        var counter = meterRegistry.counter(micrometerModuleConfig.getPrefix() + "." + metric, metricTags);
        counter.increment(count);
    }
}
