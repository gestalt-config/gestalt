/*
 * Module info definition for gestalt yaml integration
 */
@SuppressWarnings({ "requires-transitive-automatic" })
module org.github.gestalt.micrometer {
    requires org.github.gestalt.core;
    requires transitive micrometer.core;

    exports org.github.gestalt.config.micrometer.config;
    exports org.github.gestalt.config.micrometer.builder;
    exports org.github.gestalt.config.micrometer.metrics;

    provides org.github.gestalt.config.metrics.MetricsRecorder with
        org.github.gestalt.config.micrometer.metrics.MicrometerMetricRecorder;
}
