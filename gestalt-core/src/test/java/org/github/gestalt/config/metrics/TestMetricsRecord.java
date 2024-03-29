package org.github.gestalt.config.metrics;

import org.github.gestalt.config.tag.Tags;

public class TestMetricsRecord implements MetricsRecord {
    public long data;
    public boolean isOptional;
    public Tags tags;
    String path;

    public TestMetricsRecord(String path, long data, boolean isOptional, Tags tags) {
        this.path = path;
        this.data = data;
        this.isOptional = isOptional;
        this.tags = tags;
    }
}
