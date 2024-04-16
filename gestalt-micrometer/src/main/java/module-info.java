import org.github.gestalt.config.observations.ObservationRecorder;
import org.github.gestalt.config.micrometer.observations.MicrometerObservationRecorder;

/*
 * Module info definition for gestalt micrometer integration
 */
@SuppressWarnings({ "requires-transitive-automatic" })
module org.github.gestalt.micrometer {
    requires org.github.gestalt.core;
    requires transitive micrometer.core;

    exports org.github.gestalt.config.micrometer.config;
    exports org.github.gestalt.config.micrometer.builder;
    exports org.github.gestalt.config.micrometer.observations;

    provides ObservationRecorder with
        MicrometerObservationRecorder;
}
