package org.github.gestalt.config.jfr.observations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry to track observations using Double counters.
 * Maintains a thread-safe map of observation metrics.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ObservationRegistry {
    private final Map<String, Double> observations = Collections.synchronizedMap(new HashMap<>());

    /**
     * Record an observation by incrementing its counter.
     *
     * @param metricName the full metric name (including prefix)
     * @param count the amount to increment
     */
    public void recordObservation(String metricName, double count) {
        observations.merge(metricName, count, Double::sum);
    }

    /**
     * Get the current value of an observation.
     *
     * @param metricName the full metric name
     * @return the current value or 0 if not found
     */
    public double getValue(String metricName) {
        Double value = observations.get(metricName);
        return value != null ? value : 0.0;
    }

    /**
     * Get all observations as a map.
     *
     * @return a copy of the observations map
     */
    public Map<String, Double> getAllObservations() {
        return new HashMap<>(observations);
    }

    /**
     * Clear all observations.
     */
    public void clear() {
        observations.clear();
    }
}
