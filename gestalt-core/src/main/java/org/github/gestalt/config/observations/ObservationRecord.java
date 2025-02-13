package org.github.gestalt.config.observations;

/**
 * An interface for an observation record, each observation implementation would add their own details to their implementation
 * to propagate observation data between a start call and a finish call.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface ObservationRecord {
    /**
     * Get the name of the current metric.
     *
     * @return the name of the current metric.
     */
    String metric();
}
