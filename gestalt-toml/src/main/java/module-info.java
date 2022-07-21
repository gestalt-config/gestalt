/**
 * Module info definition for gestalt yaml integration
 */
module org.github.gestalt.yaml {
    requires org.github.gestalt.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.toml;

    exports org.github.gestalt.config.toml;
}
