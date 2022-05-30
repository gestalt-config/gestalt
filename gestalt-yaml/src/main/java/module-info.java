/**
 * Module info definition for gestalt yaml integration
 */
module org.github.gestalt.yaml {
    requires org.github.gestalt.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    exports org.github.gestalt.config.yaml;
}
