package org.github.gestalt.config.observations;

import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holds all the ObservationManager from all observations recorders.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ObservationManager implements ObservationService {

    private final Map<String, ObservationRecorder> observationRecorders;

    public ObservationManager(List<ObservationRecorder> recorder) {
        this.observationRecorders = recorder
            .stream()
            .collect(Collectors.toMap(ObservationRecorder::recorderId, Function.identity()));
    }

    @Override
    public void addObservationRecorder(ObservationRecorder recorder) {
        Objects.requireNonNull(recorder, "ObservationRecorder should not be null");
        this.observationRecorders.put(recorder.recorderId(), recorder);
    }

    @Override
    public void addObservationRecorders(List<ObservationRecorder> recorder) {
        Objects.requireNonNull(recorder, "ObservationRecorder should not be null");
        recorder.forEach(it -> this.observationRecorders.put(it.recorderId(), it));
    }

    @Override
    public <T> ObservationMarker startGetConfig(String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {
        var records = observationRecorders.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().startGetConfig(path, klass, tags, isOptional)));

        return new ObservationMarker(records);
    }

    @Override
    public void finalizeGetConfig(ObservationMarker markers, Tags tags) {
        if (markers != null) {
            observationRecorders.forEach((key, value) -> value.finalizeObservation(markers.getObservationRecord(key), tags));
        }
    }

    @Override
    public ObservationMarker startObservation(String metric, Tags tags) {
        var records = observationRecorders.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().startObservation(metric, tags)));

        return new ObservationMarker(records);
    }

    @Override
    public void finalizeObservation(ObservationMarker markers, Tags tags) {
        if (markers != null) {
            observationRecorders.forEach((key, value) -> value.finalizeObservation(markers.getObservationRecord(key), tags));
        }
    }

    /**
     * Record a generic observation.
     *
     * @param observation Name of the observation to record
     * @param count  the count to add to the observation
     * @param tags   tags associated with the metrics
     */
    @Override
    public void recordObservation(String observation, double count, Tags tags) {
        observationRecorders.forEach((key, value) -> value.recordObservation(observation, count, tags));
    }


    /**
     * Record the observation for a result.
     *
     * @param results the results of the request
     * @param path the path for the request
     * @param klass the type of object requested
     * @param tags the tags associated with the request
     * @param isOptional if the request result os option (ie an Optional or has a default value)
     * @param <T> generic type of config
     */
    @Override
    public <T> void recordObservation(GResultOf<T> results, String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {
        observationRecorders.forEach((key, value) -> value.recordObservation(results, path, klass, tags, isOptional));
    }
}
