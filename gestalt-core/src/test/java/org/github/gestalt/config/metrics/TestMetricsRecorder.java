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
    public <T> MetricsRecord startMetric(String metric, Tags tags) {
        return new TestMetricsRecord(metric, recoderId, false, tags);
    }

    @Override
    public void finalizeMetric(MetricsRecord marker, Tags tags) {
        ((TestMetricsRecord) marker).data = ((TestMetricsRecord) marker).data + 10;
        Set<Tag> copyTags = new HashSet<>(tags.getTags());
        copyTags.addAll(((TestMetricsRecord) marker).tags.getTags());

        ((TestMetricsRecord) marker).tags = Tags.of(copyTags);   //NOPMD
    }

    @Override
    public void recordMetric(String metric, double count, Tags tags) {
        metrics.computeIfAbsent(metric, (it) -> new TestMetricsRecord(it, count + recoderId, false, tags));
    }
}
