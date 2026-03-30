package org.github.gestalt.config.jfr.observations;

import org.github.gestalt.config.observations.ObservationRecord;
import org.github.gestalt.config.tag.Tags;

/**
 * JFR implementation of ObservationRecord.
 * Holds the state of a configuration access observation for JFR recording.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class JfrObservationRecord implements ObservationRecord {

    private final String metricName;
    private final GestaltConfigAccessEvent event;
    private final long startTime;

    public JfrObservationRecord(String path, String targetClass, boolean isOptional, Tags tags) {
        this.metricName = "config.get";
        this.event = new GestaltConfigAccessEvent();
        this.event.path = path;
        this.event.targetClass = targetClass;
        this.event.isOptional = isOptional;
        this.event.tags = tags == null ? "" : tags.toString();
        this.event.success = true;
        this.event.errorMessage = "";
        this.startTime = System.nanoTime();
    }

    @Override
    public String metric() {
        return metricName;
    }

    public GestaltConfigAccessEvent getEvent() {
        return event;
    }

    public long getStartTime() {
        return startTime;
    }

    public void complete() {
        long endTime = System.nanoTime();
        event.duration = endTime - startTime;

        try {
            event.commit();
        } catch (Exception e) {
            // JFR might not be available in all environments
            System.getLogger(JfrObservationRecord.class.getName())
                .log(System.Logger.Level.DEBUG, "Failed to commit JFR event: " + e.getMessage());
        }
    }
}

