/*
 * Module info definition for gestalt yaml integration
 */
module org.github.gestalt.toml {
    requires org.github.gestalt.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.toml;

    exports org.github.gestalt.config.toml;

    provides org.github.gestalt.config.loader.ConfigLoader with
        org.github.gestalt.config.toml.TomlLoader;
}
