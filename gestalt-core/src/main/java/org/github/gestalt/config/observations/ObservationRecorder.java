package org.github.gestalt.config.observations;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Interface for specific observation systems to implement to get hoods to record observation.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface ObservationRecorder {

    /**
     * Unique ID for the Metrics Recorder that allows us to match ObservationRecorder to the recorder.
     *
     * @return Unique ID
     */
    String recorderId();

    /**
     * Apply the GestaltConfig to the ObservationRecorder. Needed when building via the ServiceLoader
     *
     * @param config GestaltConfig to update the ObservationRecorder
     */
    default void applyConfig(GestaltConfig config) {}

    /**
     * Called when we start getting a configuration.
     *
     * @param path path we are getting
     * @param klass klass we are getting
     * @param tags tags associated with the request
     * @param isOptional if the request is optional
     * @return MetricsRecord payload with specifics for the observation recorder.
     *     This is passed back to the {@link ObservationRecorder#finalizeObservation(ObservationRecord, Tags)} to record the observation
     * @param <T> class type we are getting.
     */
    <T> ObservationRecord startGetConfig(String path, TypeCapture<T> klass, Tags tags, boolean isOptional);


    /**
     * Called when we start a generic observation.
     *
     * @param metric Name of the observation to record
     * @param tags tags associated with the observation
     * @return MetricsRecord payload with specifics for the observation recorder
     */
    ObservationRecord startObservation(String metric, Tags tags);

    /**
     * Called to record the generic observation call.
     *
     * @param marker the payload with observation specific details.
     * @param tags any additional tags to add when finalizing such as errors
     */
    void finalizeObservation(ObservationRecord marker, Tags tags);

    /**
     * Record a generic observation.
     *
     * @param observation Name of the observation to record
     * @param count number to increment the observation
     * @param tags tags associated with the observations
     */
    void recordObservation(String observation, double count, Tags tags);

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
    <T> void recordObservation(GResultOf<T> results, String path, TypeCapture<T> klass, Tags tags, boolean isOptional);
}
