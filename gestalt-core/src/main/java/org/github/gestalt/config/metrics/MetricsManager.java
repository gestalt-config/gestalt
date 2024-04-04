package org.github.gestalt.config.metrics;

import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holds all the MetricsRecord from all metric recorders.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class MetricsManager {

    private final Map<String, MetricsRecorder> metricsRecorders;

    public MetricsManager(List<MetricsRecorder> recorder) {
        this.metricsRecorders = recorder.stream().collect(Collectors.toMap(MetricsRecorder::recorderId, Function.identity()));
    }

    public void addMetricsRecorder(MetricsRecorder recorder) {
        Objects.requireNonNull(recorder, "MetricsRecorder should not be null");
        this.metricsRecorders.put(recorder.recorderId(), recorder);
    }

    public void addMetricsRecorders(List<MetricsRecorder> recorder) {
        Objects.requireNonNull(recorder, "MetricsRecorder should not be null");
        recorder.forEach(it ->  this.metricsRecorders.put(it.recorderId(), it));
    }

    public <T> MetricsMarker startGetConfig(String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {
        var records = metricsRecorders.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().startGetConfig(path, klass, tags, isOptional)));

        return new MetricsMarker(records);
    }

    public void finalizeGetConfig(MetricsMarker markers, Tags tags) {
        if(markers != null) {
            metricsRecorders.forEach((key, value) -> value.finalizeMetric(markers.getMetricsRecord(key), tags));
        }
    }

    public MetricsMarker startMetric(String metric, Tags tags) {
        var records = metricsRecorders.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().startMetric(metric, tags)));

        return new MetricsMarker(records);
    }

    public void finalizeMetric(MetricsMarker markers, Tags tags) {
        if(markers != null) {
            metricsRecorders.forEach((key, value) -> value.finalizeMetric(markers.getMetricsRecord(key), tags));
        }
    }

    /**
     * Record a generic metric.
     *
     * @param metric Name of the metric to record
     * @param count the count to add to the metric
     * @param tags tags associated with the metrics
     */
    public void recordMetric(String metric, double count, Tags tags) {
        metricsRecorders.forEach((key, value) -> value.recordMetric(metric, count, tags));
    }
}
