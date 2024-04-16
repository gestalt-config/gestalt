package org.github.gestalt.config.observations;

import org.github.gestalt.config.tag.Tags;

public class TestObservationRecord implements ObservationRecord {
    public double data;
    public boolean isOptional;
    public Tags tags;
    public String path;

    public TestObservationRecord(String path, double data, boolean isOptional, Tags tags) {
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
