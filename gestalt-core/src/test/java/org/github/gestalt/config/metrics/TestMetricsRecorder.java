package org.github.gestalt.config.metrics;

import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tag;
import org.github.gestalt.config.tag.Tags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestMetricsRecorder implements MetricsRecorder {
    public final double recoderId;

    public Map<String, TestMetricsRecord> metrics = new HashMap<>();

    public TestMetricsRecorder(double recoderId) {
        this.recoderId = recoderId;
    }

    @Override
    public String recorderId() {
        return "testMetrics" + recoderId;
    }

    @Override
    public <T> MetricsRecord startGetConfig(String path, TypeCapture<T> klass, Tags tags, boolean isOptional) {
        return new TestMetricsRecord(path, recoderId, isOptional, tags);
    }

    @Override
    public MetricsRecord startMetric(String metric, Tags tags) {
        return new TestMetricsRecord(metric, recoderId, false, tags);
    }

    @Override
    public void finalizeMetric(MetricsRecord marker, Tags tags) {
        var record = (TestMetricsRecord) marker;

        record.data = record.data + 10;
        Set<Tag> copyTags = new HashSet<>(tags.getTags());
        copyTags.addAll(record.tags.getTags());

        record.tags = Tags.of(copyTags);   //NOPMD
        metrics.put(record.metric(), record);
    }

    @Override
    public void recordMetric(String metric, double count, Tags tags) {
        var metricRecord = metrics.get(metric);
        if (metricRecord != null) {
            metricRecord.data = metricRecord.data + count;
        } else {
            metrics.put(metric, new TestMetricsRecord(metric, count + recoderId, false, tags));
        }
    }
}
