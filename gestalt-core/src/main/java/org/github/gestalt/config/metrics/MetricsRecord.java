package org.github.gestalt.config.metrics;

/**
 * An interface for a metrics record, each metrics implementation would add their own details to their implementation
 * to propagate metrics data between a start call and a finish call.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface MetricsRecord {
    /**
     * Get the name of the current metric.
     *
     * @return the name of the current metric.
     */
    String metric();
}
