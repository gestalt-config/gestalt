package org.github.gestalt.config.observations;

import java.util.Map;

/**
 * Holds all the ObservationMarker from all observation recorders.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ObservationMarker {
    private final Map<String, ObservationRecord> observationRecords;

    /**
     * save a map of observation recorder Id to the observation record.
     * So we can easily get the correct observation record for an observation recorder.
     *
     * @param observationRecord map of metric recorder
     */
    public ObservationMarker(Map<String, ObservationRecord> observationRecord) {
        this.observationRecords = observationRecord;
    }


    /**
     * Get a specific observation record for a recorder.
     *
     * @param recorderId the id of the recorder.
     * @return ObservationRecord for a specific recorder.
     */
    public ObservationRecord getObservationRecord(String recorderId) {
        return observationRecords.get(recorderId);
    }
}
