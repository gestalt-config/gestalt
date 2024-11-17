package org.github.gestalt.config.integration;

import org.github.gestalt.config.observations.ObservationRecord;
import org.github.gestalt.config.observations.ObservationRecorder;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tag;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestObservationRecorder implements ObservationRecorder {
    public final double recoderId;

    public Map<String, TestObservationRecord> metrics = new HashMap<>();

    public TestObservationRecorder(double recoderId) {
        this.recoderId = recoderId;
    }

    @Override
    public String recorderId() {
        return "testMetrics" + recoderId;
    }

    @Override
    public <T> ObservationRecord startGetConfig(String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {
        return new TestObservationRecord(path, recoderId, isOptional, tags);
    }

    @Override
    public ObservationRecord startObservation(String metric, Tags tags) {
        return new TestObservationRecord(metric, recoderId, false, tags);
    }

    @Override
    public void finalizeObservation(ObservationRecord marker, Tags tags) {
        var record = (TestObservationRecord) marker;

        record.data = record.data + 10;
        Set<Tag> copyTags = new HashSet<>(tags.getTags());
        copyTags.addAll(record.tags.getTags());

        record.tags = Tags.of(copyTags);   //NOPMD
        metrics.put(record.metric(), record);
    }

    @Override
    public void recordObservation(String observation, double count, Tags tags) {
        var metricRecord = metrics.get(observation);
        if (metricRecord != null) {
            metricRecord.data = metricRecord.data + count;
        } else {
            metrics.put(observation, new TestObservationRecord(observation, count + recoderId, false, tags));
        }
    }

    @Override
    public <T> void recordObservation(GResultOf<T> results, String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {

    }
}
