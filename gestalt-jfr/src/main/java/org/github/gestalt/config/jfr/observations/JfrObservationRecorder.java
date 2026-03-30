package org.github.gestalt.config.jfr.observations;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.jfr.builder.JfrModuleConfigBuilder;
import org.github.gestalt.config.jfr.config.JfrModuleConfig;
import org.github.gestalt.config.observations.ObservationRecord;
import org.github.gestalt.config.observations.ObservationRecorder;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Java Flight Recorder (JFR) implementation of the ObservationRecorder.
 * Records configuration access events to JFR for monitoring and profiling.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class JfrObservationRecorder implements ObservationRecorder {
    private static final System.Logger logger = System.getLogger(JfrObservationRecorder.class.getName());

    private JfrModuleConfig jfrModuleConfig;
    private ObservationRegistry observationRegistry;

    @Override
    public String recorderId() {
        return "JfrObservationRecorder";
    }

    public ObservationRegistry getObservationRegistry() {
        return observationRegistry;
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        jfrModuleConfig = config.getModuleConfig(JfrModuleConfig.class);
        if (jfrModuleConfig == null) {
            jfrModuleConfig = JfrModuleConfigBuilder.builder().build();
            logger.log(System.Logger.Level.INFO, "JFR module config not provided, using defaults");
        }
        observationRegistry = new ObservationRegistry();
    }

    @Override
    public <T> ObservationRecord startGetConfig(String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {
        String className = klass != null ? klass.getRawType().getSimpleName() : "Unknown";

        String finalPath = path;
        if (!jfrModuleConfig.isIncludePath()) {
            finalPath = null;
        }

        String finalClassName = className;
        if (!jfrModuleConfig.isIncludeClass()) {
            finalClassName = null;
        }

        boolean finalIsOptional = isOptional;
        if (!jfrModuleConfig.isIncludeOptional()) {
            finalIsOptional = false;
        }

        Tags finalTags = tags;
        if (!jfrModuleConfig.isIncludeTags()) {
            finalTags = Tags.of();
        }

        return new JfrObservationRecord(finalPath, finalClassName, finalIsOptional, finalTags);
    }

    @Override
    public ObservationRecord startObservation(String metric, Tags tags) {
        Tags finalTags = tags;
        if (!jfrModuleConfig.isIncludeTags()) {
            finalTags = Tags.of();
        }

        return new JfrObservationRecord(metric, "Observation", false, finalTags);
    }

    @Override
    public void finalizeObservation(ObservationRecord marker, Tags tags) {
        if (marker instanceof JfrObservationRecord) {
            JfrObservationRecord jfrRecord = (JfrObservationRecord) marker;
            jfrRecord.complete();
        } else {
            logger.log(System.Logger.Level.WARNING, "wrong observation record received");
        }
    }

    @Override
    public void recordObservation(String observation, double count, Tags tags) {
        observationRegistry.recordObservation(observation, count);
        
        // Log observation metric as JFR event
        ObservationMetricEvent event = new ObservationMetricEvent();
        event.metricName = observation;
        event.count = count;
        event.cumulativeValue = observationRegistry.getValue(observation);
        event.commit();
    }

    @Override
    public <T> void recordObservation(GResultOf<T> results, String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {
        // Not recording direct observation results in JFR
    }
}
