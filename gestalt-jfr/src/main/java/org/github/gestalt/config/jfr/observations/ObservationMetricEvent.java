package org.github.gestalt.config.jfr.observations;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;

/**
 * JFR Event for recording observation metrics.
 * Captures metric name, count, and cumulative value.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@Label("Observation Metric")
@Description("Observation metric recorded during configuration access")
@Category("Gestalt Config Observation")
public final class ObservationMetricEvent extends Event {
    @Label("Metric Name")
    @Description("The name of the recorded metric")
    public String metricName;

    @Label("Count")
    @Description("The count value recorded")
    public double count;

    @Label("Cumulative Value")
    @Description("The cumulative value of the metric")
    public double cumulativeValue;
}
