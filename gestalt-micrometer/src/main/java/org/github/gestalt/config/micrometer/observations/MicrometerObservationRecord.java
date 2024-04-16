package org.github.gestalt.config.micrometer.observations;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.github.gestalt.config.observations.ObservationRecord;

/**
 * Micrometer container for the MetricsRecord. It will hold details for any timer related metrics that have a start and a stop state.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class MicrometerObservationRecord implements ObservationRecord {

    private final String metric;
    private final Tags tags;
    private final Timer.Sample sample;

    public MicrometerObservationRecord(String metric, Timer.Sample sample, Tags tags) {
        this.metric = metric;
        this.sample = sample;
        this.tags = tags;
    }

    @Override
    public String metric() {
        return metric;
    }

    public Timer.Sample getSample() {
        return sample;
    }

    public Tags getTags() {
        return tags;
    }
}
