package org.github.gestalt.config.metrics;

import org.github.gestalt.config.tag.Tags;

public class TestMetricsRecord implements MetricsRecord {
    public double data;
    public boolean isOptional;
    public Tags tags;
    public String path;

    public TestMetricsRecord(String path, double data, boolean isOptional, Tags tags) {
        this.path = path;
        this.data = data;
        this.isOptional = isOptional;
        this.tags = tags;
    }

    @Override
    public String metric() {
        return path;
    }
}
