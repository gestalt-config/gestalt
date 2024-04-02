package org.github.gestalt.config.metrics;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;

/**
 * Interface for specific metrics systems to implement to get hoods to record metrics.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface MetricsRecorder {

    /**
     * Unique ID for the Metrics Recorder that allows us to match MetricsRecord to the recorder.
     *
     * @return Unique ID
     */
    String recorderId();

    /**
     * Apply the GestaltConfig to the MetricsRecorder. Needed when building via the ServiceLoader
     *
     * @param config GestaltConfig to update the MetricsRecorder
     */
    default void applyConfig(GestaltConfig config) {}

    /**
     * Called when we start getting a configuration.
     *
     * @param path path we are getting
     * @param klass klass we are getting
     * @param tags tags associated with the request
     * @param isOptional if the request is optional
     * @return MetricsRecord payload with specifics for the metrics recorder
     * @param <T> class type we are getting.
     */
    <T> MetricsRecord startGetConfig(String path, TypeCapture<T> klass, Tags tags, boolean isOptional);


    /**
     * Called when we start a generic metrics.
     *
     * @param metric Name of the metric to record
     * @param tags tags associated with the metrics
     * @return MetricsRecord payload with specifics for the metrics recorder
     * @param <T> class type we are getting.
     */
    <T> MetricsRecord startMetric(String metric, Tags tags);

    /**
     * Called to record the generic metrics call.
     *
     * @param marker the payload with metrics specific details.
     * @param tags any additional tags to add when finalizing such as errors
     */
    void finalizeMetric(MetricsRecord marker, Tags tags);

    /**
     * Record a generic metric.
     *
     * @param metric Name of the metric to record
     * @param count number to increment the metric
     * @param tags tags associated with the metrics
     */
    void recordMetric(String metric, double count, Tags tags);
}
