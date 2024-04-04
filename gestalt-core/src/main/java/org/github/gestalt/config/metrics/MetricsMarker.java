package org.github.gestalt.config.metrics;

import java.util.Map;

/**
 * Holds all the MetricsRecord from all metric recorders.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class MetricsMarker {
    private final Map<String, MetricsRecord> metricsRecords;

    /**
     * save a map of metric recorder Id to the metric record. So we can easily get the correct metrics record for a metrics recorder.
     *
     * @param metricsRecords map of metric recorder
     */
    public MetricsMarker(Map<String, MetricsRecord> metricsRecords) {
        this.metricsRecords = metricsRecords;
    }


    /**
     * Get a specific metrics record for a recorder.
     *
     * @param recorderId the id of the recorder.
     * @return MetricRecord for a specific recorder.
     */
    public MetricsRecord getMetricsRecord(String recorderId) {
        return metricsRecords.get(recorderId);
    }
}
